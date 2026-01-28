namespace WeighingCS.Interfaces;

public interface ILprCamera
{
    event EventHandler<LprPlateCapturedEventArgs>? PlateCaptured;
    Task<Models.LprCaptureResult> CaptureAsync();
    bool IsConnected { get; }
}

public class LprPlateCapturedEventArgs : EventArgs
{
    public string PlateNumber { get; set; } = string.Empty;
    public double Confidence { get; set; }
    public string? ImageUrl { get; set; }
    public DateTime CapturedAt { get; set; } = DateTime.Now;
}
