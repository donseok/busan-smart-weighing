using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Text;
using System.Windows.Forms;

namespace WeighingCS.Controls;

/// <summary>
/// Custom-drawn checkbox with rounded square and smooth styling.
/// </summary>
public class ModernCheckBox : Control
{
    private bool _checked;
    private bool _hover;

    public event EventHandler? CheckedChanged;

    public ModernCheckBox()
    {
        SetStyle(
            ControlStyles.AllPaintingInWmPaint |
            ControlStyles.UserPaint |
            ControlStyles.OptimizedDoubleBuffer |
            ControlStyles.ResizeRedraw |
            ControlStyles.StandardClick,
            true);

        Size = new Size(200, (int)(28 * Theme.LayoutScale));
        Cursor = Cursors.Hand;
        Font = Theme.FontBody;

        Theme.ThemeChanged += (_, _) => { Font = Theme.FontBody; Invalidate(); };
    }

    public bool Checked
    {
        get => _checked;
        set
        {
            if (_checked == value) return;
            _checked = value;
            Invalidate();
            CheckedChanged?.Invoke(this, EventArgs.Empty);
        }
    }

    protected override void OnMouseClick(MouseEventArgs e)
    {
        base.OnMouseClick(e);
        if (e.Button == MouseButtons.Left)
            Checked = !_checked;
    }

    protected override void OnMouseEnter(EventArgs e) { _hover = true; Invalidate(); base.OnMouseEnter(e); }
    protected override void OnMouseLeave(EventArgs e) { _hover = false; Invalidate(); base.OnMouseLeave(e); }

    protected override void OnPaintBackground(PaintEventArgs e) { }

    protected override void OnPaint(PaintEventArgs e)
    {
        var g = e.Graphics;
        g.SmoothingMode = SmoothingMode.AntiAlias;
        g.TextRenderingHint = TextRenderingHint.ClearTypeGridFit;

        // Clear
        using (var clearBrush = new SolidBrush(Parent?.BackColor ?? Theme.BgBase))
            g.FillRectangle(clearBrush, ClientRectangle);

        int boxSize = (int)(18 * Theme.LayoutScale);
        int boxY = (Height - boxSize) / 2;
        var boxRect = new Rectangle(0, boxY, boxSize, boxSize);

        if (_checked)
        {
            // Filled box with primary color
            using (var path = RoundedRectHelper.Create(boxRect, Theme.RadiusXs))
            using (var brush = new SolidBrush(Theme.Primary))
            {
                g.FillPath(brush, path);
            }

            // Checkmark
            int penW = Math.Max(2, boxSize / 9);
            using var pen = new Pen(Color.White, penW) { StartCap = LineCap.Round, EndCap = LineCap.Round, LineJoin = LineJoin.Round };
            int cx = boxRect.X + boxSize / 2;
            int cy = boxRect.Y + boxSize / 2;
            int cs = boxSize / 5;
            g.DrawLines(pen, new PointF[]
            {
                new(cx - cs, cy),
                new(cx - cs / 4f, cy + cs * 0.75f),
                new(cx + cs * 1.25f, cy - cs),
            });
        }
        else
        {
            // Empty box
            Color borderColor = _hover ? Theme.TextSecondary : Theme.Border;
            using (var path = RoundedRectHelper.Create(boxRect, Theme.RadiusXs))
            using (var pen = new Pen(borderColor, 1.5f))
            {
                g.DrawPath(pen, path);
            }

            // Subtle hover fill
            if (_hover)
            {
                using var path = RoundedRectHelper.Create(boxRect, Theme.RadiusXs);
                using var brush = new SolidBrush(Theme.WithAlpha(Theme.Primary, 15));
                g.FillPath(brush, path);
            }
        }

        // Label text
        if (!string.IsNullOrEmpty(Text))
        {
            int textX = boxSize + Theme.SpacingSm;
            Color textColor = Enabled ? (_checked ? Theme.TextPrimary : Theme.TextSecondary) : Theme.TextDisabled;
            using var textBrush = new SolidBrush(textColor);
            g.DrawString(Text, Font, textBrush, textX, (Height - Font.Height) / 2f);
        }
    }
}
