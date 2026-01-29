using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Text;
using System.Windows.Forms;

namespace WeighingCS.Controls;

/// <summary>
/// Horizontal equipment connection status bar with LED indicators.
/// Shows 4 devices: Indicator (계량기), Display (전광판), Barrier (차단기), Network (네트워크).
/// </summary>
public class ConnectionStatusPanel : Control
{
    public enum DeviceType { Indicator, Display, Barrier, Network }

    private struct DeviceInfo
    {
        public string Icon;
        public string Label;
        public bool Connected;
        public string StatusText;
    }

    private readonly DeviceInfo[] _devices;

    public ConnectionStatusPanel()
    {
        SetStyle(
            ControlStyles.AllPaintingInWmPaint |
            ControlStyles.UserPaint |
            ControlStyles.OptimizedDoubleBuffer |
            ControlStyles.ResizeRedraw,
            true);

        Size = new Size(400, 72);

        _devices = new DeviceInfo[]
        {
            new() { Icon = "\u2696", Label = "계량기", Connected = false, StatusText = "끊김" },  // ⚖
            new() { Icon = "\u25A3", Label = "전광판", Connected = false, StatusText = "끊김" },  // ▣
            new() { Icon = "\u2503", Label = "차단기", Connected = false, StatusText = "끊김" },  // ┃
            new() { Icon = "\u25C9", Label = "네트워크", Connected = false, StatusText = "끊김" },// ◉
        };
    }

    /// <summary>
    /// Sets the connection status for a device.
    /// </summary>
    public void SetDeviceStatus(DeviceType device, bool connected)
    {
        int idx = (int)device;
        _devices[idx].Connected = connected;
        _devices[idx].StatusText = connected ? "연결됨" : "끊김";
        Invalidate();
    }

    /// <summary>
    /// Gets the connection status for a device.
    /// </summary>
    public bool GetDeviceStatus(DeviceType device) => _devices[(int)device].Connected;

    protected override void OnPaintBackground(PaintEventArgs e) { }

    protected override void OnPaint(PaintEventArgs e)
    {
        var g = e.Graphics;
        g.SmoothingMode = SmoothingMode.AntiAlias;
        g.TextRenderingHint = TextRenderingHint.ClearTypeGridFit;

        var bounds = new Rectangle(0, 0, Width - 1, Height - 1);

        // Background
        using (var path = RoundedRectHelper.Create(bounds, Theme.RadiusLarge))
        using (var bgBrush = new SolidBrush(Theme.BgSurface))
        {
            g.FillPath(bgBrush, path);
        }
        using (var path = RoundedRectHelper.Create(bounds, Theme.RadiusLarge))
        using (var pen = new Pen(Theme.Border, 1f))
        {
            g.DrawPath(pen, path);
        }

        // Title
        using (var titleBrush = new SolidBrush(Theme.TextSecondary))
        {
            g.DrawString("연결 상태", Theme.FontSmallBold, titleBrush, Theme.SpacingSm, 4);
        }

        // Device items (horizontal layout)
        int itemWidth = (Width - Theme.SpacingSm * 2) / _devices.Length;
        int yStart = 22;

        for (int i = 0; i < _devices.Length; i++)
        {
            ref var dev = ref _devices[i];
            int ix = Theme.SpacingSm + i * itemWidth;

            Color statusColor = dev.Connected ? Theme.Success : Theme.Error;

            // LED dot
            int ledSize = 8;
            int ledX = ix + 4;
            int ledY = yStart + 4;

            // Glow when connected
            if (dev.Connected)
            {
                using var glowBrush = new SolidBrush(Theme.WithAlpha(statusColor, 50));
                g.FillEllipse(glowBrush, ledX - 2, ledY - 2, ledSize + 4, ledSize + 4);
            }

            using (var ledBrush = new SolidBrush(statusColor))
                g.FillEllipse(ledBrush, ledX, ledY, ledSize, ledSize);

            // Label
            using (var labelBrush = new SolidBrush(Theme.TextPrimary))
                g.DrawString(dev.Label, Theme.FontSmall, labelBrush, ledX + ledSize + 4, yStart + 1);

            // Status text
            using (var statusBrush = new SolidBrush(statusColor))
                g.DrawString(dev.StatusText, Theme.FontSmall, statusBrush, ledX + ledSize + 4, yStart + 17);
        }
    }
}
