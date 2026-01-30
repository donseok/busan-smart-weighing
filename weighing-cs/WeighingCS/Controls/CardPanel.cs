using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Text;
using System.Windows.Forms;

namespace WeighingCS.Controls;

/// <summary>
/// Card-style panel with rounded corners, subtle glass effect, and optional left accent bar.
/// </summary>
public class CardPanel : Panel
{
    private string _title = string.Empty;
    private Color _accentColor = Color.Empty;
    private static int AccentWidth => (int)(3 * Theme.LayoutScale);
    private static int TitleHeight => (int)(36 * Theme.LayoutScale);

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
        Padding = new Padding(Theme.SpacingLg, TitleHeight + Theme.SpacingSm, Theme.SpacingLg, Theme.SpacingLg);

        Theme.ThemeChanged += (_, _) => { BackColor = Theme.BgSurface; ForeColor = Theme.TextSecondary; Invalidate(); };
    }

    public string Title
    {
        get => _title;
        set { _title = value; Invalidate(); }
    }

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
            int left = _accentColor.IsEmpty ? Theme.SpacingLg : Theme.SpacingLg + AccentWidth + Theme.SpacingSm;
            return new Rectangle(
                r.X + left,
                r.Y + TitleHeight + Theme.SpacingSm,
                r.Width - left - Theme.SpacingLg,
                r.Height - TitleHeight - Theme.SpacingSm - Theme.SpacingLg);
        }
    }

    protected override void OnLayout(LayoutEventArgs levent)
    {
        base.OnLayout(levent);
        if (Width < 2 || Height < 2) return;
        var bounds = new Rectangle(0, 0, Width - 1, Height - 1);
        using var regionPath = RoundedRectHelper.Create(bounds, Theme.RadiusLarge);
        Region = new Region(regionPath);
    }

    protected override void OnPaintBackground(PaintEventArgs e) { }

    protected override void OnPaint(PaintEventArgs e)
    {
        if (Width < 10 || Height < 10) return;

        var g = e.Graphics;
        g.SmoothingMode = SmoothingMode.AntiAlias;
        g.TextRenderingHint = TextRenderingHint.ClearTypeGridFit;

        var bounds = new Rectangle(0, 0, Width - 1, Height - 1);

        // Subtle shadow (outer glow) with softer alpha
        var shadowRect = new Rectangle(2, 3, Width - 3, Height - 3);
        using (var shadowPath = RoundedRectHelper.Create(shadowRect, Theme.RadiusLarge))
        using (var shadowBrush = new SolidBrush(Theme.WithAlpha(Color.Black, 18)))
        {
            g.FillPath(shadowBrush, shadowPath);
        }

        // Background fill
        using (var path = RoundedRectHelper.Create(bounds, Theme.RadiusLarge))
        using (var bgBrush = new SolidBrush(Theme.BgSurface))
        {
            g.FillPath(bgBrush, path);
        }

        // Glass highlight on top half
        var glassRect = new RectangleF(bounds.X, bounds.Y, bounds.Width, bounds.Height * 0.4f);
        using (var clipPath = RoundedRectHelper.Create(bounds, Theme.RadiusLarge))
        {
            g.SetClip(clipPath);
            using var glassBrush = new LinearGradientBrush(glassRect,
                Theme.WithAlpha(Color.White, 6), Theme.WithAlpha(Color.White, 0),
                LinearGradientMode.Vertical);
            g.FillRectangle(glassBrush, glassRect);
            g.ResetClip();
        }

        // Left accent bar (drawn before border so border overlaps cleanly)
        if (!_accentColor.IsEmpty)
        {
            using var clip = RoundedRectHelper.Create(bounds, Theme.RadiusLarge);
            g.SetClip(clip);
            using var accentBrush = new SolidBrush(_accentColor);
            g.FillRectangle(accentBrush, bounds.X, bounds.Y, AccentWidth + 1, bounds.Height);
            g.ResetClip();
        }

        // Border
        using (var path = RoundedRectHelper.Create(bounds, Theme.RadiusLarge))
        using (var borderPen = new Pen(Theme.WithAlpha(Theme.Border, 160), 1f))
        {
            g.DrawPath(borderPen, path);
        }

        // Title text
        if (!string.IsNullOrEmpty(_title))
        {
            int textX = _accentColor.IsEmpty ? Theme.SpacingLg : Theme.SpacingLg + AccentWidth + Theme.SpacingSm;
            using var titleBrush = new SolidBrush(Theme.TextSecondary);
            g.DrawString(_title, Theme.FontSmallBold, titleBrush, textX, Theme.SpacingMd);
        }

        // Title bottom separator
        int sepY = TitleHeight;
        using (var sepPen = new Pen(Theme.WithAlpha(Theme.Border, 60), 1f))
        {
            int sepX = _accentColor.IsEmpty ? Theme.SpacingMd : Theme.SpacingMd + AccentWidth;
            g.DrawLine(sepPen, sepX, sepY, Width - Theme.SpacingMd, sepY);
        }
    }
}
