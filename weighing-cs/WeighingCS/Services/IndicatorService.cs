using System.IO.Ports;
using WeighingCS.Models;

namespace WeighingCS.Services;

/// <summary>
/// FUNC-010: RS-232C serial communication service for the weighing indicator.
/// Reads weight values from the indicator, detects stability, and raises events.
/// </summary>
public sealed class IndicatorService : IDisposable
{
    private const int ReadIntervalMs = 200;
    private const int TimeoutWarningMs = 5000;
    private const int MaxReconnectAttempts = 3;
    private const int ReconnectDelayMs = 2000;

    private readonly ScaleConfig _config;
    private SerialPort? _serialPort;
    private CancellationTokenSource? _cts;
    private Task? _readTask;

    private readonly List<decimal> _recentWeights = new();
    private DateTime _lastDataReceived = DateTime.MinValue;
    private bool _timeoutWarningRaised;
    private bool _disposed;

    // -- Public state ----------------------------------------------------------

    public decimal CurrentWeight { get; private set; }
    public bool IsStable { get; private set; }
    public bool IsConnected => _serialPort?.IsOpen == true;

    // -- Events ---------------------------------------------------------------

    /// <summary>Raised every time a new weight value is parsed from the indicator.</summary>
    public event EventHandler<WeightEventArgs>? WeightReceived;

    /// <summary>Raised when N consecutive readings are within tolerance (BR-010).</summary>
    public event EventHandler<WeightEventArgs>? WeightStabilized;

    /// <summary>Raised on communication errors, timeouts, or negative weight.</summary>
    public event EventHandler<CommunicationErrorEventArgs>? CommunicationError;

    /// <summary>Raised when the connection state changes.</summary>
    public event EventHandler<bool>? ConnectionStateChanged;

    // -- Constructor -----------------------------------------------------------

    public IndicatorService(ScaleConfig config)
    {
        _config = config ?? throw new ArgumentNullException(nameof(config));
    }

    // -- Connect / Disconnect --------------------------------------------------

    public async Task ConnectAsync(CancellationToken cancellationToken = default)
    {
        if (_disposed) throw new ObjectDisposedException(nameof(IndicatorService));

        int attempts = 0;
        while (attempts < MaxReconnectAttempts)
        {
            attempts++;
            try
            {
                ClosePort();

                _serialPort = new SerialPort
                {
                    PortName = _config.ComPort,
                    BaudRate = _config.BaudRate,
                    DataBits = 8,
                    Parity = Parity.None,
                    StopBits = StopBits.One,
                    ReadTimeout = 1000,
                    WriteTimeout = 1000,
                    Encoding = System.Text.Encoding.ASCII
                };

                _serialPort.Open();
                _lastDataReceived = DateTime.Now;
                _timeoutWarningRaised = false;

                ConnectionStateChanged?.Invoke(this, true);

                _cts = new CancellationTokenSource();
                var linked = CancellationTokenSource.CreateLinkedTokenSource(_cts.Token, cancellationToken);
                _readTask = Task.Run(() => ReadLoopAsync(linked.Token), linked.Token);

                return; // success
            }
            catch (Exception ex)
            {
                CommunicationError?.Invoke(this, new CommunicationErrorEventArgs(
                    $"Connection attempt {attempts}/{MaxReconnectAttempts} failed: {ex.Message}",
                    ex));

                if (attempts < MaxReconnectAttempts)
                {
                    await Task.Delay(ReconnectDelayMs, cancellationToken);
                }
            }
        }

        ConnectionStateChanged?.Invoke(this, false);
        throw new InvalidOperationException(
            $"Failed to connect to indicator on {_config.ComPort} after {MaxReconnectAttempts} attempts.");
    }

    public async Task DisconnectAsync()
    {
        if (_cts is not null)
        {
            await _cts.CancelAsync();
            if (_readTask is not null)
            {
                try { await _readTask; }
                catch (OperationCanceledException) { /* expected */ }
            }
        }

        ClosePort();
        ConnectionStateChanged?.Invoke(this, false);
    }

    // -- Read loop -------------------------------------------------------------

