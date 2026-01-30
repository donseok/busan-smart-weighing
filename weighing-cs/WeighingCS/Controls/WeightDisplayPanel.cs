using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Text;
using System.Windows.Forms;

namespace WeighingCS.Controls;

/// <summary>
/// Multi-weight display panel matching the reference layout:
/// Header (company/factory) | Current weight (large golden) |
/// 2x2 grid: 1st/2nd weight (blue), Net/Theoretical (teal) |
/// Notification bar (green).
/// </summary>
public class WeightDisplayPanel : Control
{
    public enum StabilityState { Unstable, Stable, Error }

    // ── Backing fields ──────────────────────────────────────────────────
    private decimal _currentWeight;
    private decimal _firstWeight;
    private decimal _secondWeight;
    private decimal _netWeight;
    private decimal _theoreticalWeight;
    private string _notificationText = "";
    private StabilityState _stability = StabilityState.Unstable;
    private string _unit = "Kg";

    // ── Font cache ──────────────────────────────────────────────────────
    private Font? _cachedLargeFont;
    private float _cachedLargeFontSize;
    private Font? _cachedGridFont;
    private float _cachedGridFontSize;

    // ── Color palette (from reference image) ────────────────────────────
    private static readonly Color HeaderBg = Color.FromArgb(26, 35, 50);       // #1A2332
    private static readonly Color CurrentWeightBg = Color.FromArgb(15, 25, 35); // #0F1923
    private static readonly Color CurrentWeightNum = Color.FromArgb(255, 215, 0); // #FFD700 golden
    private static readonly Color CurrentWeightLabel = Color.FromArgb(255, 51, 51); // #FF3333 red label
    private static readonly Color GridBlueBg = Color.FromArgb(27, 42, 74);     // #1B2A4A
    private static readonly Color GridBlueLabel = Color.FromArgb(100, 160, 255); // light blue label
    private static readonly Color GridTealBg = Color.FromArgb(26, 58, 58);     // #1A3A3A
    private static readonly Color GridTealLabel = Color.FromArgb(80, 200, 180); // light teal label
    private static readonly Color NotifBg = Color.FromArgb(40, 167, 69);       // #28A745
    private static readonly Color NotifText = Color.White;
    private static readonly Color GridNumColor = Color.White;
    private static readonly Color HeaderText = Color.White;
    private static readonly Color UnitColor = Color.FromArgb(180, 180, 180);   // muted unit

    public WeightDisplayPanel()
    {
        SetStyle(
            ControlStyles.AllPaintingInWmPaint |
            ControlStyles.UserPaint |
            ControlStyles.OptimizedDoubleBuffer |
            ControlStyles.ResizeRedraw,
            true);

        Size = new Size(400, 480);
        Theme.ThemeChanged += (_, _) => Invalidate();
    }

    // ── Public properties ───────────────────────────────────────────────

    /// <summary>Real-time weight from indicator (현재중량)</summary>
    public decimal CurrentWeight
    {
        get => _currentWeight;
        set { _currentWeight = value; Invalidate(); }
    }

    /// <summary>First weighing / tare weight (1차중량)</summary>
    public decimal FirstWeight
    {
        get => _firstWeight;
        set { _firstWeight = value; Invalidate(); }
    }

    /// <summary>Second weighing / gross weight (2차중량)</summary>
    public decimal SecondWeight
    {
        get => _secondWeight;
        set { _secondWeight = value; Invalidate(); }
    }

    /// <summary>Net weight = |2nd - 1st| (실중량)</summary>
    public decimal NetWeight
    {
        get => _netWeight;
        set { _netWeight = value; Invalidate(); }
    }

    /// <summary>Expected weight from dispatch (이론중량)</summary>
    public decimal TheoreticalWeight
    {
        get => _theoreticalWeight;
        set { _theoreticalWeight = value; Invalidate(); }
    }

    /// <summary>Notification / status message text (알림)</summary>
    public string NotificationText
    {
        get => _notificationText;
        set { _notificationText = value ?? ""; Invalidate(); }
    }

