using Newtonsoft.Json;

namespace WeighingCS.Models;

/// <summary>
/// Generic API response wrapper matching the Spring Boot backend format.
/// </summary>
public class ApiResponse<T>
{
    [JsonProperty("success")]
    public bool Success { get; set; }

    [JsonProperty("data")]
    public T? Data { get; set; }

    [JsonProperty("message")]
    public string? Message { get; set; }

    [JsonProperty("error")]
    public ApiError? Error { get; set; }

    [JsonProperty("timestamp")]
    public string? Timestamp { get; set; }
}

/// <summary>
/// Non-generic API response for calls that do not return typed data.
/// </summary>
public class ApiResponse : ApiResponse<object>
{
}

/// <summary>
/// Error detail block returned by the backend.
/// </summary>
public class ApiError
{
    [JsonProperty("code")]
    public string? Code { get; set; }

    [JsonProperty("message")]
    public string? Message { get; set; }
}

/// <summary>
/// JWT authentication response from the login endpoint.
/// </summary>
public class AuthTokenResponse
{
    [JsonProperty("accessToken")]
    public string AccessToken { get; set; } = string.Empty;

    [JsonProperty("refreshToken")]
    public string? RefreshToken { get; set; }

    [JsonProperty("expiresIn")]
    public long ExpiresIn { get; set; }
}
