using Newtonsoft.Json;

namespace WeighingCS.Models;

/// <summary>
/// Result from the LPR (License Plate Recognition) capture and verification pipeline.
/// </summary>
public class LprCaptureResult
{
    [JsonProperty("captureId")]
    public long CaptureId { get; set; }

    [JsonProperty("plateNumber")]
    public string PlateNumber { get; set; } = string.Empty;

    [JsonProperty("confidence")]
    public double Confidence { get; set; }

    [JsonProperty("captureImageUrl")]
    public string? CaptureImageUrl { get; set; }

    [JsonProperty("capturedAt")]
    public DateTime CapturedAt { get; set; } = DateTime.Now;

    [JsonProperty("verificationStatus")]
    public string VerificationStatus { get; set; } = LprVerificationStatuses.Pending;

    [JsonProperty("matchResult")]
    public string MatchResult { get; set; } = LprMatchResults.NoMatch;

    [JsonProperty("dispatches")]
    public List<DispatchInfo> Dispatches { get; set; } = new();

    /// <summary>
    /// True when AI confidence >= 0.90 and exactly one dispatch matched.
    /// </summary>
    [JsonIgnore]
    public bool IsAutoWeighEligible =>
        Confidence >= 0.90
        && VerificationStatus == LprVerificationStatuses.Verified
        && MatchResult == LprMatchResults.SingleMatch
        && Dispatches.Count == 1;
}

/// <summary>
/// Constants for LprCaptureResult.VerificationStatus.
/// </summary>
public static class LprVerificationStatuses
{
    public const string Pending = "PENDING";
    public const string Verified = "VERIFIED";
    public const string Rejected = "REJECTED";
    public const string ManualRequired = "MANUAL_REQUIRED";
}

/// <summary>
/// Constants for LprCaptureResult.MatchResult.
/// </summary>
public static class LprMatchResults
{
    public const string NoMatch = "NO_MATCH";
    public const string SingleMatch = "SINGLE_MATCH";
    public const string MultipleMatch = "MULTIPLE_MATCH";
}