    public StabilityState Stability
    {
        get => _stability;
        set { _stability = value; Invalidate(); }
    }

    // ── Legacy compatibility ────────────────────────────────────────────
    /// <summary>Legacy property — sets CurrentWeight from formatted string.</summary>
    public string WeightValue
    {
        get => _currentWeight.ToString("F1");
        set
        {
            if (decimal.TryParse(value, out var w))
                CurrentWeight = w;
            else
                Invalidate();
        }
    }

    // ── Paint ───────────────────────────────────────────────────────────

    protected override void OnPaintBackground(PaintEventArgs e) { }

    protected override void OnPaint(PaintEventArgs e)
    {
        if (Width < 10 || Height < 10) return;

        var g = e.Graphics;
        g.SmoothingMode = SmoothingMode.AntiAlias;
        g.TextRenderingHint = TextRenderingHint.ClearTypeGridFit;

        var bounds = new Rectangle(0, 0, Width, Height);

        // Layout heights (proportional)
        int headerH = (int)(bounds.Height * 0.10f);         // ~48px at 480
        int currentH = (int)(bounds.Height * 0.30f);        // ~144px
        int gridRowH = (int)(bounds.Height * 0.22f);        // ~106px each row
        int notifH = bounds.Height - headerH - currentH - gridRowH * 2; // remainder

        int yHeader = 0;
        int yCurrent = headerH;
        int yGridRow1 = yCurrent + currentH;
        int yGridRow2 = yGridRow1 + gridRowH;
        int yNotif = yGridRow2 + gridRowH;

        // Clip to rounded rectangle
        using var clipPath = RoundedRectHelper.Create(bounds, Theme.RadiusXl);
        g.SetClip(clipPath);

        // ── 1. Header ──────────────────────────────────────────────────
        var headerRect = new Rectangle(0, yHeader, bounds.Width, headerH);
        using (var hBrush = new SolidBrush(HeaderBg))
            g.FillRectangle(hBrush, headerRect);

        using (var hFont = new Font("맑은 고딕", 11f * Theme.FontScale, FontStyle.Bold))
        using (var hBrush = new SolidBrush(HeaderText))
        {
            g.DrawString("DK 동국씨엠", hFont, hBrush, 14, yHeader + (headerH - hFont.Height) / 2f);

            var factoryText = "부산공장";
            var factorySize = g.MeasureString(factoryText, hFont);
            g.DrawString(factoryText, hFont, hBrush,
                bounds.Width - factorySize.Width - 14, yHeader + (headerH - hFont.Height) / 2f);
        }

        // ── 2. Current weight area ─────────────────────────────────────
        var currentRect = new Rectangle(0, yCurrent, bounds.Width, currentH);
        using (var cBrush = new SolidBrush(CurrentWeightBg))
            g.FillRectangle(cBrush, currentRect);

        // "현재중량" label (red)
        using (var lblFont = new Font("맑은 고딕", 10f * Theme.FontScale, FontStyle.Bold))
        using (var lblBrush = new SolidBrush(CurrentWeightLabel))
        {
            g.DrawString("현재중량", lblFont, lblBrush, 16, yCurrent + 8);
        }

        // Stability badge (top-right of current area)
        DrawStabilityBadge(g, currentRect);

        // Large weight number (golden)
        float largeFontSize = CalculateLargeFontSize();
        if (_cachedLargeFont == null || Math.Abs(_cachedLargeFontSize - largeFontSize) > 0.5f)
        {
            _cachedLargeFont = new Font("Consolas", largeFontSize, FontStyle.Bold);
            _cachedLargeFontSize = largeFontSize;
        }

        string currentText = FormatWeight(_currentWeight);
        var currentSize = g.MeasureString(currentText, _cachedLargeFont);

        float numX = bounds.Width - currentSize.Width - 50;
        float numY = yCurrent + (currentH - currentSize.Height) / 2f + 6;

        // Glow effect when stable
        if (_stability == StabilityState.Stable)
        {
            for (int i = 3; i >= 1; i--)
            {
                int alpha = 15 + (3 - i) * 8;
                using var glowBrush = new SolidBrush(Color.FromArgb(alpha, CurrentWeightNum));
                g.DrawString(currentText, _cachedLargeFont, glowBrush, numX - i, numY);
                g.DrawString(currentText, _cachedLargeFont, glowBrush, numX + i, numY);
                g.DrawString(currentText, _cachedLargeFont, glowBrush, numX, numY - i);
                g.DrawString(currentText, _cachedLargeFont, glowBrush, numX, numY + i);
            }
        }

        using (var numBrush = new SolidBrush(CurrentWeightNum))
            g.DrawString(currentText, _cachedLargeFont, numBrush, numX, numY);

        // "Kg" unit
        using (var unitFont = new Font("맑은 고딕", 14f * Theme.FontScale, FontStyle.Regular))
        using (var unitBrush = new SolidBrush(UnitColor))
        {
            var unitSize = g.MeasureString(_unit, unitFont);
            g.DrawString(_unit, unitFont, unitBrush,
                bounds.Width - unitSize.Width - 14,
                numY + currentSize.Height - unitSize.Height - 2);
        }

        // ── 3. Grid row 1: 1차중량 / 2차중량 (blue) ────────────────────
        int halfW = bounds.Width / 2;

        DrawGridCell(g, new Rectangle(0, yGridRow1, halfW, gridRowH),
            "1차중량", _firstWeight, GridBlueBg, GridBlueLabel, 1);
        DrawGridCell(g, new Rectangle(halfW, yGridRow1, bounds.Width - halfW, gridRowH),
            "2차중량", _secondWeight, GridBlueBg, GridBlueLabel, 1);

        // Vertical divider between cells
        using (var divPen = new Pen(Color.FromArgb(40, 255, 255, 255), 1f))
        {
            g.DrawLine(divPen, halfW, yGridRow1 + 8, halfW, yGridRow1 + gridRowH - 8);
            // Horizontal divider between row 1 and row 2
            g.DrawLine(divPen, 8, yGridRow2, bounds.Width - 8, yGridRow2);
            // Vertical divider in row 2
            g.DrawLine(divPen, halfW, yGridRow2 + 8, halfW, yGridRow2 + gridRowH - 8);
        }

        // ── 4. Grid row 2: 실중량 / 이론중량 (teal) ────────────────────
        DrawGridCell(g, new Rectangle(0, yGridRow2, halfW, gridRowH),
            "실중량", _netWeight, GridTealBg, GridTealLabel, 1);
        DrawGridCell(g, new Rectangle(halfW, yGridRow2, bounds.Width - halfW, gridRowH),
            "이론중량", _theoreticalWeight, GridTealBg, GridTealLabel, 1);

        // ── 5. Notification bar ─────────────────────────────────────────
        var notifRect = new Rectangle(0, yNotif, bounds.Width, Math.Max(notifH, 30));
        Color actualNotifBg = string.IsNullOrEmpty(_notificationText) ? NotifBg : NotifBg;
        using (var nBrush = new SolidBrush(actualNotifBg))
            g.FillRectangle(nBrush, notifRect);

        string notifDisplay = string.IsNullOrEmpty(_notificationText) ? "알림" : _notificationText;
        using (var nFont = new Font("맑은 고딕", 10f * Theme.FontScale, FontStyle.Bold))
        using (var nBrush = new SolidBrush(NotifText))
        {
            var nSize = g.MeasureString(notifDisplay, nFont);
            g.DrawString(notifDisplay, nFont, nBrush,
                14, yNotif + (notifRect.Height - nSize.Height) / 2f);
        }

        g.ResetClip();

        // ── Outer border ────────────────────────────────────────────────
        using var borderPath = RoundedRectHelper.Create(new Rectangle(0, 0, Width - 1, Height - 1), Theme.RadiusXl);
        Color borderColor = _stability == StabilityState.Stable
            ? Theme.WithAlpha(Theme.Primary, 80)
            : Theme.WithAlpha(Theme.Border, 120);
        using var borderPen = new Pen(borderColor, 1.5f);
        g.DrawPath(borderPen, borderPath);
    }

