using WeighingCS.Interfaces;

namespace WeighingCS.Simulators;

public class VehicleSensorSimulator : IVehicleSensor
{
    private CancellationTokenSource? _cts;
    private readonly Random _random = new();

    public event EventHandler<VehicleDetectedEventArgs>? VehicleDetected;
    public bool IsListening { get; private set; }

    public int DetectionIntervalMs { get; set; } = 5000;
    public double DetectionProbability { get; set; } = 0.3;

    public Task StartListeningAsync()
    {
        if (IsListening) return Task.CompletedTask;
        IsListening = true;
        _cts = new CancellationTokenSource();
        _ = RunDetectionLoopAsync(_cts.Token);
        return Task.CompletedTask;
    }

    public Task StopListeningAsync()
    {
        IsListening = false;
        _cts?.Cancel();
        _cts?.Dispose();
        _cts = null;
        return Task.CompletedTask;
    }

    public void TriggerDetection()
    {
        VehicleDetected?.Invoke(this, new VehicleDetectedEventArgs
        {
            IsDetected = true,
            DetectedAt = DateTime.Now,
            SensorId = "SIM-SENSOR-01",
        });
    }

    private async Task RunDetectionLoopAsync(CancellationToken ct)
    {
        while (!ct.IsCancellationRequested)
        {
            await Task.Delay(DetectionIntervalMs, ct).ConfigureAwait(false);
            if (_random.NextDouble() < DetectionProbability)
            {
                VehicleDetected?.Invoke(this, new VehicleDetectedEventArgs
                {
                    IsDetected = true,
                    DetectedAt = DateTime.Now,
                    SensorId = "SIM-SENSOR-01",
                });
            }
        }
    }
}
