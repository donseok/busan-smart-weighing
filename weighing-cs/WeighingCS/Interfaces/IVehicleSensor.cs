namespace WeighingCS.Interfaces;

public interface IVehicleSensor
{
    event EventHandler<VehicleDetectedEventArgs>? VehicleDetected;
    Task StartListeningAsync();
    Task StopListeningAsync();
    bool IsListening { get; }
}

public class VehicleDetectedEventArgs : EventArgs
{
    public bool IsDetected { get; set; }
    public DateTime DetectedAt { get; set; } = DateTime.Now;
    public string SensorId { get; set; } = string.Empty;
}
