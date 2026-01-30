using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Text;
using System.Windows.Forms;

namespace WeighingCS.Controls;

/// <summary>
/// Owner-drawn ListView with modern dark theme styling.
/// Provides alternating row colors, custom header, and selection highlights.
/// </summary>
public class ModernListView : ListView
{
    public ModernListView()
    {
        OwnerDraw = true;
        FullRowSelect = true;
        GridLines = false;
        BorderStyle = BorderStyle.None;
        View = View.Details;
        BackColor = Theme.BgElevated;
        ForeColor = Theme.TextPrimary;
        Font = Theme.FontBody;
        HeaderStyle = ColumnHeaderStyle.Nonclickable;

        SetStyle(ControlStyles.OptimizedDoubleBuffer | ControlStyles.AllPaintingInWmPaint, true);

        DrawColumnHeader += OnDrawHeader;
        DrawItem += OnDrawItem;
        DrawSubItem += OnDrawSubItem;

        Theme.ThemeChanged += (_, _) =>
        {
            BackColor = Theme.BgElevated;
            ForeColor = Theme.TextPrimary;
            Font = Theme.FontBody;
            Invalidate();
        };

        Resize += (_, _) => FillLastColumn();
    }

    /// <summary>
    /// Stretches the last column to fill remaining horizontal space,
    /// preventing an empty white gap on the right side.
    /// </summary>
    private void FillLastColumn()
    {
        if (Columns.Count == 0) return;

        int usedWidth = 0;
        for (int i = 0; i < Columns.Count - 1; i++)
            usedWidth += Columns[i].Width;

        int remaining = ClientSize.Width - usedWidth;
        if (remaining > 0)
            Columns[Columns.Count - 1].Width = remaining;
    }

    protected override void OnHandleCreated(EventArgs e)
    {
        base.OnHandleCreated(e);
        FillLastColumn();
    }

    /// <summary>
    /// Enable column click for sorting (overrides default Nonclickable).
    /// </summary>
    public void EnableSortableHeaders()
    {
        HeaderStyle = ColumnHeaderStyle.Clickable;
    }

    private void OnDrawHeader(object? sender, DrawListViewColumnHeaderEventArgs e)
    {
        var g = e.Graphics;
        g.TextRenderingHint = TextRenderingHint.ClearTypeGridFit;

        // Header background
        using (var brush = new SolidBrush(Theme.BgSurface))
            g.FillRectangle(brush, e.Bounds);

        // Bottom border
        using (var pen = new Pen(Theme.Border, 1f))
            g.DrawLine(pen, e.Bounds.Left, e.Bounds.Bottom - 1, e.Bounds.Right, e.Bounds.Bottom - 1);

        // Right separator
        if (e.ColumnIndex < Columns.Count - 1)
        {
            using var pen = new Pen(Theme.WithAlpha(Theme.Border, 80), 1f);
            g.DrawLine(pen, e.Bounds.Right - 1, e.Bounds.Top + 4, e.Bounds.Right - 1, e.Bounds.Bottom - 4);
        }

        // Header text
        int textPad = (int)(8 * Theme.LayoutScale);
        var textRect = new Rectangle(e.Bounds.X + textPad, e.Bounds.Y, e.Bounds.Width - textPad * 2, e.Bounds.Height);
        using var textBrush = new SolidBrush(Theme.TextSecondary);
        using var sf = new StringFormat
        {
            Alignment = StringAlignment.Near,
            LineAlignment = StringAlignment.Center,
            Trimming = StringTrimming.EllipsisCharacter,
        };
        g.DrawString(e.Header?.Text ?? "", Theme.FontSmallBold, textBrush, textRect, sf);
    }

    private void OnDrawItem(object? sender, DrawListViewItemEventArgs e)
    {
        // Row background handled in OnDrawSubItem
    }

    private void OnDrawSubItem(object? sender, DrawListViewSubItemEventArgs e)
    {
        if (e.Item == null || e.SubItem == null) return;

        var g = e.Graphics;
        g.TextRenderingHint = TextRenderingHint.ClearTypeGridFit;

        bool isSelected = e.Item.Selected;
        bool isAlternate = e.ItemIndex % 2 == 1;

        // Row background
        Color bgColor;
        if (isSelected)
            bgColor = Theme.WithAlpha(Theme.Primary, 30);
        else if (isAlternate)
            bgColor = Theme.WithAlpha(Theme.BgSurface, 80);
        else
            bgColor = Theme.BgElevated;

        using (var brush = new SolidBrush(bgColor))
            g.FillRectangle(brush, e.Bounds);

        // Left accent for selected row
        if (isSelected && e.ColumnIndex == 0)
        {
            using var accentBrush = new SolidBrush(Theme.Primary);
            g.FillRectangle(accentBrush, e.Bounds.X, e.Bounds.Y, 3, e.Bounds.Height);
        }

        // Bottom border (subtle)
        using (var pen = new Pen(Theme.WithAlpha(Theme.Border, 40), 1f))
            g.DrawLine(pen, e.Bounds.Left, e.Bounds.Bottom - 1, e.Bounds.Right, e.Bounds.Bottom - 1);

        // Cell text
        Color textColor = isSelected ? Theme.PrimaryLight : Theme.TextPrimary;

        // Status column (last column) - colorize by status text
        if (e.ColumnIndex == Columns.Count - 1)
        {
            string status = e.SubItem.Text;
            if (status.Contains("완료")) textColor = Theme.Success;
            else if (status.Contains("진행")) textColor = Theme.Warning;
            else if (status.Contains("오류") || status.Contains("실패")) textColor = Theme.Error;
        }

        int textPad = (int)(8 * Theme.LayoutScale);
        var textRect = new Rectangle(e.Bounds.X + textPad, e.Bounds.Y, e.Bounds.Width - textPad * 2, e.Bounds.Height);
        using var textBrush = new SolidBrush(textColor);
        using var sf = new StringFormat
        {
            Alignment = StringAlignment.Near,
            LineAlignment = StringAlignment.Center,
            Trimming = StringTrimming.EllipsisCharacter,
        };
        g.DrawString(e.SubItem.Text, Theme.FontBody, textBrush, textRect, sf);
    }
}
