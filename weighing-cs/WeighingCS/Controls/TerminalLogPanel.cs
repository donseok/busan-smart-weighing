using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Text;
using System.Windows.Forms;

namespace WeighingCS.Controls;

/// <summary>
/// Terminal-style log panel with macOS-style traffic-light header dots.
/// Wraps a <see cref="RichTextBox"/> for log content.
/// </summary>
public class TerminalLogPanel : Control
{
    public enum LogLevel { Info, Success, Warning, Error }

    private const int HeaderHeight = 32;
    private readonly RichTextBox _rtb;

    public TerminalLogPanel()
    {
        SetStyle(
            ControlStyles.AllPaintingInWmPaint |
            ControlStyles.UserPaint |
            ControlStyles.OptimizedDoubleBuffer |
            ControlStyles.ResizeRedraw,
            true);

        _rtb = new RichTextBox
        {
            ReadOnly = true,
            BorderStyle = BorderStyle.None,
            BackColor = Theme.BgElevated,
            ForeColor = Theme.Success,
            Font = Theme.FontMono,
            ScrollBars = RichTextBoxScrollBars.Vertical,
        };

        Controls.Add(_rtb);
        Size = new Size(400, 200);
    }

    /// <summary>
    /// Direct access to the underlying RichTextBox (for compatibility with existing code).
    /// </summary>
    public RichTextBox InnerTextBox => _rtb;

    protected override void OnLayout(LayoutEventArgs levent)
    {
        base.OnLayout(levent);
        _rtb.SetBounds(
            Theme.SpacingSm, HeaderHeight + Theme.SpacingXs,
            Width - Theme.SpacingSm * 2,
            Height - HeaderHeight - Theme.SpacingSm - Theme.SpacingXs);
    }

    protected override void OnPaintBackground(PaintEventArgs e) { }

    protected override void OnPaint(PaintEventArgs e)
    {
        var g = e.Graphics;
        g.SmoothingMode = SmoothingMode.AntiAlias;
        g.TextRenderingHint = TextRenderingHint.ClearTypeGridFit;

        var bounds = new Rectangle(0, 0, Width - 1, Height - 1);

        // Background
        using (var path = RoundedRectHelper.Create(bounds, Theme.RadiusLarge))
        using (var bgBrush = new SolidBrush(Theme.BgElevated))
        {
            g.FillPath(bgBrush, path);
        }

        // Border
        using (var path = RoundedRectHelper.Create(bounds, Theme.RadiusLarge))
        using (var pen = new Pen(Theme.Border, 1f))
        {
            g.DrawPath(pen, path);
        }

        // Header bar background
        var headerRect = new Rectangle(0, 0, Width - 1, HeaderHeight);
        using (var clipPath = RoundedRectHelper.Create(bounds, Theme.RadiusLarge))
        {
            g.SetClip(clipPath);
            using var headerBrush = new SolidBrush(Theme.BgSurface);
            g.FillRectangle(headerBrush, headerRect);
            g.ResetClip();
        }

        // Header separator line
        using (var linePen = new Pen(Theme.Border, 1f))
        {
            g.DrawLine(linePen, 0, HeaderHeight, Width, HeaderHeight);
        }

        // Traffic-light dots
        int dotSize = 10;
        int dotY = (HeaderHeight - dotSize) / 2;
        int dotX = Theme.SpacingMd;
        Color[] dotColors = { Color.FromArgb(255, 95, 87), Color.FromArgb(255, 189, 46), Color.FromArgb(39, 201, 63) };
        foreach (var dc in dotColors)
        {
            using var dotBrush = new SolidBrush(dc);
            g.FillEllipse(dotBrush, dotX, dotY, dotSize, dotSize);
            dotX += dotSize + 6;
        }

        // Title
        using var titleBrush = new SolidBrush(Theme.TextSecondary);
        g.DrawString("상태 로그", Theme.FontSmallBold, titleBrush, dotX + 8, (HeaderHeight - Theme.FontSmallBold.Height) / 2f);
    }

    /// <summary>
    /// Appends a log message with level-based coloring.
    /// </summary>
    public void AppendLog(string message, LogLevel level = LogLevel.Info)
    {
        if (_rtb.InvokeRequired)
        {
            _rtb.BeginInvoke(() => AppendLog(message, level));
            return;
        }

        string line = $"[{DateTime.Now:HH:mm:ss}] {message}";
        Color color = level switch
        {
            LogLevel.Success => Theme.Success,
            LogLevel.Warning => Theme.Warning,
            LogLevel.Error => Theme.Error,
            _ => Theme.TextSecondary,
        };

        _rtb.SelectionStart = _rtb.TextLength;
        _rtb.SelectionLength = 0;
        _rtb.SelectionColor = color;
        _rtb.AppendText(line + Environment.NewLine);

        // Auto-cleanup
        if (_rtb.TextLength > 50000)
        {
            _rtb.Clear();
            _rtb.SelectionColor = Theme.TextSecondary;
            _rtb.AppendText("[로그가 정리되었습니다]" + Environment.NewLine);
        }
    }

    /// <summary>
    /// Determines log level from message content (for backward compatibility).
    /// </summary>
    public static LogLevel DetectLevel(string message)
    {
        if (message.Contains("오류") || message.Contains("실패") || message.Contains("[오류]") || message.Contains("ERROR"))
            return LogLevel.Error;
        if (message.Contains("경고") || message.Contains("WARNING") || message.Contains("오프라인"))
            return LogLevel.Warning;
        if (message.Contains("완료") || message.Contains("연결됨") || message.Contains("성공"))
            return LogLevel.Success;
        return LogLevel.Info;
    }
}