    // ── Helper: draw a single grid cell ─────────────────────────────────
    private void DrawGridCell(Graphics g, Rectangle rect, string label, decimal value,
        Color bgColor, Color labelColor, float borderWidth)
    {
        using (var bg = new SolidBrush(bgColor))
            g.FillRectangle(bg, rect);

        // Label
        using (var lblFont = new Font("맑은 고딕", 9.5f * Theme.FontScale, FontStyle.Bold))
        using (var lblBrush = new SolidBrush(labelColor))
        {
            g.DrawString(label, lblFont, lblBrush, rect.X + 14, rect.Y + 10);
        }

        // Value number
        float gridFontSize = CalculateGridFontSize();
        if (_cachedGridFont == null || Math.Abs(_cachedGridFontSize - gridFontSize) > 0.5f)
        {
            _cachedGridFont = new Font("Consolas", gridFontSize, FontStyle.Bold);
            _cachedGridFontSize = gridFontSize;
        }

        string valText = FormatWeight(value);
        var valSize = g.MeasureString(valText, _cachedGridFont);

        // Right-align the number with space for unit
        float valX = rect.Right - valSize.Width - 50;
        float valY = rect.Y + (rect.Height + 16) / 2f - valSize.Height / 2f;

        using (var valBrush = new SolidBrush(GridNumColor))
            g.DrawString(valText, _cachedGridFont, valBrush, valX, valY);

        // Unit
        using (var unitFont = new Font("맑은 고딕", 10f * Theme.FontScale, FontStyle.Regular))
        using (var unitBrush = new SolidBrush(UnitColor))
        {
            var unitSize = g.MeasureString(_unit, unitFont);
            g.DrawString(_unit, unitFont, unitBrush,
                rect.Right - unitSize.Width - 12,
                valY + valSize.Height - unitSize.Height - 2);
        }
    }