    private async Task ReadLoopAsync(CancellationToken ct)
    {
        while (!ct.IsCancellationRequested)
        {
            try
            {
                // Check for timeout (BR-010: 5s no data warning)
                if ((DateTime.Now - _lastDataReceived).TotalMilliseconds > TimeoutWarningMs && !_timeoutWarningRaised)
                {
                    _timeoutWarningRaised = true;
                    CommunicationError?.Invoke(this, new CommunicationErrorEventArgs(
                        "No data received for 5 seconds. Check indicator connection.", null));
                }

                if (_serialPort is null || !_serialPort.IsOpen)
                {
                    await Task.Delay(ReadIntervalMs, ct);
                    continue;
                }

                string? line = null;
                try
                {
                    line = _serialPort.ReadLine();
                }
                catch (TimeoutException)
                {
                    // No data available this cycle, continue.
                }

                if (!string.IsNullOrWhiteSpace(line))
                {
                    _lastDataReceived = DateTime.Now;
                    _timeoutWarningRaised = false;

                    decimal weight = ParseWeight(line);

                    // BR-010: Negative weight is an error.
                    if (weight < 0)
                    {
                        CommunicationError?.Invoke(this, new CommunicationErrorEventArgs(
                            $"Negative weight detected: {weight} kg", null));
                        continue;
                    }

                    CurrentWeight = weight;
                    WeightReceived?.Invoke(this, new WeightEventArgs(weight, false));

                    CheckStability(weight);
                }

                await Task.Delay(ReadIntervalMs, ct);
            }
            catch (OperationCanceledException)
            {
                break;
            }
            catch (Exception ex)
            {
                CommunicationError?.Invoke(this, new CommunicationErrorEventArgs(
                    $"Read error: {ex.Message}", ex));
                await Task.Delay(ReadIntervalMs, ct);
            }
        }
    }

    // -- Stability check (BR-010) ----------------------------------------------

    private void CheckStability(decimal weight)
    {
        _recentWeights.Add(weight);

        if (_recentWeights.Count > _config.StabilityCount)
        {
            _recentWeights.RemoveAt(0);
        }

        if (_recentWeights.Count < _config.StabilityCount)
        {
            IsStable = false;
            return;
        }

        // All N consecutive values must be within tolerance of the first.
        decimal reference = _recentWeights[0];
        bool stable = true;
        foreach (decimal w in _recentWeights)
        {
            if (Math.Abs(w - reference) > _config.ToleranceKg)
            {
                stable = false;
                break;
            }
        }

        if (stable && !IsStable)
        {
            IsStable = true;
            WeightStabilized?.Invoke(this, new WeightEventArgs(weight, true));
        }
        else if (!stable)
        {
            IsStable = false;
        }
    }

    /// <summary>
    /// Resets the stability buffer so the next stable reading requires a fresh run.
    /// </summary>
    public void ResetStability()
    {
        _recentWeights.Clear();
        IsStable = false;
    }

    // -- Helpers ---------------------------------------------------------------

    /// <summary>
    /// Parses a weight value from a raw indicator line.
    /// Typical format variations: "  +  1234.5 kg", "1234.5", "ST,GS,+001234.5 kg"
    /// </summary>
    private static decimal ParseWeight(string raw)
    {
        // Strip common prefixes, status characters, and unit suffixes.
        string cleaned = raw
            .Replace("ST,", "")
            .Replace("US,", "")
            .Replace("GS,", "")
            .Replace("NT,", "")
            .Replace("kg", "", StringComparison.OrdinalIgnoreCase)
            .Replace("+", "")
            .Trim();

        if (decimal.TryParse(cleaned, System.Globalization.NumberStyles.Any,
                System.Globalization.CultureInfo.InvariantCulture, out decimal value))
        {
            return value;
        }

        return 0m;
    }

    private void ClosePort()
    {
        try
        {
            if (_serialPort?.IsOpen == true)
            {
                _serialPort.Close();
            }
            _serialPort?.Dispose();
            _serialPort = null;
        }
        catch
        {
            // Best-effort cleanup.
        }
    }

    // -- IDisposable -----------------------------------------------------------

    public void Dispose()
    {
        if (_disposed) return;
        _disposed = true;

        _cts?.Cancel();
        _cts?.Dispose();
        ClosePort();
    }
}

// -- Event arg classes --------------------------------------------------------

public class WeightEventArgs : EventArgs
{
    public decimal Weight { get; }
    public bool IsStable { get; }
    public DateTime Timestamp { get; }

    public WeightEventArgs(decimal weight, bool isStable)
    {
        Weight = weight;
        IsStable = isStable;
        Timestamp = DateTime.Now;
    }
}

public class CommunicationErrorEventArgs : EventArgs
{
    public string Message { get; }
    public Exception? Exception { get; }
    public DateTime Timestamp { get; }

    public CommunicationErrorEventArgs(string message, Exception? exception)
    {
        Message = message;
        Exception = exception;
        Timestamp = DateTime.Now;
    }
}
