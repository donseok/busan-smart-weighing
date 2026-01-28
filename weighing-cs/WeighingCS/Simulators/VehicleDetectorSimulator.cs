using WeighingCS.Interfaces;

namespace WeighingCS.Simulators;

public class VehicleDetectorSimulator : IVehicleDetector
{
    public event EventHandler<VehiclePositionEventArgs>? PositionChanged;
    public bool IsConnected { get; private set; }
    public bool IsVehicleInPosition { get; private set; }

    public Task ConnectAsync()
    {
        IsConnected = true;
        return Task.CompletedTask;
    }

    public Task DisconnectAsync()
    {
        IsConnected = false;
        IsVehicleInPosition = false;
        return Task.CompletedTask;
    }

    public void TogglePosition()
    {
        IsVehicleInPosition = !IsVehicleInPosition;
        PositionChanged?.Invoke(this, new VehiclePositionEventArgs
        {
            IsInPosition = IsVehicleInPosition,
            Timestamp = DateTime.Now,
        });
    }

    public void SetInPosition(bool inPosition)
    {
        if (IsVehicleInPosition == inPosition) return;
        IsVehicleInPosition = inPosition;
        PositionChanged?.Invoke(this, new VehiclePositionEventArgs
        {
            IsInPosition = IsVehicleInPosition,
            Timestamp = DateTime.Now,
        });
    }
}
