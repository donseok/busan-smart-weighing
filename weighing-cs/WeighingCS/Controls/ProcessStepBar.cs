using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Text;
using System.Windows.Forms;

namespace WeighingCS.Controls;

/// <summary>
/// Horizontal 4-step progress bar: 대기 → 계량 → 안정화 → 완료.
/// Current step has glow effect, completed steps filled.
/// </summary>
public class ProcessStepBar : Control
{
    private static readonly string[] Steps = { "대기", "계량", "안정화", "완료" };
    private int _currentStep; // 0-based, -1 for none
    private string _statusTag = "대기";

    public ProcessStepBar()
    {
        SetStyle(
            ControlStyles.AllPaintingInWmPaint |
            ControlStyles.UserPaint |
            ControlStyles.OptimizedDoubleBuffer |
            ControlStyles.ResizeRedraw,
            true);

        Size = new Size(600, 48);
    }

    /// <summary>
    /// Current step index (0 = 대기, 1 = 계량, 2 = 안정화, 3 = 완료). -1 = none.
    /// </summary>
    public int CurrentStep
    {
        get => _currentStep;
        set { _currentStep = Math.Clamp(value, -1, Steps.Length - 1); Invalidate(); }
    }

    /// <summary>
    /// Optional status tag text shown at the right side.
    /// </summary>
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

        // Calculate segment widths
        int tagWidth = 80;
        int stepsAreaWidth = Width - tagWidth - Theme.SpacingMd * 2;
        int segmentWidth = stepsAreaWidth / Steps.Length;
        int segHeight = 6;
        int segY = Height / 2 - segHeight / 2 + 8;
        int labelY = Height / 2 - 16;

        for (int i = 0; i < Steps.Length; i++)
        {
            int sx = Theme.SpacingMd + i * segmentWidth;

            // Segment background
            var segRect = new Rectangle(sx, segY, segmentWidth - 4, segHeight);
            bool isCompleted = i < _currentStep;
            bool isCurrent = i == _currentStep;

            Color segColor = isCompleted ? Theme.Primary :
                             isCurrent ? Theme.Primary :
                             Theme.BgElevated;

            // Glow for current step
            if (isCurrent)
            {
                var glowRect = new Rectangle(sx - 2, segY - 2, segmentWidth, segHeight + 4);
                using var glowPath = new GraphicsPath();
                glowPath.AddRectangle(glowRect);
                using var glowBrush = new SolidBrush(Theme.WithAlpha(Theme.Primary, 50));
                g.FillRectangle(glowBrush, glowRect);
            }

            // Segment fill
            using (var segPath = RoundedRectHelper.Create(segRect, segHeight / 2))
            using (var segBrush = new SolidBrush(isCompleted || isCurrent ? segColor : Theme.BgElevated))
            {
                g.FillPath(segBrush, segPath);
            }

            // Inactive segment border
            if (!isCompleted && !isCurrent)
            {
                using var segPath = RoundedRectHelper.Create(segRect, segHeight / 2);
                using var pen = new Pen(Theme.Border, 0.5f);
                g.DrawPath(pen, segPath);
            }

            // Step label
            Color labelColor = isCompleted || isCurrent ? Theme.TextPrimary : Theme.TextMuted;
            using var labelBrush = new SolidBrush(labelColor);
            var labelSize = g.MeasureString(Steps[i], Theme.FontSmall);
            float lx = sx + (segmentWidth - 4 - labelSize.Width) / 2f;
            g.DrawString(Steps[i], Theme.FontSmall, labelBrush, lx, labelY);
        }

        // Status tag (right side)
        if (!string.IsNullOrEmpty(_statusTag))
        {
            using var tagFont = new Font("Segoe UI", 8F, FontStyle.Bold);
            var tagSize = g.MeasureString(_statusTag, tagFont);
            float tw = tagSize.Width + 14;
            float th = tagSize.Height + 4;
            float tx = Width - tw - Theme.SpacingSm;
            float ty = (Height - th) / 2f;

            Color tagColor = _currentStep >= Steps.Length - 1 ? Theme.Success :
                             _currentStep >= 0 ? Theme.Primary : Theme.TextMuted;

            var tagRect = new RectangleF(tx, ty, tw, th);
            using (var tagPath = RoundedRectHelper.Create(tagRect, (int)(th / 2)))
            using (var tagBrush = new SolidBrush(Theme.WithAlpha(tagColor, 35)))
            {
                g.FillPath(tagBrush, tagPath);
            }
            using var tagTextBrush = new SolidBrush(tagColor);
            g.DrawString(_statusTag, tagFont, tagTextBrush, tx + 7, ty + 2);
        }
    }
}
