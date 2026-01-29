using System.Drawing;
using System.Drawing.Drawing2D;
using System.Windows.Forms;

namespace WeighingCS.Controls;

/// <summary>
/// Circular LED indicator with glow effect.
/// </summary>
public class LedIndicator : Control
{
    private Color _ledColor = Theme.TextMuted; // gray = unknown
    private bool _isOn;

    public LedIndicator()
    {
        SetStyle(
            ControlStyles.AllPaintingInWmPaint |
            ControlStyles.UserPaint |
            ControlStyles.OptimizedDoubleBuffer |
            ControlStyles.ResizeRedraw,
            true);

        Size = new Size(16, 16);
    }

    public bool IsOn
    {
        get => _isOn;
        set { _isOn = value; Invalidate(); }
    }

    public Color OnColor { get; set; } = Theme.Success;
    public Color OffColor { get; set; } = Theme.Error;

    /// <summary>
    /// The current display color, derived from <see cref="IsOn"/> state.
    /// </summary>
    public Color LedColor => _isOn ? OnColor : (_ledColor == Theme.TextMuted ? OffColor : _ledColor);

    /// <summary>
    /// Directly sets the LED color (overrides on/off logic).
    /// </summary>
    public void SetColor(Color color) { _ledColor = color; Invalidate(); }

    protected override void OnPaintBackground(PaintEventArgs e) { }

    protected override void OnPaint(PaintEventArgs e)
    {
        var g = e.Graphics;
        g.SmoothingMode = SmoothingMode.AntiAlias;

        // Clear
        using (var clearBrush = new SolidBrush(Parent?.BackColor ?? Theme.BgSurface))
            g.FillRectangle(clearBrush, ClientRectangle);

        int size = Math.Min(Width, Height);
        int x = (Width - size) / 2;
        int y = (Height - size) / 2;
        var ledRect = new Rectangle(x + 2, y + 2, size - 4, size - 4);
        Color color = _isOn ? OnColor : OffColor;

        // Outer glow when on
        if (_isOn)
        {
            var glowRect = new Rectangle(x, y, size, size);
            using var glowPath = new GraphicsPath();
            glowPath.AddEllipse(glowRect);
            using var glowBrush = new PathGradientBrush(glowPath)
            {
                CenterColor = Theme.WithAlpha(color, 80),
                SurroundColors = new[] { Theme.WithAlpha(color, 0) }
            };
            g.FillEllipse(glowBrush, glowRect);
        }

        // Main circle
        using (var brush = new SolidBrush(color))
            g.FillEllipse(brush, ledRect);

        // Highlight (specular)
        var hlRect = new Rectangle(ledRect.X + 2, ledRect.Y + 1, ledRect.Width / 2, ledRect.Height / 2);
        using (var hlBrush = new LinearGradientBrush(hlRect,
            Theme.WithAlpha(Color.White, 90), Theme.WithAlpha(Color.White, 0),
            LinearGradientMode.Vertical))
        {
            g.FillEllipse(hlBrush, hlRect);
        }
    }
}
