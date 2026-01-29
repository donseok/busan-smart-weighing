using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Text;
using System.Windows.Forms;

namespace WeighingCS.Controls;

/// <summary>
/// Large weight display with gradient background, glow effect, and stability badge.
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

        Size = new Size(400, 280);
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
        var g = e.Graphics;
        g.SmoothingMode = SmoothingMode.AntiAlias;
        g.TextRenderingHint = TextRenderingHint.ClearTypeGridFit;

        var bounds = new Rectangle(0, 0, Width - 1, Height - 1);

        // Gradient background
        using (var path = RoundedRectHelper.Create(bounds, Theme.RadiusLarge))
        {
            using var bgBrush = new LinearGradientBrush(bounds,
                Theme.BgElevated, Theme.BgSurface, LinearGradientMode.Vertical);
            g.FillPath(bgBrush, path);
        }

        // Border
        using (var path = RoundedRectHelper.Create(bounds, Theme.RadiusLarge))
        using (var borderPen = new Pen(Theme.Border, 1f))
        {
            g.DrawPath(borderPen, path);
        }

        // Left accent bar (cyan)
        using (var clipPath = RoundedRectHelper.Create(bounds, Theme.RadiusLarge))
        {
            g.SetClip(clipPath);
            using var accentBrush = new SolidBrush(Theme.Primary);
            g.FillRectangle(accentBrush, 0, 0, 3, Height);
            g.ResetClip();
        }

        // Title "중량 (kg)" top-left
        using (var titleBrush = new SolidBrush(Theme.TextSecondary))
        {
            g.DrawString("중량 (kg)", Theme.FontSmallBold, titleBrush, Theme.SpacingMd, Theme.SpacingSm);
        }

        // --- Stability badge (top-right) ---
        DrawStabilityBadge(g, bounds);

        // --- Main weight text ---
        float fontSize = CalculateFontSize();
        using var weightFont = new Font("Consolas", fontSize, FontStyle.Bold);
        var weightSize = g.MeasureString(_weightText, weightFont);

        float textX = (Width - weightSize.Width) / 2f;
        float textY = (Height - weightSize.Height) / 2f + 8; // offset down a bit from title

        // Glow effect when stable
        if (_stability == StabilityState.Stable)
        {
            using var glowBrush = new SolidBrush(Theme.WithAlpha(Theme.Primary, 40));
            for (int i = 3; i >= 1; i--)
            {
                g.DrawString(_weightText, weightFont, glowBrush, textX - i, textY);
                g.DrawString(_weightText, weightFont, glowBrush, textX + i, textY);
                g.DrawString(_weightText, weightFont, glowBrush, textX, textY - i);
                g.DrawString(_weightText, weightFont, glowBrush, textX, textY + i);
            }
        }

        // Main text
        Color textColor = _stability switch
        {
            StabilityState.Stable => Theme.Primary,
            StabilityState.Error => Theme.Error,
            _ => Theme.Primary,
        };
        using var textBrush = new SolidBrush(textColor);
        g.DrawString(_weightText, weightFont, textBrush, textX, textY);

        // Unit suffix
        using var unitFont = new Font("Segoe UI", 14F, FontStyle.Bold);
        var unitSize = g.MeasureString(_unit, unitFont);
        using var unitBrush = new SolidBrush(Theme.TextSecondary);
        g.DrawString(_unit, unitFont, unitBrush,
            textX + weightSize.Width + 4, textY + weightSize.Height - unitSize.Height - 4);
    }

    private void DrawStabilityBadge(Graphics g, Rectangle bounds)
    {
        string badgeText = _stability switch
        {
            StabilityState.Stable => "안정",
            StabilityState.Error => "오류",
            _ => "불안정",
        };
        Color badgeBg = _stability switch
        {
            StabilityState.Stable => Theme.Success,
            StabilityState.Error => Theme.Error,
            _ => Theme.Warning,
        };

        using var badgeFont = new Font("Segoe UI", 8F, FontStyle.Bold);
        var badgeSize = g.MeasureString(badgeText, badgeFont);
        float bw = badgeSize.Width + 16;
        float bh = badgeSize.Height + 6;
        float bx = bounds.Right - bw - Theme.SpacingSm;
        float by = Theme.SpacingSm;

        var badgeRect = new RectangleF(bx, by, bw, bh);
        using (var badgePath = RoundedRectHelper.Create(badgeRect, (int)(bh / 2)))
        using (var badgeBrush = new SolidBrush(Theme.WithAlpha(badgeBg, 40)))
        {
            g.FillPath(badgeBrush, badgePath);
        }
        using (var badgePath = RoundedRectHelper.Create(badgeRect, (int)(bh / 2)))
        using (var badgePen = new Pen(badgeBg, 1f))
        {
            g.DrawPath(badgePen, badgePath);
        }

        using var badgeTextBrush = new SolidBrush(badgeBg);
        g.DrawString(badgeText, badgeFont, badgeTextBrush, bx + 8, by + 3);
    }

    private float CalculateFontSize()
    {
        float scale = Math.Max(0.5f, Math.Min(1.2f, Width / 400f));
        return Math.Clamp(60f * scale, 36f, 80f);
    }
}
