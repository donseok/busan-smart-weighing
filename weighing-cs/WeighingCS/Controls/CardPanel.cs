using System.Drawing;
using System.Drawing.Drawing2D;
using System.Windows.Forms;

namespace WeighingCS.Controls;

/// <summary>
/// Card-style panel with rounded corners and optional left accent bar.
/// Replaces <see cref="GroupBox"/> throughout the application.
/// </summary>
public class CardPanel : Panel
{
    private string _title = string.Empty;
    private Color _accentColor = Color.Empty;
    private const int AccentWidth = 3;
    private const int TitleHeight = 32;

    public CardPanel()
    {
        SetStyle(
            ControlStyles.AllPaintingInWmPaint |
            ControlStyles.UserPaint |
            ControlStyles.OptimizedDoubleBuffer |
            ControlStyles.ResizeRedraw,
            true);

        BackColor = Theme.BgSurface;
        ForeColor = Theme.TextSecondary;
        Padding = new Padding(Theme.SpacingMd, TitleHeight + Theme.SpacingXs, Theme.SpacingMd, Theme.SpacingMd);
    }

    /// <summary>
    /// Title text displayed in the card header.
    /// </summary>
    public string Title
    {
        get => _title;
        set { _title = value; Invalidate(); }
    }

    /// <summary>
    /// Optional left accent bar color. <see cref="Color.Empty"/> disables the accent.
    /// </summary>
    public Color AccentColor
    {
        get => _accentColor;
        set { _accentColor = value; Invalidate(); }
    }

    public override Rectangle DisplayRectangle
    {
        get
        {
            var r = base.DisplayRectangle;
            int left = _accentColor.IsEmpty ? Theme.SpacingMd : Theme.SpacingMd + AccentWidth + Theme.SpacingXs;
            return new Rectangle(
                r.X + left,
                r.Y + TitleHeight + Theme.SpacingXs,
                r.Width - left - Theme.SpacingMd,
                r.Height - TitleHeight - Theme.SpacingXs - Theme.SpacingMd);
        }
    }

    protected override void OnPaintBackground(PaintEventArgs e) { /* prevent flicker */ }

    protected override void OnPaint(PaintEventArgs e)
    {
        var g = e.Graphics;
        g.SmoothingMode = SmoothingMode.AntiAlias;
        g.TextRenderingHint = System.Drawing.Text.TextRenderingHint.ClearTypeGridFit;

        var bounds = new Rectangle(0, 0, Width - 1, Height - 1);

        // Background fill with rounded corners
        using (var path = RoundedRectHelper.Create(bounds, Theme.RadiusLarge))
        using (var bgBrush = new SolidBrush(Theme.BgSurface))
        {
            g.FillPath(bgBrush, path);
        }

        // Border
        using (var path = RoundedRectHelper.Create(bounds, Theme.RadiusLarge))
        using (var borderPen = new Pen(Theme.Border, 1f))
        {
            g.DrawPath(borderPen, path);
        }

        // Left accent bar
        if (!_accentColor.IsEmpty)
        {
            var accentRect = new RectangleF(0, 0, AccentWidth, Height);
            using var accentPath = RoundedRectHelper.Create(
                new RectangleF(0, 0, AccentWidth + Theme.RadiusLarge, Height), Theme.RadiusLarge);
            using var clip = RoundedRectHelper.Create(bounds, Theme.RadiusLarge);
            g.SetClip(clip);
            using (var accentBrush = new SolidBrush(_accentColor))
            {
                g.FillRectangle(accentBrush, 0, 0, AccentWidth + 2, Height);
            }
            g.ResetClip();
        }

        // Title text
        if (!string.IsNullOrEmpty(_title))
        {
            int textX = _accentColor.IsEmpty ? Theme.SpacingMd : Theme.SpacingMd + AccentWidth + Theme.SpacingXs;
            using var titleBrush = new SolidBrush(Theme.TextSecondary);
            g.DrawString(_title, Theme.FontSmallBold, titleBrush, textX, Theme.SpacingSm);
        }
    }
}
