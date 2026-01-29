using System.Net.Http.Headers;
using System.Text;
using Newtonsoft.Json;
using WeighingCS.Models;

namespace WeighingCS.Services;

/// <summary>
/// HTTP API client for the Spring Boot weighing backend.
/// Handles JWT authentication, automatic token refresh, and typed response parsing.
/// </summary>
public sealed class ApiService : IDisposable
{
    private const int MaxRetries = 3;
    private const int RetryDelayMs = 1000;

    private readonly HttpClient _http;
    private readonly ApiConfig _config;
    private readonly int _scaleId;

    private string? _accessToken;
    private string? _refreshToken;
    private DateTime _tokenExpiry = DateTime.MinValue;
    private bool _disposed;

    // -- Events ---------------------------------------------------------------

    public event EventHandler<bool>? NetworkStatusChanged;
    public event EventHandler<string>? ApiError;

    // -- Public state ---------------------------------------------------------

    public bool IsAuthenticated => !string.IsNullOrEmpty(_accessToken) && DateTime.Now < _tokenExpiry;
    public bool IsNetworkAvailable { get; private set; } = true;

    // -- Constructor -----------------------------------------------------------

    public ApiService(ApiConfig apiConfig, int scaleId)
    {
        _config = apiConfig ?? throw new ArgumentNullException(nameof(apiConfig));
        _scaleId = scaleId;

        _http = new HttpClient
        {
            BaseAddress = new Uri(_config.BaseUrl.TrimEnd('/')),
            Timeout = TimeSpan.FromSeconds(15)
        };
    }

    // -- Authentication -------------------------------------------------------

    public async Task<bool> LoginAsync()
    {
        try
        {
            var payload = new { loginId = _config.LoginId, password = _config.Password };
            var response = await PostAsync<AuthTokenResponse>("/auth/login", payload, authenticated: false);

            if (response is not null)
            {
                _accessToken = response.AccessToken;
                _refreshToken = response.RefreshToken;
                _tokenExpiry = DateTime.Now.AddSeconds(response.ExpiresIn > 0 ? response.ExpiresIn : 3600);
                SetNetworkAvailable(true);
                return true;
            }
        }
        catch (Exception ex)
        {
            ApiError?.Invoke(this, $"Login failed: {ex.Message}");
        }

        return false;
    }

    private async Task EnsureAuthenticatedAsync()
    {
        if (DateTime.Now >= _tokenExpiry.AddMinutes(-1))
        {
            // Token is expired or about to expire. Try refresh first, then full login.
            bool refreshed = false;

            if (!string.IsNullOrEmpty(_refreshToken))
            {
                try
                {
                    var payload = new { refreshToken = _refreshToken };
                    var response = await PostAsync<AuthTokenResponse>("/auth/refresh", payload, authenticated: false);
                    if (response is not null)
                    {
                        _accessToken = response.AccessToken;
                        _refreshToken = response.RefreshToken;
                        _tokenExpiry = DateTime.Now.AddSeconds(response.ExpiresIn > 0 ? response.ExpiresIn : 3600);
                        refreshed = true;
                    }
                }
                catch
                {
                    // Fall through to full login.
                }
            }

            if (!refreshed)
            {
                await LoginAsync();
            }
        }
    }

    // -- Weighing API ---------------------------------------------------------

    public async Task<WeighingRecord?> CreateWeighingAsync(WeighingRecord record)
    {
        await EnsureAuthenticatedAsync();
        return await PostAsync<WeighingRecord>("/weighings", record);
    }

    public async Task<WeighingRecord?> UpdateWeighingAsync(long weighingId, WeighingRecord record)
    {
        await EnsureAuthenticatedAsync();
        return await PutAsync<WeighingRecord>($"/weighings/{weighingId}", record);
    }

    public async Task<WeighingRecord?> GetWeighingAsync(long weighingId)
    {
        await EnsureAuthenticatedAsync();
        return await GetAsync<WeighingRecord>($"/weighings/{weighingId}");
    }

    // -- Dispatch API ---------------------------------------------------------

    public async Task<List<DispatchInfo>?> GetDispatchesAsync(string? plateNumber = null, string? status = null)
    {
        await EnsureAuthenticatedAsync();

        var queryParams = new List<string>();
        if (!string.IsNullOrEmpty(plateNumber))
            queryParams.Add($"plateNumber={Uri.EscapeDataString(plateNumber)}");
        if (!string.IsNullOrEmpty(status))
            queryParams.Add($"status={Uri.EscapeDataString(status)}");

        string query = queryParams.Count > 0 ? "?" + string.Join("&", queryParams) : "";
        return await GetAsync<List<DispatchInfo>>($"/dispatches{query}");
    }

    public async Task<DispatchInfo?> GetDispatchAsync(long dispatchId)
    {
        await EnsureAuthenticatedAsync();
        return await GetAsync<DispatchInfo>($"/dispatches/{dispatchId}");
    }

    // -- LPR / AI Verification API --------------------------------------------

    public async Task<LprCaptureResult?> RegisterLprCaptureAsync(string plateNumber, double confidence, string? imageUrl)
    {
        await EnsureAuthenticatedAsync();
        var payload = new
        {
            scaleId = _scaleId,
            plateNumber,
            confidence,
            captureImageUrl = imageUrl
        };
        return await PostAsync<LprCaptureResult>("/lpr/captures", payload);
    }

