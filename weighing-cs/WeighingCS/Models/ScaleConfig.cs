using Newtonsoft.Json;

namespace WeighingCS.Models;

/// <summary>
/// Configuration for a weighing scale station, loaded from appsettings.json.
/// </summary>
public class ScaleConfig
{
    [JsonProperty("scaleId")]
    public int ScaleId { get; set; } = 1;

    [JsonProperty("comPort")]
    public string ComPort { get; set; } = "COM1";

    [JsonProperty("baudRate")]
    public int BaudRate { get; set; } = 9600;

    [JsonProperty("stabilityCount")]
    public int StabilityCount { get; set; } = 5;

    [JsonProperty("toleranceKg")]
    public decimal ToleranceKg { get; set; } = 0.5m;
}

/// <summary>
/// API connection configuration.
/// </summary>
public class ApiConfig
{
    [JsonProperty("baseUrl")]
    public string BaseUrl { get; set; } = "http://localhost:8080/api/v1";

    [JsonProperty("loginId")]
    public string LoginId { get; set; } = "scale-cs";

    [JsonProperty("password")]
    public string Password { get; set; } = "password";
}

/// <summary>
/// TCP device connection configuration (display board, barrier).
/// </summary>
public class DeviceConnectionConfig
{
    [JsonProperty("host")]
    public string Host { get; set; } = "127.0.0.1";

    [JsonProperty("port")]
    public int Port { get; set; }
}

/// <summary>
/// Local database configuration.
/// </summary>
public class DatabaseConfig
{
    [JsonProperty("path")]
    public string Path { get; set; } = "weighing_cache.db";
}

/// <summary>
/// Root configuration object matching appsettings.json structure.
/// </summary>
public class AppSettings
{
    [JsonProperty("Scale")]
    public ScaleConfig Scale { get; set; } = new();

    [JsonProperty("Api")]
    public ApiConfig Api { get; set; } = new();

    [JsonProperty("DisplayBoard")]
    public DeviceConnectionConfig DisplayBoard { get; set; } = new();

    [JsonProperty("Barrier")]
    public DeviceConnectionConfig Barrier { get; set; } = new();

    [JsonProperty("Database")]
    public DatabaseConfig Database { get; set; } = new();
}
