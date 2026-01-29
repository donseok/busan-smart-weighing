using System.Net;
using Newtonsoft.Json;
using WeighingCS.Models;
using WeighingCS.Services;

namespace WeighingCS.Tests;

/// <summary>
/// ApiService.HandleResponseAsync 응답 처리 단위 테스트.
/// HTTP 상태코드, API 에러코드/메시지 파싱, 성공 응답을 검증.
/// </summary>
public class ApiServiceTests : IDisposable
{
    private readonly ApiService _api;
    private readonly List<string> _capturedErrors = new();

    public ApiServiceTests()
    {
        var config = new ApiConfig
        {
            BaseUrl = "http://localhost:9999",
            LoginId = "test",
            Password = "test"
        };
        _api = new ApiService(config, scaleId: 1);
        _api.ApiError += (_, msg) => _capturedErrors.Add(msg);
    }

    public void Dispose()
    {
        _api.Dispose();
    }

    [Fact]
    public async Task HandleResponseAsync_SuccessResponse_ReturnsData()
    {
        var apiResponse = new ApiResponse<TestData>
        {
            Success = true,
            Data = new TestData { Id = 42, Name = "Test" }
        };

        var httpResponse = CreateHttpResponse(HttpStatusCode.OK, apiResponse);

        var result = await _api.HandleResponseAsync<TestData>(httpResponse);

        Assert.NotNull(result);
        Assert.Equal(42, result!.Id);
        Assert.Equal("Test", result.Name);
        Assert.Empty(_capturedErrors);
    }

    [Fact]
    public async Task HandleResponseAsync_ApiErrorWithCode_ReportsCodeAndMessage()
    {
        var apiResponse = new ApiResponse<object>
        {
            Success = false,
            Error = new ApiError { Code = "AUTH_001", Message = "인증 실패" }
        };

        var httpResponse = CreateHttpResponse(HttpStatusCode.OK, apiResponse);

        var result = await _api.HandleResponseAsync<object>(httpResponse);

        Assert.Null(result);
        Assert.Single(_capturedErrors);
        Assert.Contains("AUTH_001", _capturedErrors[0]);
        Assert.Contains("인증 실패", _capturedErrors[0]);
    }

    [Fact]
    public async Task HandleResponseAsync_HttpError_ReportsStatusCode()
    {
        var errorBody = new ApiResponse<object>
        {
            Success = false,
            Error = new ApiError { Code = "USER_001", Message = "사용자를 찾을 수 없습니다" }
        };

        var httpResponse = CreateHttpResponse(HttpStatusCode.NotFound, errorBody);

        var result = await _api.HandleResponseAsync<object>(httpResponse);

        Assert.Null(result);
        Assert.Single(_capturedErrors);
        Assert.Contains("404", _capturedErrors[0]);
        Assert.Contains("USER_001", _capturedErrors[0]);
    }

    [Fact]
    public async Task HandleResponseAsync_HttpErrorPlainText_ReportsRawContent()
    {
        var httpResponse = new HttpResponseMessage(HttpStatusCode.InternalServerError)
        {
            Content = new StringContent("Internal Server Error")
        };

        var result = await _api.HandleResponseAsync<object>(httpResponse);

        Assert.Null(result);
        Assert.Single(_capturedErrors);
        Assert.Contains("500", _capturedErrors[0]);
        Assert.Contains("Internal Server Error", _capturedErrors[0]);
    }

    [Fact]
    public async Task HandleResponseAsync_NullApiResponse_ReturnsDefault()
    {
        var httpResponse = new HttpResponseMessage(HttpStatusCode.OK)
        {
            Content = new StringContent("null")
        };

        var result = await _api.HandleResponseAsync<TestData>(httpResponse);

        Assert.Null(result);
    }

    [Fact]
    public async Task HandleResponseAsync_SuccessFalseWithMessageOnly_ReportsMessage()
    {
        var apiResponse = new ApiResponse<object>
        {
            Success = false,
            Message = "잘못된 요청입니다"
        };

        var httpResponse = CreateHttpResponse(HttpStatusCode.OK, apiResponse);

        var result = await _api.HandleResponseAsync<object>(httpResponse);

        Assert.Null(result);
        Assert.Single(_capturedErrors);
        Assert.Contains("잘못된 요청입니다", _capturedErrors[0]);
    }

    private static HttpResponseMessage CreateHttpResponse<T>(HttpStatusCode statusCode, T body)
    {
        string json = JsonConvert.SerializeObject(body);
        return new HttpResponseMessage(statusCode)
        {
            Content = new StringContent(json, System.Text.Encoding.UTF8, "application/json")
        };
    }

    public class TestData
    {
        public int Id { get; set; }
        public string Name { get; set; } = string.Empty;
    }
}
