using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Text;
using System.Windows.Forms;

namespace WeighingCS.Controls;

/// <summary>
/// Sliding toggle switch with labels: "자동 (LPR)" / "수동".
/// Replaces radio buttons for mode selection.
/// </summary>
public class ModernToggle : Control
{
    private bool _isAutoMode = true;
    private float _thumbPosition; // 0.0 = left (auto), 1.0 = right (manual)
    private readonly System.Windows.Forms.Timer _animTimer;

    public event EventHandler? ModeChanged;

    public ModernToggle()
    {
        SetStyle(
            ControlStyles.AllPaintingInWmPaint |
            ControlStyles.UserPaint |
            ControlStyles.OptimizedDoubleBuffer |
            ControlStyles.ResizeRedraw |
            ControlStyles.StandardClick,
            true);

        Size = new Size(260, 40);
        Cursor = Cursors.Hand;

        _animTimer = new System.Windows.Forms.Timer { Interval = 16 }; // ~60fps
        _animTimer.Tick += OnAnimTick;
    }

    /// <summary>
    /// True = auto mode (LPR), false = manual mode.
    /// </summary>
    public bool IsAutoMode
    {
        get => _isAutoMode;
        set
        {
            if (_isAutoMode == value) return;
            _isAutoMode = value;
            _animTimer.Start();
            ModeChanged?.Invoke(this, EventArgs.Empty);
        }
    }

    private void OnAnimTick(object? sender, EventArgs e)
    {
        float target = _isAutoMode ? 0f : 1f;
        float diff = target - _thumbPosition;

        if (Math.Abs(diff) < 0.05f)
        {
            _thumbPosition = target;
            _animTimer.Stop();
        }
        else
        {
            _thumbPosition += diff * 0.25f; // ease-out
        }
        Invalidate();
    }

    protected override void OnMouseClick(MouseEventArgs e)
    {
        base.OnMouseClick(e);
        IsAutoMode = !_isAutoMode;
    }

    protected override void OnPaintBackground(PaintEventArgs e) { }

    protected override void OnPaint(PaintEventArgs e)
    {
        var g = e.Graphics;
        g.SmoothingMode = SmoothingMode.AntiAlias;
        g.TextRenderingHint = TextRenderingHint.ClearTypeGridFit;

        // Clear
        using (var clearBrush = new SolidBrush(Parent?.BackColor ?? Theme.BgBase))
            g.FillRectangle(clearBrush, ClientRectangle);

        // Background rounded rect
        var bounds = new Rectangle(0, 0, Width - 1, Height - 1);
        using (var path = RoundedRectHelper.Create(bounds, Theme.RadiusMedium))
        using (var bgBrush = new SolidBrush(Theme.BgSurface))
        {
            g.FillPath(bgBrush, path);
        }
        using (var path = RoundedRectHelper.Create(bounds, Theme.RadiusMedium))
        using (var pen = new Pen(Theme.Border, 1f))
        {
            g.DrawPath(pen, path);
        }

        // Track area
        int trackMargin = 4;
        int halfWidth = (Width - trackMargin * 3) / 2;
        int trackHeight = Height - trackMargin * 2;
        int trackRadius = Theme.RadiusSmall;

        // Sliding thumb
        float thumbX = trackMargin + _thumbPosition * (halfWidth + trackMargin);
        var thumbRect = new RectangleF(thumbX, trackMargin, halfWidth, trackHeight);
        Color thumbColor = _isAutoMode ? Theme.Primary : Theme.Purple;

        // Interpolate color during animation
        if (_animTimer.Enabled)
        {
            int r = (int)(Theme.Primary.R + (Theme.Purple.R - Theme.Primary.R) * _thumbPosition);
            int gv = (int)(Theme.Primary.G + (Theme.Purple.G - Theme.Primary.G) * _thumbPosition);
            int b = (int)(Theme.Primary.B + (Theme.Purple.B - Theme.Primary.B) * _thumbPosition);
            thumbColor = Color.FromArgb(r, gv, b);
        }

        using (var thumbPath = RoundedRectHelper.Create(thumbRect, trackRadius))
        using (var thumbBrush = new SolidBrush(Theme.WithAlpha(thumbColor, 50)))
        {
            g.FillPath(thumbBrush, thumbPath);
        }
        using (var thumbPath = RoundedRectHelper.Create(thumbRect, trackRadius))
        using (var thumbPen = new Pen(thumbColor, 1.5f))
        {
            g.DrawPath(thumbPen, thumbPath);
        }

        // Labels
        string leftText = "자동 (LPR)";
        string rightText = "수동";
        using var leftFont = _isAutoMode ? Theme.FontBodyBold : Theme.FontBody;
        using var rightFont = !_isAutoMode ? Theme.FontBodyBold : Theme.FontBody;

        Color leftColor = _isAutoMode ? Theme.TextPrimary : Theme.TextMuted;
        Color rightColor = !_isAutoMode ? Theme.TextPrimary : Theme.TextMuted;

        var leftSize = g.MeasureString(leftText, leftFont);
        var rightSize = g.MeasureString(rightText, rightFont);

        float leftX = trackMargin + (halfWidth - leftSize.Width) / 2f;
        float rightX = trackMargin * 2 + halfWidth + (halfWidth - rightSize.Width) / 2f;
        float textY = (Height - leftSize.Height) / 2f;

        using var leftBrush = new SolidBrush(leftColor);
        using var rightBrush = new SolidBrush(rightColor);
        g.DrawString(leftText, leftFont, leftBrush, leftX, textY);
        g.DrawString(rightText, rightFont, rightBrush, rightX, textY);
    }

    protected override void Dispose(bool disposing)
    {
        if (disposing)
        {
            _animTimer.Stop();
            _animTimer.Dispose();
        }
        base.Dispose(disposing);
    }
}
