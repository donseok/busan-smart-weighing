namespace WeighingCS.Interfaces;

public interface IVehicleDetector
{
    event EventHandler<VehiclePositionEventArgs>? PositionChanged;
    Task ConnectAsync();
    Task DisconnectAsync();
    bool IsConnected { get; }
    bool IsVehicleInPosition { get; }
}

public class VehiclePositionEventArgs : EventArgs
{
    public bool IsInPosition { get; set; }
    public DateTime Timestamp { get; set; } = DateTime.Now;
}
