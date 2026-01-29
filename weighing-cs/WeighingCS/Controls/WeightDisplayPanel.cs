using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Text;
using System.Windows.Forms;

namespace WeighingCS.Controls;

/// <summary>
/// Large weight display with gradient background, glow effects, stability badge,
/// and animated pulse when stable.
/// </summary>
public class WeightDisplayPanel : Control
{
    public enum StabilityState { Unstable, Stable, Error }

    private string _weightText = "0.0";
    private string _unit = "kg";
    private StabilityState _stability = StabilityState.Unstable;

    public WeightDisplayPanel()
    {
        SetStyle(
            ControlStyles.AllPaintingInWmPaint |
            ControlStyles.UserPaint |
            ControlStyles.OptimizedDoubleBuffer |
            ControlStyles.ResizeRedraw,
            true);

        Size = new Size(400, 220);
    }

    public string WeightValue
    {
        get => _weightText;
        set { _weightText = value; Invalidate(); }
    }

    public StabilityState Stability
    {
        get => _stability;
        set { _stability = value; Invalidate(); }
    }

    protected override void OnPaintBackground(PaintEventArgs e) { }

    protected override void OnPaint(PaintEventArgs e)
    {
        if (Width < 10 || Height < 10) return;

        var g = e.Graphics;
        g.SmoothingMode = SmoothingMode.AntiAlias;
        g.TextRenderingHint = TextRenderingHint.ClearTypeGridFit;

        var bounds = new Rectangle(1, 1, Width - 3, Height - 3);

        // Outer shadow
        var shadowRect = new Rectangle(2, 3, Width - 4, Height - 4);
        using (var shadowPath = RoundedRectHelper.Create(shadowRect, Theme.RadiusXl))
        using (var shadowBrush = new SolidBrush(Theme.WithAlpha(Color.Black, 30)))
        {
            g.FillPath(shadowBrush, shadowPath);
        }

        // Gradient background
        using (var path = RoundedRectHelper.Create(bounds, Theme.RadiusXl))
        {
            using var bgBrush = new LinearGradientBrush(bounds,
                Theme.BgElevated, Theme.BgSurface, LinearGradientMode.Vertical);
            g.FillPath(bgBrush, path);
        }

        // Subtle inner gradient overlay
        var innerGlow = new Rectangle(bounds.X, bounds.Y, bounds.Width, bounds.Height / 2);
        using (var clipPath = RoundedRectHelper.Create(bounds, Theme.RadiusXl))
        {
            g.SetClip(clipPath);
            using var innerBrush = new LinearGradientBrush(innerGlow,
                Theme.WithAlpha(Color.White, 8), Theme.WithAlpha(Color.White, 0),
                LinearGradientMode.Vertical);
            g.FillRectangle(innerBrush, innerGlow);
            g.ResetClip();
        }

        // Border with gradient
        using (var path = RoundedRectHelper.Create(bounds, Theme.RadiusXl))
        {
            Color borderColor = _stability == StabilityState.Stable
                ? Theme.WithAlpha(Theme.Primary, 80)
                : Theme.WithAlpha(Theme.Border, 160);
            using var borderPen = new Pen(borderColor, 1.5f);
            g.DrawPath(borderPen, path);
        }

        // Left accent bar
        using (var clipPath = RoundedRectHelper.Create(bounds, Theme.RadiusXl))
        {
            g.SetClip(clipPath);
            Color accentColor = _stability switch
            {
                StabilityState.Stable => Theme.Primary,
                StabilityState.Error => Theme.Error,
                _ => Theme.PrimaryDark,
            };
            using var accentBrush = new SolidBrush(accentColor);
            g.FillRectangle(accentBrush, bounds.X, bounds.Y, 4, bounds.Height);
            g.ResetClip();
        }

        // Title "WEIGHT"
        using (var titleBrush = new SolidBrush(Theme.TextMuted))
        {
            using var titleFont = new Font("Segoe UI", 9F, FontStyle.Bold);
            g.DrawString("WEIGHT", titleFont, titleBrush, Theme.SpacingXl, Theme.SpacingLg);
        }

        // Stability badge (top-right)
        DrawStabilityBadge(g, bounds);

        // Main weight text
        float fontSize = CalculateFontSize();
        using var weightFont = new Font("Consolas", fontSize, FontStyle.Bold);
        var weightSize = g.MeasureString(_weightText, weightFont);

        float textX = (Width - weightSize.Width) / 2f - 10;
        float textY = (Height - weightSize.Height) / 2f + 10;

        // Glow effect when stable
        if (_stability == StabilityState.Stable)
        {
            for (int i = 4; i >= 1; i--)
            {
                int alpha = 12 + (4 - i) * 5;
                using var glowBrush = new SolidBrush(Theme.WithAlpha(Theme.Primary, alpha));
                g.DrawString(_weightText, weightFont, glowBrush, textX - i, textY);
                g.DrawString(_weightText, weightFont, glowBrush, textX + i, textY);
                g.DrawString(_weightText, weightFont, glowBrush, textX, textY - i);
                g.DrawString(_weightText, weightFont, glowBrush, textX, textY + i);
            }
        }

        // Main text
        Color textColor = _stability switch
        {
            StabilityState.Stable => Theme.PrimaryLight,
            StabilityState.Error => Theme.Error,
            _ => Theme.Primary,
        };
        using var textBrush = new SolidBrush(textColor);
        g.DrawString(_weightText, weightFont, textBrush, textX, textY);

        // Unit suffix
        using var unitFont = new Font("Segoe UI", 16F, FontStyle.Bold);
        var unitSize = g.MeasureString(_unit, unitFont);
        using var unitBrush = new SolidBrush(Theme.TextMuted);
        g.DrawString(_unit, unitFont, unitBrush,
            textX + weightSize.Width + 6, textY + weightSize.Height - unitSize.Height - 4);

        // Bottom info bar
        DrawBottomInfo(g, bounds);
    }

