using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Text;
using System.Windows.Forms;

namespace WeighingCS.Controls;

/// <summary>
/// Bottom status bar showing scale info, mode, sync status, and time.
/// </summary>
public class StatusFooter : Control
{
    private string _scaleInfo = "계량대 #1";
    private string _modeText = "자동 모드";
    private string _syncInfo = "";
    private string _timeText = "";
    private readonly System.Windows.Forms.Timer _timer;

    public StatusFooter()
    {
        SetStyle(
            ControlStyles.AllPaintingInWmPaint |
            ControlStyles.UserPaint |
            ControlStyles.OptimizedDoubleBuffer |
            ControlStyles.ResizeRedraw,
            true);

        Size = new Size(1200, Theme.FooterHeight);
        Dock = DockStyle.Bottom;

        _timer = new System.Windows.Forms.Timer { Interval = 1000 };
        _timer.Tick += (_, _) => { _timeText = DateTime.Now.ToString("HH:mm:ss"); Invalidate(); };
        _timer.Start();
        _timeText = DateTime.Now.ToString("HH:mm:ss");

        Theme.ThemeChanged += (_, _) => { Size = new Size(Width, Theme.FooterHeight); Invalidate(); };
    }

    public string ScaleInfo { get => _scaleInfo; set { _scaleInfo = value; Invalidate(); } }
    public string ModeText { get => _modeText; set { _modeText = value; Invalidate(); } }
    public string SyncInfo { get => _syncInfo; set { _syncInfo = value; Invalidate(); } }

    protected override void OnPaintBackground(PaintEventArgs e) { }

    protected override void OnPaint(PaintEventArgs e)
    {
        var g = e.Graphics;
        g.SmoothingMode = SmoothingMode.AntiAlias;
        g.TextRenderingHint = TextRenderingHint.ClearTypeGridFit;

        // Background
        using (var bgBrush = new SolidBrush(Theme.BgDarkest))
            g.FillRectangle(bgBrush, ClientRectangle);

        // Top border
        using (var borderPen = new Pen(Theme.Border, 1f))
            g.DrawLine(borderPen, 0, 0, Width, 0);

        int y = (Height - Theme.FontSmall.Height) / 2;
        int x = Theme.SpacingLg;

        // Scale info
        using (var brush = new SolidBrush(Theme.TextSecondary))
            g.DrawString(_scaleInfo, Theme.FontSmall, brush, x, y);

        x += (int)g.MeasureString(_scaleInfo, Theme.FontSmall).Width + Theme.SpacingMd;
        DrawSeparator(g, ref x, y);

        // Mode indicator with colored dot
        Color modeDotColor = _modeText.Contains("자동") ? Theme.Primary : Theme.Purple;
        int dotSize = (int)(6 * Theme.LayoutScale);
        int dotY = y + (Theme.FontSmall.Height - dotSize) / 2;
        using (var dotBrush = new SolidBrush(modeDotColor))
            g.FillEllipse(dotBrush, x, dotY, dotSize, dotSize);
        x += dotSize + 5;

        using (var brush = new SolidBrush(Theme.TextSecondary))
            g.DrawString(_modeText, Theme.FontSmall, brush, x, y);

        x += (int)g.MeasureString(_modeText, Theme.FontSmall).Width + Theme.SpacingMd;

        // Sync info
        if (!string.IsNullOrEmpty(_syncInfo))
        {
            DrawSeparator(g, ref x, y);
            using var brush = new SolidBrush(Theme.TextMuted);
            g.DrawString(_syncInfo, Theme.FontSmall, brush, x, y);
        }

        // Right side: version + time
        float rightX = Width - Theme.SpacingLg;

        var timeSize = g.MeasureString(_timeText, Theme.FontMonoSmall);
        rightX -= timeSize.Width;
        using (var timeBrush = new SolidBrush(Theme.TextSecondary))
            g.DrawString(_timeText, Theme.FontMonoSmall, timeBrush, rightX, y);

        rightX -= Theme.SpacingLg;

        string version = "v1.0.0";
        var versionSize = g.MeasureString(version, Theme.FontCaption);
        rightX -= versionSize.Width;
        using (var vBrush = new SolidBrush(Theme.TextMuted))
            g.DrawString(version, Theme.FontCaption, vBrush, rightX, y + 1);
    }

    private void DrawSeparator(Graphics g, ref int x, int y)
    {
        using var pen = new Pen(Theme.Border, 1f);
        g.DrawLine(pen, x, y - 1, x, y + Theme.FontSmall.Height + 1);
        x += Theme.SpacingMd;
    }

    protected override void Dispose(bool disposing)
    {
        if (disposing) { _timer.Stop(); _timer.Dispose(); }
        base.Dispose(disposing);
    }
}
