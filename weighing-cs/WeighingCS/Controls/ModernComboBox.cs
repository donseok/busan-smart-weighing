using System.Diagnostics.CodeAnalysis;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Text;
using System.Windows.Forms;

namespace WeighingCS.Controls;

/// <summary>
/// Styled combo box with rounded border, matching the modern dark theme.
/// Wraps a standard ComboBox with custom-painted container.
/// </summary>
public class ModernComboBox : Control
{
    private readonly ComboBox _inner;
    private bool _focused;

    public ModernComboBox()
    {
        SetStyle(
            ControlStyles.AllPaintingInWmPaint |
            ControlStyles.UserPaint |
            ControlStyles.OptimizedDoubleBuffer |
            ControlStyles.ResizeRedraw,
            true);

        Size = new Size(200, Theme.InputHeight);

        _inner = new ComboBox
        {
            DropDownStyle = ComboBoxStyle.DropDownList,
            FlatStyle = FlatStyle.Flat,
            BackColor = Theme.BgElevated,
            ForeColor = Theme.TextPrimary,
            Font = Theme.FontBody,
            DrawMode = DrawMode.OwnerDrawFixed,
            ItemHeight = (int)(24 * Theme.LayoutScale),
        };

        _inner.GotFocus += (_, _) => { _focused = true; Invalidate(); };
        _inner.LostFocus += (_, _) => { _focused = false; Invalidate(); };
        _inner.DrawItem += OnDrawItem;

        Controls.Add(_inner);

        Theme.ThemeChanged += (_, _) =>
        {
            _inner.BackColor = Theme.BgElevated;
            _inner.ForeColor = Theme.TextPrimary;
            _inner.Font = Theme.FontBody;
            _inner.ItemHeight = (int)(24 * Theme.LayoutScale);
            Invalidate();
        };
    }

    /// <summary>Items collection (delegated to inner ComboBox).</summary>
    public ComboBox.ObjectCollection Items => _inner.Items;

    /// <summary>Selected index (delegated).</summary>
    public int SelectedIndex
    {
        get => _inner.SelectedIndex;
        set => _inner.SelectedIndex = value;
    }

    public object? SelectedItem => _inner.SelectedItem;

    public ComboBoxStyle DropDownStyle
    {
        get => _inner.DropDownStyle;
        set => _inner.DropDownStyle = value;
    }

    [AllowNull]
    public override Font Font
    {
        get => base.Font;
        set { base.Font = value ?? base.Font; _inner.Font = value ?? _inner.Font; }
    }

    private void OnDrawItem(object? sender, DrawItemEventArgs e)
    {
        if (e.Index < 0) return;

        bool selected = (e.State & DrawItemState.Selected) == DrawItemState.Selected;
        Color bg = selected ? Theme.Primary : Theme.BgElevated;
        Color fg = selected ? Color.White : Theme.TextPrimary;

        using var bgBrush = new SolidBrush(bg);
        e.Graphics.FillRectangle(bgBrush, e.Bounds);

        using var textBrush = new SolidBrush(fg);
        string text = _inner.Items[e.Index]?.ToString() ?? "";
        e.Graphics.DrawString(text, Theme.FontBody, textBrush,
            e.Bounds.X + 6, e.Bounds.Y + (e.Bounds.Height - Theme.FontBody.Height) / 2f);
    }

    protected override void OnLayout(LayoutEventArgs levent)
    {
        base.OnLayout(levent);
        if (_inner == null) return;
        int pad = 3;
        _inner.SetBounds(pad, pad, Width - pad * 2, Height - pad * 2);
    }

    protected override void OnPaintBackground(PaintEventArgs e) { }

    protected override void OnPaint(PaintEventArgs e)
    {
        var g = e.Graphics;
        g.SmoothingMode = SmoothingMode.AntiAlias;

        var bounds = new Rectangle(0, 0, Width - 1, Height - 1);

        // Focus glow
        if (_focused)
        {
            var glowRect = new Rectangle(-1, -1, Width + 1, Height + 1);
            using var glowPath = RoundedRectHelper.Create(glowRect, Theme.RadiusSmall + 1);
            using var glowBrush = new SolidBrush(Theme.WithAlpha(Theme.Primary, 25));
            g.FillPath(glowBrush, glowPath);
        }

        // Background
        using (var path = RoundedRectHelper.Create(bounds, Theme.RadiusSmall))
        using (var bgBrush = new SolidBrush(Theme.BgElevated))
        {
            g.FillPath(bgBrush, path);
        }

        // Border
        Color borderColor = _focused ? Theme.BorderFocus : Theme.Border;
        using (var path = RoundedRectHelper.Create(bounds, Theme.RadiusSmall))
        using (var pen = new Pen(borderColor, _focused ? 1.5f : 1f))
        {
            g.DrawPath(pen, path);
        }
    }

    protected override void OnEnabledChanged(EventArgs e)
    {
        base.OnEnabledChanged(e);
        _inner.Enabled = Enabled;
        Invalidate();
    }
}
