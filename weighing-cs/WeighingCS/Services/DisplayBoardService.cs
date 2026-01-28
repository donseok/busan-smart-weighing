using System.Net.Sockets;
using System.Text;
using WeighingCS.Models;

namespace WeighingCS.Services;

/// <summary>
/// FUNC-012: LED display board control over TCP (or RS-485 via TCP converter).
/// 3-row, 6-column large LED (BR-012). Sends formatted display commands.
/// </summary>
public sealed class DisplayBoardService : IDisposable
{
    private const int ConnectTimeoutMs = 5000;
    private const int MaxReconnectAttempts = 3;
    private const int ReconnectDelayMs = 2000;

    private readonly DeviceConnectionConfig _config;
    private TcpClient? _client;
    private NetworkStream? _stream;
    private bool _disposed;

    // -- Public state ---------------------------------------------------------

    public bool IsConnected => _client?.Connected == true;

    // -- Events ---------------------------------------------------------------

    public event EventHandler<bool>? ConnectionStateChanged;
    public event EventHandler<string>? ErrorOccurred;

    // -- Predefined messages (Korean - BR-012) ---------------------------------

    public static class Messages
    {
        public const string Waiting = "계량 대기";
        public const string Weighing = "계량 중";
        public const string Completed = "계량 완료";
        public const string MobileAuthRequired = "모바일 인증 필요";
        public const string UnregisteredVehicle = "미등록 차량";
        public const string SystemMaintenance = "시스템 점검 중";
    }

    // -- Display types --------------------------------------------------------

    public enum DisplayType
    {
        /// <summary>One-Time Password / primary large text.</summary>
        Otp,
        /// <summary>Status message row.</summary>
        Status,
        /// <summary>General message row.</summary>
        Message,
        /// <summary>Error message row.</summary>
        Error
    }

    // -- Constructor -----------------------------------------------------------

    public DisplayBoardService(DeviceConnectionConfig config)
    {
        _config = config ?? throw new ArgumentNullException(nameof(config));
    }

    // -- Connect / Disconnect --------------------------------------------------

    public async Task ConnectAsync(CancellationToken cancellationToken = default)
    {
        if (_disposed) throw new ObjectDisposedException(nameof(DisplayBoardService));

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
                ErrorOccurred?.Invoke(this, $"Display board connection attempt {attempts}/{MaxReconnectAttempts}: {ex.Message}");

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

    // -- Display commands ------------------------------------------------------

    /// <summary>
    /// Sends a display command to the LED board.
    /// </summary>
    /// <param name="displayType">Which row/style to target.</param>
    /// <param name="text">Text to display (max 6 columns, will be truncated).</param>
    public async Task DisplayAsync(DisplayType displayType, string text)
    {
        if (!IsConnected)
        {
            ErrorOccurred?.Invoke(this, "Display board not connected.");
            return;
        }

        try
        {
            byte[] command = BuildCommand(displayType, text);
            await _stream!.WriteAsync(command);
            await _stream.FlushAsync();
        }
        catch (Exception ex)
        {
            ErrorOccurred?.Invoke(this, $"Display send error: {ex.Message}");
            ConnectionStateChanged?.Invoke(this, false);
        }
    }

    /// <summary>
    /// Convenience method: show OTP (large font) on the display board.
    /// </summary>
    public Task DisplayOtpAsync(string otp) => DisplayAsync(DisplayType.Otp, otp);

    /// <summary>
    /// Convenience method: show a status message.
    /// </summary>
    public Task DisplayStatusAsync(string message) => DisplayAsync(DisplayType.Status, message);

    /// <summary>
    /// Convenience method: show a general message.
    /// </summary>
    public Task DisplayMessageAsync(string message) => DisplayAsync(DisplayType.Message, message);

    /// <summary>
    /// Convenience method: show an error message.
    /// </summary>
    public Task DisplayErrorAsync(string message) => DisplayAsync(DisplayType.Error, message);

    /// <summary>
    /// Shows the standard waiting display.
    /// </summary>
    public async Task ShowWaitingAsync()
    {
        await DisplayStatusAsync(Messages.Waiting);
    }

    /// <summary>
    /// Shows weighing-in-progress display with the plate number.
    /// </summary>
    public async Task ShowWeighingAsync(string plateNumber)
    {
        await DisplayOtpAsync(plateNumber);
        await DisplayStatusAsync(Messages.Weighing);
    }

    /// <summary>
    /// Shows weighing-complete display with weight.
    /// </summary>
    public async Task ShowCompletedAsync(decimal weight)
    {
        await DisplayOtpAsync($"{weight:F1}kg");
        await DisplayStatusAsync(Messages.Completed);
    }

    /// <summary>
    /// Clears all rows of the display.
    /// </summary>
    public async Task ClearAsync()
    {
        await DisplayAsync(DisplayType.Otp, "");
        await DisplayAsync(DisplayType.Status, "");
        await DisplayAsync(DisplayType.Message, "");
    }

    // -- Protocol helpers ------------------------------------------------------

    /// <summary>
    /// Builds a raw command frame for the display board protocol.
    /// Protocol: STX + ROW(1) + LEN(2) + DATA(n) + ETX + BCC
    /// This is a representative implementation; actual protocol depends on hardware vendor.
    /// </summary>
    private static byte[] BuildCommand(DisplayType displayType, string text)
    {
        const byte STX = 0x02;
        const byte ETX = 0x03;

        byte row = displayType switch
        {
            DisplayType.Otp => 0x01,
            DisplayType.Status => 0x02,
            DisplayType.Message => 0x03,
            DisplayType.Error => 0x03, // error shares message row
            _ => 0x01
        };

        // Encode text as Korean-capable encoding (EUC-KR fallback to UTF8).
        byte[] textBytes;
        try
        {
            Encoding.RegisterProvider(CodePagesEncodingProvider.Instance);
            textBytes = Encoding.GetEncoding("EUC-KR").GetBytes(text);
        }
        catch
        {
            textBytes = Encoding.UTF8.GetBytes(text);
        }

        // Truncate to max display width (6 columns x ~2 bytes per Korean char = 12 bytes).
        if (textBytes.Length > 12)
        {
            textBytes = textBytes[..12];
        }

        byte len = (byte)textBytes.Length;

        // Build frame: STX + ROW + LEN + DATA + ETX
        var frame = new byte[4 + textBytes.Length];
        frame[0] = STX;
        frame[1] = row;
        frame[2] = len;
        Array.Copy(textBytes, 0, frame, 3, textBytes.Length);
        frame[^1] = ETX;

        // Append BCC (XOR of everything between STX and ETX exclusive).
        byte bcc = 0;
        for (int i = 1; i < frame.Length - 1; i++)
        {
            bcc ^= frame[i];
        }

        var result = new byte[frame.Length + 1];
        Array.Copy(frame, result, frame.Length);
        result[^1] = bcc;

        return result;
    }

    // -- IDisposable -----------------------------------------------------------

    public void Dispose()
    {
        if (_disposed) return;
        _disposed = true;
        Disconnect();
    }
}
