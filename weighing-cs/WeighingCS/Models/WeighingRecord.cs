using Newtonsoft.Json;

namespace WeighingCS.Models;

/// <summary>
/// Represents a single weighing transaction record.
/// </summary>
public class WeighingRecord
{
    [JsonProperty("weighingId")]
    public long WeighingId { get; set; }

    [JsonProperty("dispatchId")]
    public long DispatchId { get; set; }

    [JsonProperty("scaleId")]
    public int ScaleId { get; set; }

    [JsonProperty("weighingMode")]
    public string WeighingMode { get; set; } = WeighingModes.Auto;

    [JsonProperty("grossWeight")]
    public decimal GrossWeight { get; set; }

    [JsonProperty("tareWeight")]
    public decimal TareWeight { get; set; }

    [JsonProperty("netWeight")]
    public decimal NetWeight { get; set; }

    [JsonProperty("status")]
    public string Status { get; set; } = WeighingStatuses.Pending;

    [JsonProperty("plateNumber")]
    public string? PlateNumber { get; set; }

    [JsonProperty("vehicleId")]
    public long? VehicleId { get; set; }

    [JsonProperty("weighingDatetime")]
    public DateTime WeighingDatetime { get; set; } = DateTime.Now;

    [JsonProperty("reWeighReason")]
    public string? ReWeighReason { get; set; }

    [JsonProperty("originalWeighingId")]
    public long? OriginalWeighingId { get; set; }

    /// <summary>
    /// Local-only flag indicating this record has not been synced to the server.
    /// </summary>
    [JsonIgnore]
    public bool IsCached { get; set; }

    /// <summary>
    /// Local-only timestamp for cache ordering (FIFO sync).
    /// </summary>
    [JsonIgnore]
    public DateTime? CachedAt { get; set; }
}

/// <summary>
/// Constants for WeighingRecord.WeighingMode.
/// </summary>
public static class WeighingModes
{
    public const string Auto = "AUTO";
    public const string Manual = "MANUAL";
    public const string ReWeigh = "RE_WEIGH";
}

/// <summary>
/// Constants for WeighingRecord.Status.
/// </summary>
public static class WeighingStatuses
{
    public const string Pending = "PENDING";
    public const string InProgress = "IN_PROGRESS";
    public const string Completed = "COMPLETED";
    public const string ReWeighing = "RE_WEIGHING";
    public const string Error = "ERROR";
    public const string Cancelled = "CANCELLED";
}