    // ── Helper: stability badge ─────────────────────────────────────────
    private void DrawStabilityBadge(Graphics g, Rectangle area)
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

        using var badgeFont = new Font("맑은 고딕", 8f * Theme.FontScale, FontStyle.Bold);
        var badgeSize = g.MeasureString(badgeText, badgeFont);
        float bw = badgeSize.Width + 16;
        float bh = badgeSize.Height + 6;
        float bx = area.Right - bw - 12;
        float by = area.Y + 8;

        var badgeRect = new RectangleF(bx, by, bw, bh);
        using (var badgePath = RoundedRectHelper.Create(badgeRect, (int)(bh / 2)))
        using (var bgBrush = new SolidBrush(Theme.WithAlpha(badgeBg, 40)))
        {
            g.FillPath(bgBrush, badgePath);
        }
        using (var badgePath = RoundedRectHelper.Create(badgeRect, (int)(bh / 2)))
        using (var borderPen = new Pen(Theme.WithAlpha(badgeBg, 140), 1f))
        {
            g.DrawPath(borderPen, badgePath);
        }
        using var textBrush = new SolidBrush(badgeBg);
        g.DrawString(badgeText, badgeFont, textBrush, bx + 8, by + 3);
    }

    // ── Font sizing ─────────────────────────────────────────────────────
    private float CalculateLargeFontSize()
    {
        float scale = Math.Max(0.5f, Math.Min(1.2f, Width / 400f));
        return Math.Clamp(48f * scale, 28f, 64f) * Theme.FontScale;
    }

    private float CalculateGridFontSize()
    {
        float scale = Math.Max(0.5f, Math.Min(1.2f, Width / 400f));
        return Math.Clamp(28f * scale, 18f, 40f) * Theme.FontScale;
    }

    // ── Weight formatting ───────────────────────────────────────────────
    private static string FormatWeight(decimal weight)
    {
        if (weight == 0) return "0";
        return weight.ToString("#,##0");
    }
}
