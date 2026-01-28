using System.Net.Sockets;
using WeighingCS.Models;

namespace WeighingCS.Services;

/// <summary>
/// FUNC-013: Barrier gate control over TCP (or RS-485 via TCP converter).
/// Safety-first design: auto-open on weighing complete, manual override support (BR-013).
/// </summary>
public sealed class BarrierService : IDisposable
{
    private const int ConnectTimeoutMs = 5000;
    private const int MaxReconnectAttempts = 3;
    private const int ReconnectDelayMs = 2000;

    // Command bytes (representative; actual protocol depends on hardware vendor).
    private static readonly byte[] CmdOpen = { 0x02, 0x4F, 0x50, 0x03 };   // STX 'O' 'P' ETX
    private static readonly byte[] CmdClose = { 0x02, 0x43, 0x4C, 0x03 };  // STX 'C' 'L' ETX
    private static readonly byte[] CmdStatus = { 0x02, 0x53, 0x54, 0x03 }; // STX 'S' 'T' ETX

    private readonly DeviceConnectionConfig _config;
    private TcpClient? _client;
    private NetworkStream? _stream;
    private bool _disposed;

    // -- Public state ---------------------------------------------------------

    public bool IsConnected => _client?.Connected == true;
    public bool IsOpen { get; private set; }
    public bool ManualOverrideActive { get; private set; }

    // -- Events ---------------------------------------------------------------

    public event EventHandler<bool>? ConnectionStateChanged;
    public event EventHandler<bool>? BarrierStateChanged;
    public event EventHandler<string>? ErrorOccurred;

    // -- Constructor -----------------------------------------------------------

    public BarrierService(DeviceConnectionConfig config)
    {
        _config = config ?? throw new ArgumentNullException(nameof(config));
    }

    // -- Connect / Disconnect --------------------------------------------------

    public async Task ConnectAsync(CancellationToken cancellationToken = default)
    {
        if (_disposed) throw new ObjectDisposedException(nameof(BarrierService));

        int attempts = 0;
        while (attempts < MaxReconnectAttempts)
        {
            attempts++;
            try
            {
                Disconnect();

                _client = new TcpClient();
                using var cts = CancellationTokenSource.CreateLinkedTokenSource(cancellationToken);
                cts.CancelAfter(ConnectTimeoutMs);

                await _client.ConnectAsync(_config.Host, _config.Port, cts.Token);
                _stream = _client.GetStream();

                ConnectionStateChanged?.Invoke(this, true);
                return;
            }
            catch (Exception ex)
            {
                ErrorOccurred?.Invoke(this, $"Barrier connection attempt {attempts}/{MaxReconnectAttempts}: {ex.Message}");

                if (attempts < MaxReconnectAttempts)
                {
                    await Task.Delay(ReconnectDelayMs, cancellationToken);
                }
            }
        }

        ConnectionStateChanged?.Invoke(this, false);
    }

    public void Disconnect()
    {
        try
        {
            _stream?.Close();
            _stream?.Dispose();
            _stream = null;

            _client?.Close();
            _client?.Dispose();
            _client = null;
        }
        catch
        {
            // Best-effort cleanup.
        }
    }

    // -- Barrier commands ------------------------------------------------------

    /// <summary>
    /// Opens the barrier gate. Safety check: only opens if vehicle position is confirmed (BR-013).
    /// </summary>
    /// <param name="vehiclePositionConfirmed">Whether the vehicle sensor confirms correct positioning.</param>
    public async Task<bool> OpenAsync(bool vehiclePositionConfirmed = true)
    {
        if (!vehiclePositionConfirmed)
        {
            ErrorOccurred?.Invoke(this, "Cannot open barrier: vehicle position not confirmed (safety check).");
            return false;
        }

        if (!IsConnected)
        {
            ErrorOccurred?.Invoke(this, "Barrier not connected.");
            return false;
        }

        try
        {
            await SendCommandAsync(CmdOpen);
            IsOpen = true;
            BarrierStateChanged?.Invoke(this, true);
            return true;
        }
        catch (Exception ex)
        {
            ErrorOccurred?.Invoke(this, $"Barrier open failed: {ex.Message}");
            return false;
        }
    }

    /// <summary>
    /// Closes the barrier gate.
    /// </summary>
    public async Task<bool> CloseAsync()
    {
        if (!IsConnected)
        {
            ErrorOccurred?.Invoke(this, "Barrier not connected.");
            return false;
        }

        try
        {
            await SendCommandAsync(CmdClose);
            IsOpen = false;
            BarrierStateChanged?.Invoke(this, false);
            return true;
        }
        catch (Exception ex)
        {
            ErrorOccurred?.Invoke(this, $"Barrier close failed: {ex.Message}");
            return false;
        }
    }

    /// <summary>
    /// Enables manual override mode. The barrier will ignore automated commands
    /// until <see cref="DisableManualOverride"/> is called.
    /// </summary>
    public void EnableManualOverride()
    {
        ManualOverrideActive = true;
    }

    /// <summary>
    /// Disables manual override, returning to automated control.
    /// </summary>
    public void DisableManualOverride()
    {
        ManualOverrideActive = false;
    }

    /// <summary>
    /// Opens the barrier only if manual override is not active.
    /// Used by the automated weighing process.
    /// </summary>
    public async Task<bool> AutoOpenAsync(bool vehiclePositionConfirmed = true)
    {
        if (ManualOverrideActive)
        {
            ErrorOccurred?.Invoke(this, "Auto-open blocked: manual override is active.");
            return false;
        }

        return await OpenAsync(vehiclePositionConfirmed);
    }

    /// <summary>
    /// Queries the current barrier status from the hardware.
    /// </summary>
    public async Task<bool> QueryStatusAsync()
    {
        if (!IsConnected) return false;

        try
        {
            await SendCommandAsync(CmdStatus);
            // In a real implementation, we would read the response and parse the status.
            // For now, return the locally tracked state.
            return IsOpen;
        }
        catch (Exception ex)
        {
            ErrorOccurred?.Invoke(this, $"Barrier status query failed: {ex.Message}");
            return false;
        }
    }

    // -- Helpers ---------------------------------------------------------------

    private async Task SendCommandAsync(byte[] command)
    {
        if (_stream is null)
            throw new InvalidOperationException("Barrier stream is not available.");

        await _stream.WriteAsync(command);
        await _stream.FlushAsync();
    }

    // -- IDisposable -----------------------------------------------------------

    public void Dispose()
    {
        if (_disposed) return;
        _disposed = true;
        Disconnect();
    }
}
