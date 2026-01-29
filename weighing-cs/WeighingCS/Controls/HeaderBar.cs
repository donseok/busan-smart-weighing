using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Text;
using System.Windows.Forms;

namespace WeighingCS.Controls;

/// <summary>
/// Application header bar with logo, title, connection status indicators, and clock.
/// </summary>
public class HeaderBar : Control
{
    public enum DeviceType { Indicator, Display, Barrier, Network }

    private readonly struct DeviceStatus
    {
        public string Label { get; init; }
        public bool Connected { get; init; }
    }

    private DeviceStatus[] _devices;
    private readonly System.Windows.Forms.Timer _clockTimer;
    private string _clockText = "";

    public HeaderBar()
    {
        SetStyle(
            ControlStyles.AllPaintingInWmPaint |
            ControlStyles.UserPaint |
            ControlStyles.OptimizedDoubleBuffer |
            ControlStyles.ResizeRedraw,
            true);

        Size = new Size(1200, Theme.HeaderHeight);
        Dock = DockStyle.Top;

        _devices = new DeviceStatus[]
        {
            new() { Label = "계량기", Connected = false },
            new() { Label = "전광판", Connected = false },
            new() { Label = "차단기", Connected = false },
            new() { Label = "네트워크", Connected = false },
        };

        _clockTimer = new System.Windows.Forms.Timer { Interval = 1000 };
        _clockTimer.Tick += (_, _) => { _clockText = DateTime.Now.ToString("HH:mm"); Invalidate(); };
        _clockTimer.Start();
        _clockText = DateTime.Now.ToString("HH:mm");
    }

    public void SetDeviceStatus(DeviceType device, bool connected)
    {
        int idx = (int)device;
        _devices[idx] = new DeviceStatus { Label = _devices[idx].Label, Connected = connected };
        Invalidate();
    }

    public bool GetDeviceStatus(DeviceType device) => _devices[(int)device].Connected;

    protected override void OnPaintBackground(PaintEventArgs e) { }

    protected override void OnPaint(PaintEventArgs e)
    {
        if (Width < 10 || Height < 10) return;

        var g = e.Graphics;
        g.SmoothingMode = SmoothingMode.AntiAlias;
        g.TextRenderingHint = TextRenderingHint.ClearTypeGridFit;

        // Background
        using (var bgBrush = new SolidBrush(Theme.BgDarkest))
            g.FillRectangle(bgBrush, ClientRectangle);

        // Bottom border: subtle gradient line
        using (var borderBrush = new LinearGradientBrush(
            new Point(0, Height - 1), new Point(Width, Height - 1),
            Theme.WithAlpha(Theme.Primary, 60), Theme.WithAlpha(Theme.Primary, 10)))
        {
            using var pen = new Pen(borderBrush, 1f);
            g.DrawLine(pen, 0, Height - 1, Width, Height - 1);
        }

        // ── Left: Logo + Title ──────────────────────────────────────
        int x = Theme.SpacingLg;
        int centerY = Height / 2;

        // Logo circle
        int logoSize = 34;
        int logoY = centerY - logoSize / 2;
        var logoRect = new Rectangle(x, logoY, logoSize, logoSize);

        // Logo gradient background
        using (var logoPath = new GraphicsPath())
        {
            logoPath.AddEllipse(logoRect);
            using var logoBrush = new LinearGradientBrush(logoRect,
                Theme.Primary, Theme.PrimaryDark, LinearGradientMode.ForwardDiagonal);
            g.FillPath(logoBrush, logoPath);
        }

        // Logo text "DK"
        using (var logoFont = new Font("Segoe UI", 11F, FontStyle.Bold))
        {
            var logoTextSize = g.MeasureString("DK", logoFont);
            float ltx = x + (logoSize - logoTextSize.Width) / 2f;
            float lty = logoY + (logoSize - logoTextSize.Height) / 2f;
            using var logoTextBrush = new SolidBrush(Color.White);
            g.DrawString("DK", logoFont, logoTextBrush, ltx, lty);
        }

        x += logoSize + Theme.SpacingMd;

        // Title
        using (var titleBrush = new SolidBrush(Theme.TextPrimary))
            g.DrawString("부산 스마트 계량 시스템", Theme.FontHeading, titleBrush, x, centerY - 18);

        // Subtitle
        using (var subBrush = new SolidBrush(Theme.TextMuted))
            g.DrawString("BUSAN SMART WEIGHING", Theme.FontCaption, subBrush, x, centerY + 4);

        // ── Right: Connection indicators + Clock ────────────────────
        float rightX = Width - Theme.SpacingLg;

        // Clock
        using (var clockFont = new Font("Consolas", 13F, FontStyle.Bold))
        {
            var clockSize = g.MeasureString(_clockText, clockFont);
            rightX -= clockSize.Width;
            using var clockBrush = new SolidBrush(Theme.TextPrimary);
            g.DrawString(_clockText, clockFont, clockBrush, rightX, centerY - clockSize.Height / 2f);
        }

        // Separator
        rightX -= Theme.SpacingXl;
        using (var sepPen = new Pen(Theme.Border, 1f))
            g.DrawLine(sepPen, rightX, 14, rightX, Height - 14);

        rightX -= Theme.SpacingLg;

        // Connection indicators (right to left)
        for (int i = _devices.Length - 1; i >= 0; i--)
        {
            ref readonly var dev = ref _devices[i];
            Color dotColor = dev.Connected ? Theme.Success : Theme.WithAlpha(Theme.Error, 150);

            // Label
            var labelSize = g.MeasureString(dev.Label, Theme.FontCaption);
            rightX -= labelSize.Width;
            using (var labelBrush = new SolidBrush(Theme.TextMuted))
                g.DrawString(dev.Label, Theme.FontCaption, labelBrush, rightX, centerY - labelSize.Height / 2f);

            // Dot
            int dotSize = 7;
            rightX -= dotSize + 5;
            int dotY = centerY - dotSize / 2;

            // Glow when connected
            if (dev.Connected)
            {
                using var glowBrush = new SolidBrush(Theme.WithAlpha(dotColor, 40));
                g.FillEllipse(glowBrush, rightX - 3, dotY - 3, dotSize + 6, dotSize + 6);
            }

            using (var dotBrush = new SolidBrush(dotColor))
                g.FillEllipse(dotBrush, rightX, dotY, dotSize, dotSize);

            rightX -= Theme.SpacingMd;
        }
    }

    protected override void Dispose(bool disposing)
    {
        if (disposing)
        {
            _clockTimer.Stop();
            _clockTimer.Dispose();
        }
        base.Dispose(disposing);
    }
}
