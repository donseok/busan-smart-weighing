using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Text;
using System.Windows.Forms;

namespace WeighingCS.Controls;

/// <summary>
/// Horizontal 4-step progress bar with circle indicators and connecting lines.
/// Steps: 대기 → 계량 → 안정화 → 완료.
/// </summary>
public class ProcessStepBar : Control
{
    private static readonly string[] Steps = { "대기", "계량", "안정화", "완료" };
    private int _currentStep;
    private string _statusTag = "대기";

    public ProcessStepBar()
    {
        SetStyle(
            ControlStyles.AllPaintingInWmPaint |
            ControlStyles.UserPaint |
            ControlStyles.OptimizedDoubleBuffer |
            ControlStyles.ResizeRedraw,
            true);

        Size = new Size(600, (int)(64 * Theme.LayoutScale));
        Theme.ThemeChanged += (_, _) => Invalidate();
    }

    public int CurrentStep
    {
        get => _currentStep;
        set { _currentStep = Math.Clamp(value, -1, Steps.Length - 1); Invalidate(); }
    }

    public string StatusTag
    {
        get => _statusTag;
        set { _statusTag = value; Invalidate(); }
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
        using (var pen = new Pen(Theme.WithAlpha(Theme.Border, 120), 1f))
        {
            g.DrawPath(pen, path);
        }

        int circleSize = (int)(24 * Theme.LayoutScale);
        int tagWidth = (int)(90 * Theme.LayoutScale);
        int stepsAreaWidth = Width - tagWidth - Theme.SpacingXl * 2;
        int spacing = stepsAreaWidth / (Steps.Length - 1);
        int startX = Theme.SpacingXl;
        int centerY = Height / 2;

        for (int i = 0; i < Steps.Length; i++)
        {
            int cx = startX + i * spacing;
            bool isCompleted = i < _currentStep;
            bool isCurrent = i == _currentStep;
            bool isFuture = i > _currentStep;

            // Connecting line to next step
            if (i < Steps.Length - 1)
            {
                int nextX = startX + (i + 1) * spacing;
                Color lineColor = isCompleted ? Theme.Primary : Theme.WithAlpha(Theme.Border, 100);
                float lineWidth = isCompleted ? 2f : 1.5f;
                using var linePen = new Pen(lineColor, lineWidth);
                int lineGap = (int)(2 * Theme.LayoutScale);
                g.DrawLine(linePen, cx + circleSize / 2 + lineGap, centerY - 6, nextX - circleSize / 2 - lineGap, centerY - 6);
            }

            // Circle
            int circleX = cx - circleSize / 2;
            int circleY = centerY - 6 - circleSize / 2;
            var circleRect = new Rectangle(circleX, circleY, circleSize, circleSize);

            if (isCompleted)
            {
                // Filled circle with checkmark
                using (var fillBrush = new SolidBrush(Theme.Primary))
                    g.FillEllipse(fillBrush, circleRect);

                // Checkmark
                int checkScale = circleSize / 24;
                using var checkPen = new Pen(Color.White, 2f * checkScale) { StartCap = LineCap.Round, EndCap = LineCap.Round, LineJoin = LineJoin.Round };
                int ccx = circleX + circleSize / 2;
                int ccy = circleY + circleSize / 2;
                int cs = Math.Max(1, circleSize / 6);
                g.DrawLines(checkPen, new PointF[]
                {
                    new(ccx - cs, ccy),
                    new(ccx - cs / 4f, ccy + cs * 0.75f),
                    new(ccx + cs * 1.25f, ccy - cs),
                });
            }
            else if (isCurrent)
            {
                // Glow
                var glowRect = new Rectangle(circleX - 4, circleY - 4, circleSize + 8, circleSize + 8);
                using (var glowBrush = new SolidBrush(Theme.WithAlpha(Theme.Primary, 30)))
                    g.FillEllipse(glowBrush, glowRect);

                // Outlined circle with dot
                using (var outlinePen = new Pen(Theme.Primary, 2f))
                    g.DrawEllipse(outlinePen, circleRect);

                int dotSize = (int)(8 * Theme.LayoutScale);
                using (var dotBrush = new SolidBrush(Theme.Primary))
                    g.FillEllipse(dotBrush,
                        circleX + (circleSize - dotSize) / 2,
                        circleY + (circleSize - dotSize) / 2,
                        dotSize, dotSize);
            }
            else
            {
                // Empty circle
                using var outlinePen = new Pen(Theme.WithAlpha(Theme.Border, 120), 1.5f);
                g.DrawEllipse(outlinePen, circleRect);
            }

            // Step label below circle
            Color labelColor = isCompleted || isCurrent ? Theme.TextPrimary : Theme.TextMuted;
            using var labelBrush = new SolidBrush(labelColor);
            Font labelFont = isCurrent ? Theme.FontSmallBold : Theme.FontSmall;
            var labelSize = g.MeasureString(Steps[i], labelFont);
            float lx = cx - labelSize.Width / 2f;
            float ly = circleY + circleSize + Theme.SpacingXs;
            g.DrawString(Steps[i], labelFont, labelBrush, lx, ly);
        }

        // Status tag (right side)
        if (!string.IsNullOrEmpty(_statusTag))
        {
            var tagSize = g.MeasureString(_statusTag, Theme.FontStatusTag);
            float tw = Math.Max(tagSize.Width + 16, 60);
            float th = tagSize.Height + 6;
            float tx = Width - tw - Theme.SpacingMd;
            float ty = (Height - th) / 2f;

            Color tagColor = _currentStep >= Steps.Length - 1 ? Theme.Success :
                             _currentStep >= 0 ? Theme.Primary : Theme.TextMuted;

            var tagRect = new RectangleF(tx, ty, tw, th);
            using (var tagPath = RoundedRectHelper.Create(tagRect, (int)(th / 2)))
            using (var tagBrush = new SolidBrush(Theme.WithAlpha(tagColor, 25)))
            {
                g.FillPath(tagBrush, tagPath);
            }
            using (var tagPath = RoundedRectHelper.Create(tagRect, (int)(th / 2)))
            using (var tagPen = new Pen(Theme.WithAlpha(tagColor, 80), 1f))
            {
                g.DrawPath(tagPen, tagPath);
            }
            using var tagTextBrush = new SolidBrush(tagColor);
            g.DrawString(_statusTag, Theme.FontStatusTag, tagTextBrush,
                tx + (tw - tagSize.Width) / 2f, ty + 3);
        }
    }
}
