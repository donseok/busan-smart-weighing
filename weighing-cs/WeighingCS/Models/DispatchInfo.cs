using Newtonsoft.Json;

namespace WeighingCS.Models;

/// <summary>
/// Dispatch information linked to a weighing transaction.
/// </summary>
public class DispatchInfo
{
    [JsonProperty("dispatchId")]
    public long DispatchId { get; set; }

    [JsonProperty("vehicleId")]
    public long VehicleId { get; set; }

    [JsonProperty("companyId")]
    public long CompanyId { get; set; }

    [JsonProperty("itemType")]
    public string ItemType { get; set; } = string.Empty;

    [JsonProperty("itemName")]
    public string ItemName { get; set; } = string.Empty;

    [JsonProperty("plateNumber")]
    public string PlateNumber { get; set; } = string.Empty;

    [JsonProperty("companyName")]
    public string? CompanyName { get; set; }

    [JsonProperty("driverName")]
    public string? DriverName { get; set; }

    [JsonProperty("driverPhone")]
    public string? DriverPhone { get; set; }

    [JsonProperty("expectedWeight")]
    public decimal? ExpectedWeight { get; set; }

    [JsonProperty("tareWeight")]
    public decimal? TareWeight { get; set; }

    [JsonProperty("dispatchStatus")]
    public string? DispatchStatus { get; set; }

    [JsonProperty("scheduledDate")]
    public DateTime? ScheduledDate { get; set; }
}
