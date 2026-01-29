using System.Drawing;
using System.Drawing.Drawing2D;
using System.Windows.Forms;

namespace WeighingCS.Controls;

/// <summary>
/// Themed progress bar with rounded track and fill.
/// </summary>
public class ModernProgressBar : Control
{
    private int _value;
    private int _minimum;
    private int _maximum = 100;

    public ModernProgressBar()
    {
        SetStyle(
            ControlStyles.AllPaintingInWmPaint |
            ControlStyles.UserPaint |
            ControlStyles.OptimizedDoubleBuffer |
            ControlStyles.ResizeRedraw,
            true);

        Size = new Size(360, 6);
    }

    public int Value
    {
        get => _value;
        set { _value = Math.Clamp(value, _minimum, _maximum); Invalidate(); }
    }

    public int Minimum
    {
        get => _minimum;
        set { _minimum = value; if (_value < _minimum) _value = _minimum; Invalidate(); }
    }

    public int Maximum
    {
        get => _maximum;
        set { _maximum = value; if (_value > _maximum) _value = _maximum; Invalidate(); }
    }

    public Color BarColor { get; set; } = Theme.Primary;

    protected override void OnPaintBackground(PaintEventArgs e) { }

    protected override void OnPaint(PaintEventArgs e)
    {
        var g = e.Graphics;
        g.SmoothingMode = SmoothingMode.AntiAlias;

        // Clear
        using (var clearBrush = new SolidBrush(Parent?.BackColor ?? Theme.BgBase))
            g.FillRectangle(clearBrush, ClientRectangle);

        int radius = Height / 2;
        var trackRect = new Rectangle(0, 0, Width - 1, Height - 1);

        // Track background
        using (var trackPath = RoundedRectHelper.Create(trackRect, radius))
        using (var trackBrush = new SolidBrush(Theme.BgElevated))
        {
            g.FillPath(trackBrush, trackPath);
        }

        // Fill
        float range = _maximum - _minimum;
        if (range <= 0) return;
        float fraction = (_value - _minimum) / range;
        int fillWidth = (int)(trackRect.Width * fraction);
        if (fillWidth < Height) fillWidth = _value > _minimum ? Height : 0; // minimum pill width
        if (fillWidth <= 0) return;

        var fillRect = new Rectangle(0, 0, fillWidth, Height - 1);
        using (var fillPath = RoundedRectHelper.Create(fillRect, radius))
        using (var fillBrush = new LinearGradientBrush(fillRect, BarColor, Theme.Lighten(BarColor, 0.15f), LinearGradientMode.Horizontal))
        {
            g.FillPath(fillBrush, fillPath);
        }
    }
}
