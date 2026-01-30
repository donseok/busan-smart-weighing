using System.Diagnostics.CodeAnalysis;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Text;
using System.Windows.Forms;

namespace WeighingCS.Controls;

/// <summary>
/// Styled text input with rounded border, focus glow, and placeholder support.
/// Wraps a standard TextBox with custom-painted container.
/// </summary>
public class ModernTextBox : Control
{
    private readonly TextBox _inner;
    private bool _focused;
    private string _placeholder = "";

    public ModernTextBox()
    {
        SetStyle(
            ControlStyles.AllPaintingInWmPaint |
            ControlStyles.UserPaint |
            ControlStyles.OptimizedDoubleBuffer |
            ControlStyles.ResizeRedraw,
            true);

        Size = new Size(200, Theme.InputHeight);

        _inner = new TextBox
        {
            BorderStyle = BorderStyle.None,
            BackColor = Theme.BgElevated,
            ForeColor = Theme.TextPrimary,
            Font = Theme.FontBody,
        };

        _inner.GotFocus += (_, _) => { _focused = true; Invalidate(); };
        _inner.LostFocus += (_, _) => { _focused = false; Invalidate(); };
        _inner.TextChanged += (s, e) => OnTextChanged(e);

        Controls.Add(_inner);

        Theme.ThemeChanged += (_, _) =>
        {
            _inner.BackColor = Theme.BgElevated;
            _inner.ForeColor = Theme.TextPrimary;
            _inner.Font = Theme.FontBody;
            Invalidate();
        };
    }

    [AllowNull]
    public override string Text
    {
        get => _inner.Text;
        set => _inner.Text = value ?? "";
    }

    public string Placeholder
    {
        get => _placeholder;
        set { _placeholder = value; Invalidate(); }
    }

    [AllowNull]
    public override Font Font
    {
        get => base.Font;
        set { base.Font = value ?? base.Font; _inner.Font = value ?? _inner.Font; }
    }

    public new void Focus() => _inner.Focus();
    public void SelectAll() => _inner.SelectAll();

    protected override void OnLayout(LayoutEventArgs levent)
    {
        base.OnLayout(levent);
        if (_inner == null) return;
        int pad = Theme.SpacingSm + 2;
        int innerHeight = _inner.PreferredHeight;
        int innerY = (Height - innerHeight) / 2;
        _inner.SetBounds(pad, innerY, Width - pad * 2, innerHeight);
    }

    protected override void OnPaintBackground(PaintEventArgs e) { }

    protected override void OnPaint(PaintEventArgs e)
    {
        var g = e.Graphics;
        g.SmoothingMode = SmoothingMode.AntiAlias;
        g.TextRenderingHint = TextRenderingHint.ClearTypeGridFit;

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
        float borderWidth = _focused ? 1.5f : 1f;
        using (var path = RoundedRectHelper.Create(bounds, Theme.RadiusSmall))
        using (var pen = new Pen(borderColor, borderWidth))
        {
            g.DrawPath(pen, path);
        }

        // Placeholder text when empty and not focused
        if (string.IsNullOrEmpty(_inner.Text) && !_focused && !string.IsNullOrEmpty(_placeholder))
        {
            int pad = Theme.SpacingSm + 2;
            using var placeholderBrush = new SolidBrush(Theme.TextMuted);
            g.DrawString(_placeholder, Font, placeholderBrush, pad, (Height - Font.Height) / 2f);
        }
    }

    protected override void OnEnabledChanged(EventArgs e)
    {
        base.OnEnabledChanged(e);
        _inner.Enabled = Enabled;
        _inner.BackColor = Enabled ? Theme.BgElevated : Theme.BgSurface;
        _inner.ForeColor = Enabled ? Theme.TextPrimary : Theme.TextDisabled;
        Invalidate();
    }

    protected override void OnClick(EventArgs e)
    {
        base.OnClick(e);
        _inner.Focus();
    }
}