    public async Task<LprCaptureResult?> VerifyAiAsync(long captureId)
    {
        await EnsureAuthenticatedAsync();
        return await PostAsync<LprCaptureResult>($"/lpr/captures/{captureId}/verify", new { });
    }

    public async Task<LprCaptureResult?> MatchDispatchAsync(long captureId)
    {
        await EnsureAuthenticatedAsync();
        return await PostAsync<LprCaptureResult>($"/lpr/captures/{captureId}/match", new { scaleId = _scaleId });
    }

    // -- Gate Pass API --------------------------------------------------------

    public async Task<bool> CreateGatePassAsync(long weighingId)
    {
        await EnsureAuthenticatedAsync();
        var payload = new { weighingId, scaleId = _scaleId };
        var result = await PostAsync<object>("/gate-passes", payload);
        return result is not null;
    }

    // -- Network health check -------------------------------------------------

    public async Task<bool> CheckNetworkAsync()
    {
        try
        {
            using var request = new HttpRequestMessage(HttpMethod.Get, "/health");
            using var response = await _http.SendAsync(request, HttpCompletionOption.ResponseHeadersRead);
            bool available = response.IsSuccessStatusCode;
            SetNetworkAvailable(available);
            return available;
        }
        catch
        {
            SetNetworkAvailable(false);
            return false;
        }
    }

    private void SetNetworkAvailable(bool available)
    {
        if (IsNetworkAvailable != available)
        {
            IsNetworkAvailable = available;
            NetworkStatusChanged?.Invoke(this, available);
        }
    }

    // -- HTTP helpers with retry -----------------------------------------------

    private Task<T?> GetAsync<T>(string path, bool authenticated = true)
        => SendAsync<T>(HttpMethod.Get, path, body: null, authenticated: authenticated);

    private Task<T?> PostAsync<T>(string path, object? body, bool authenticated = true)
        => SendAsync<T>(HttpMethod.Post, path, body, authenticated);

    private Task<T?> PutAsync<T>(string path, object? body, bool authenticated = true)
        => SendAsync<T>(HttpMethod.Put, path, body, authenticated);

    private async Task<T?> SendAsync<T>(HttpMethod method, string path, object? body, bool authenticated = true)
    {
        return await ExecuteWithRetryAsync<T>(async () =>
        {
            using var request = new HttpRequestMessage(method, path);
            if (authenticated) AttachAuth(request);
            if (body is not null)
            {
                string json = JsonConvert.SerializeObject(body);
                request.Content = new StringContent(json, Encoding.UTF8, "application/json");
            }
            using var response = await _http.SendAsync(request);
            return await HandleResponseAsync<T>(response);
        });
    }

    private async Task<T?> ExecuteWithRetryAsync<T>(Func<Task<T?>> action)
    {
        Exception? lastException = null;

        for (int attempt = 1; attempt <= MaxRetries; attempt++)
        {
            try
            {
                T? result = await action();
                SetNetworkAvailable(true);
                return result;
            }
            catch (HttpRequestException ex)
            {
                lastException = ex;
                SetNetworkAvailable(false);
                if (attempt < MaxRetries)
                {
                    await Task.Delay(RetryDelayMs * attempt);
                }
            }
            catch (TaskCanceledException ex) when (!ex.CancellationToken.IsCancellationRequested)
            {
                // HTTP timeout
                lastException = ex;
                if (attempt < MaxRetries)
                {
                    await Task.Delay(RetryDelayMs * attempt);
                }
            }
        }

        ApiError?.Invoke(this, $"API call failed after {MaxRetries} retries: {lastException?.Message}");
        throw lastException ?? new InvalidOperationException("API call failed.");
    }

    internal async Task<T?> HandleResponseAsync<T>(HttpResponseMessage response)
    {
        string content = await response.Content.ReadAsStringAsync();

        if (!response.IsSuccessStatusCode)
        {
            int statusCode = (int)response.StatusCode;
            string errorDetail;

            try
            {
                var errorResponse = JsonConvert.DeserializeObject<ApiResponse<object>>(content);
                string errorCode = errorResponse?.Error?.Code ?? "";
                string errorMessage = errorResponse?.Error?.Message ?? errorResponse?.Message ?? content;
                errorDetail = !string.IsNullOrEmpty(errorCode)
                    ? $"HTTP {statusCode} [{errorCode}]: {errorMessage}"
                    : $"HTTP {statusCode}: {errorMessage}";
            }
            catch
            {
                errorDetail = $"HTTP {statusCode}: {content}";
            }

            ApiError?.Invoke(this, errorDetail);
            return default;
        }

        var apiResponse = JsonConvert.DeserializeObject<ApiResponse<T>>(content);

        if (apiResponse is null)
        {
            return default;
        }

        if (!apiResponse.Success)
        {
            string errorCode = apiResponse.Error?.Code ?? "";
            string errorMsg = apiResponse.Error?.Message ?? apiResponse.Message ?? "Unknown API error";
            string detail = !string.IsNullOrEmpty(errorCode)
                ? $"[{errorCode}] {errorMsg}"
                : errorMsg;
            ApiError?.Invoke(this, detail);
            return default;
        }

        return apiResponse.Data;
    }

    private void AttachAuth(HttpRequestMessage request)
    {
        if (!string.IsNullOrEmpty(_accessToken))
        {
            request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", _accessToken);
        }
    }

    // -- IDisposable -----------------------------------------------------------

    public void Dispose()
    {
        if (_disposed) return;
        _disposed = true;
        _http.Dispose();
    }
}
