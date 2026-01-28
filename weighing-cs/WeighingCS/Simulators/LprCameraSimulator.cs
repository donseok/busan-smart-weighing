using WeighingCS.Interfaces;
using WeighingCS.Models;

namespace WeighingCS.Simulators;

public class LprCameraSimulator : ILprCamera
{
    private readonly Random _random = new();
    private readonly string[] _samplePlates = { "12가3456", "34나5678", "56다7890", "78라9012", "90마1234" };

    public event EventHandler<LprPlateCapturedEventArgs>? PlateCaptured;
    public bool IsConnected => true;

    public double MinConfidence { get; set; } = 0.85;
    public double MaxConfidence { get; set; } = 0.99;

    public Task<LprCaptureResult> CaptureAsync()
    {
        var plate = _samplePlates[_random.Next(_samplePlates.Length)];
        var confidence = MinConfidence + _random.NextDouble() * (MaxConfidence - MinConfidence);
        confidence = Math.Round(confidence, 4);

        var result = new LprCaptureResult
        {
            PlateNumber = plate,
            Confidence = confidence,
            CapturedAt = DateTime.Now,
            VerificationStatus = confidence >= 0.90 ? LprVerificationStatuses.Verified : LprVerificationStatuses.ManualRequired,
        };

        PlateCaptured?.Invoke(this, new LprPlateCapturedEventArgs
        {
            PlateNumber = plate,
            Confidence = confidence,
            CapturedAt = DateTime.Now,
        });

        return Task.FromResult(result);
    }

    public void TriggerCapture()
    {
        _ = CaptureAsync();
    }
}
