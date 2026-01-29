using System.Drawing;
using System.Drawing.Drawing2D;
using System.Windows.Forms;

namespace WeighingCS.Controls;

/// <summary>
/// Rounded button with hover/press effects and three style variants.
/// </summary>
public class ModernButton : Control
{
    public enum ButtonVariant { Primary, Secondary, Danger }

    private ButtonVariant _variant = ButtonVariant.Secondary;
    private bool _hover;
    private bool _pressed;

    public ModernButton()
    {
        SetStyle(
            ControlStyles.AllPaintingInWmPaint |
            ControlStyles.UserPaint |
            ControlStyles.OptimizedDoubleBuffer |
            ControlStyles.ResizeRedraw |
            ControlStyles.StandardClick |
            ControlStyles.StandardDoubleClick,
            true);

        Size = new Size(120, 40);
        Cursor = Cursors.Hand;
        Font = Theme.FontBody;
    }

    public ButtonVariant Variant
    {
        get => _variant;
        set { _variant = value; Invalidate(); }
    }

    protected override void OnPaintBackground(PaintEventArgs e) { }

    protected override void OnPaint(PaintEventArgs e)
    {
        var g = e.Graphics;
        g.SmoothingMode = SmoothingMode.AntiAlias;
        g.TextRenderingHint = System.Drawing.Text.TextRenderingHint.ClearTypeGridFit;

        // Clear with parent background
        using (var clearBrush = new SolidBrush(Parent?.BackColor ?? Theme.BgBase))
            g.FillRectangle(clearBrush, ClientRectangle);

        var bounds = new Rectangle(0, 0, Width - 1, Height - 1);
        Color bgColor = GetBackgroundColor();
        Color fgColor = GetForegroundColor();

        if (!Enabled)
        {
            bgColor = Theme.WithAlpha(bgColor, 128);
            fgColor = Theme.WithAlpha(fgColor, 128);
        }
        else if (_pressed)
        {
            bgColor = Theme.Darken(bgColor, 0.15f);
        }
        else if (_hover)
        {
            bgColor = Theme.Lighten(bgColor, 0.10f);
        }

        // Fill
        using (var path = RoundedRectHelper.Create(bounds, Theme.RadiusMedium))
        using (var bgBrush = new SolidBrush(bgColor))
        {
            g.FillPath(bgBrush, path);
        }

        // Border for secondary variant
        if (_variant == ButtonVariant.Secondary)
        {
            using var path = RoundedRectHelper.Create(bounds, Theme.RadiusMedium);
            using var pen = new Pen(Theme.Border, 1f);
            g.DrawPath(pen, path);
        }

        // Text
        var textSize = g.MeasureString(Text, Font);
        float tx = (Width - textSize.Width) / 2f;
        float ty = (Height - textSize.Height) / 2f;
        using var textBrush = new SolidBrush(fgColor);
        g.DrawString(Text, Font, textBrush, tx, ty);
    }

    private Color GetBackgroundColor() => _variant switch
    {
        ButtonVariant.Primary => Theme.Primary,
        ButtonVariant.Danger => Theme.Error,
        _ => Theme.BgSurface,
    };

    private Color GetForegroundColor() => _variant switch
    {
        ButtonVariant.Primary => Color.White,
        ButtonVariant.Danger => Color.White,
        _ => Theme.TextPrimary,
    };

    protected override void OnMouseEnter(EventArgs e) { _hover = true; Invalidate(); base.OnMouseEnter(e); }
    protected override void OnMouseLeave(EventArgs e) { _hover = false; _pressed = false; Invalidate(); base.OnMouseLeave(e); }
    protected override void OnMouseDown(MouseEventArgs e) { if (e.Button == MouseButtons.Left) { _pressed = true; Invalidate(); } base.OnMouseDown(e); }
    protected override void OnMouseUp(MouseEventArgs e) { _pressed = false; Invalidate(); base.OnMouseUp(e); }
    protected override void OnEnabledChanged(EventArgs e) { _hover = false; _pressed = false; Invalidate(); base.OnEnabledChanged(e); }
}
