using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Text;
using WeighingCS.Controls;

namespace WeighingCS;

/// <summary>
/// Modern splash screen with gradient background, animated progress, and branding.
/// </summary>
public class SplashForm : Form
{
    private readonly ModernProgressBar progressBar;
    private readonly Label lblStatus;

    public SplashForm()
    {
        FormBorderStyle = FormBorderStyle.None;
        StartPosition = FormStartPosition.CenterScreen;
        ClientSize = new Size(520, 320);
        BackColor = Theme.BgDarkest;
        ShowInTaskbar = false;
        TopMost = true;
        DoubleBuffered = true;

        SetStyle(
            ControlStyles.AllPaintingInWmPaint |
            ControlStyles.UserPaint |
            ControlStyles.OptimizedDoubleBuffer,
            true);

        // Progress bar
        progressBar = new ModernProgressBar
        {
            Minimum = 0,
            Maximum = 100,
            Value = 0,
            BarColor = Theme.Primary,
            Location = new Point(80, 220),
            Size = new Size(360, 4),
        };

        // Status text
        lblStatus = new Label
        {
            Text = "시스템을 초기화하는 중...",
            Font = Theme.FontBody,
            ForeColor = Theme.Primary,
            AutoSize = false,
            TextAlign = ContentAlignment.MiddleCenter,
            Size = new Size(520, 25),
            Location = new Point(0, 238),
            BackColor = Color.Transparent,
        };

        // Version
        var lblVersion = new Label
        {
            Text = "v1.0.0",
            Font = Theme.FontCaption,
            ForeColor = Theme.TextMuted,
            AutoSize = false,
            TextAlign = ContentAlignment.MiddleCenter,
            Size = new Size(520, 18),
            Location = new Point(0, 290),
            BackColor = Color.Transparent,
        };

        Controls.AddRange(new Control[] { progressBar, lblStatus, lblVersion });
    }

    protected override void OnPaint(PaintEventArgs e)
    {
        base.OnPaint(e);
        var g = e.Graphics;
        g.SmoothingMode = SmoothingMode.AntiAlias;
        g.TextRenderingHint = TextRenderingHint.ClearTypeGridFit;

        // Background gradient
        using (var bgBrush = new LinearGradientBrush(
            ClientRectangle, Theme.BgDarkest, Theme.BgBase, LinearGradientMode.ForwardDiagonal))
        {
            g.FillRectangle(bgBrush, ClientRectangle);
        }

        // Subtle radial glow in center
        int glowSize = 300;
        var glowRect = new Rectangle((Width - glowSize) / 2, 20, glowSize, glowSize);
        using (var glowPath = new GraphicsPath())
        {
            glowPath.AddEllipse(glowRect);
            using var glowBrush = new PathGradientBrush(glowPath)
            {
                CenterColor = Theme.WithAlpha(Theme.Primary, 15),
                SurroundColors = new[] { Theme.WithAlpha(Theme.Primary, 0) }
            };
            g.FillEllipse(glowBrush, glowRect);
        }

        // Border
        using (var borderPen = new Pen(Theme.WithAlpha(Theme.Border, 100), 1f))
            g.DrawRectangle(borderPen, 0, 0, Width - 1, Height - 1);

        // Logo circle
        int logoSize = 52;
        int logoX = (Width - logoSize) / 2;
        int logoY = 40;
        var logoRect = new Rectangle(logoX, logoY, logoSize, logoSize);

        using (var logoPath = new GraphicsPath())
        {
            logoPath.AddEllipse(logoRect);
            using var logoBrush = new LinearGradientBrush(logoRect,
                Theme.Primary, Theme.PrimaryDark, LinearGradientMode.ForwardDiagonal);
            g.FillPath(logoBrush, logoPath);
        }

        // Logo text
        using (var logoFont = new Font("Segoe UI", 16F, FontStyle.Bold))
        {
            var logoTextSize = g.MeasureString("DK", logoFont);
            float ltx = logoX + (logoSize - logoTextSize.Width) / 2f;
            float lty = logoY + (logoSize - logoTextSize.Height) / 2f;
            using var logoTextBrush = new SolidBrush(Color.White);
            g.DrawString("DK", logoFont, logoTextBrush, ltx, lty);
        }

        // Title
        using (var titleFont = new Font("Segoe UI", 20F, FontStyle.Bold))
        using (var titleBrush = new SolidBrush(Theme.TextPrimary))
        {
            var titleSize = g.MeasureString("부산 스마트 계량 시스템", titleFont);
            g.DrawString("부산 스마트 계량 시스템", titleFont,
                titleBrush, (Width - titleSize.Width) / 2f, 110);
        }

        // Subtitle
        using (var subFont = new Font("Segoe UI", 9.5F))
        using (var subBrush = new SolidBrush(Theme.TextMuted))
        {
            string sub = "BUSAN SMART WEIGHING SYSTEM";
            var subSize = g.MeasureString(sub, subFont);
            g.DrawString(sub, subFont, subBrush, (Width - subSize.Width) / 2f, 150);
        }

        // Decorative line
        int lineY = 180;
        int lineWidth = 60;
        using (var lineBrush = new LinearGradientBrush(
            new Point(Width / 2 - lineWidth, lineY), new Point(Width / 2 + lineWidth, lineY),
            Theme.WithAlpha(Theme.Primary, 0), Theme.Primary))
        {
            using var pen = new Pen(lineBrush, 1f);
            g.DrawLine(pen, Width / 2 - lineWidth, lineY, Width / 2, lineY);
        }
        using (var lineBrush = new LinearGradientBrush(
            new Point(Width / 2, lineY), new Point(Width / 2 + lineWidth, lineY),
            Theme.Primary, Theme.WithAlpha(Theme.Primary, 0)))
        {
            using var pen = new Pen(lineBrush, 1f);
            g.DrawLine(pen, Width / 2, lineY, Width / 2 + lineWidth, lineY);
        }
    }

    public void UpdateProgress(int percent, string status)
    {
        if (InvokeRequired)
        {
            BeginInvoke(() => UpdateProgress(percent, status));
            return;
        }

        progressBar.Value = Math.Min(100, percent);
        lblStatus.Text = status;
        Invalidate();
    }
}