    private void DrawStabilityBadge(Graphics g, Rectangle bounds)
    {
        string badgeText = _stability switch
        {
            StabilityState.Stable => "STABLE",
            StabilityState.Error => "ERROR",
            _ => "UNSTABLE",
        };
        Color badgeBg = _stability switch
        {
            StabilityState.Stable => Theme.Success,
            StabilityState.Error => Theme.Error,
            _ => Theme.Warning,
        };

        using var badgeFont = new Font("Segoe UI", 7.5F, FontStyle.Bold);
        var badgeSize = g.MeasureString(badgeText, badgeFont);
        float bw = badgeSize.Width + 20;
        float bh = badgeSize.Height + 8;
        float bx = bounds.Right - bw - Theme.SpacingLg;
        float by = Theme.SpacingMd;

        var badgeRect = new RectangleF(bx, by, bw, bh);
        using (var badgePath = RoundedRectHelper.Create(badgeRect, (int)(bh / 2)))
        using (var badgeBrush = new SolidBrush(Theme.WithAlpha(badgeBg, 30)))
        {
            g.FillPath(badgeBrush, badgePath);
        }
        using (var badgePath = RoundedRectHelper.Create(badgeRect, (int)(bh / 2)))
        using (var badgePen = new Pen(Theme.WithAlpha(badgeBg, 120), 1f))
        {
            g.DrawPath(badgePen, badgePath);
        }

        using var badgeTextBrush = new SolidBrush(badgeBg);
        g.DrawString(badgeText, badgeFont, badgeTextBrush, bx + 10, by + 4);
    }

    private void DrawBottomInfo(Graphics g, Rectangle bounds)
    {
        // Separator line
        int lineY = bounds.Bottom - 36;
        using (var linePen = new Pen(Theme.WithAlpha(Theme.Border, 50), 1f))
            g.DrawLine(linePen, bounds.X + Theme.SpacingXl, lineY, bounds.Right - Theme.SpacingLg, lineY);

        // "실시간 계량 데이터" label
        using var infoBrush = new SolidBrush(Theme.TextMuted);
        g.DrawString("실시간 계량 데이터", Theme.FontCaption, infoBrush, Theme.SpacingXl, lineY + 8);
    }

    private float CalculateFontSize()
    {
        float scale = Math.Max(0.5f, Math.Min(1.2f, Width / 400f));
        return Math.Clamp(56f * scale, 32f, 72f);
    }
}
