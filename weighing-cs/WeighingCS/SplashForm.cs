using WeighingCS.Controls;

namespace WeighingCS;

/// <summary>
/// Splash screen with initialization progress.
/// </summary>
public class SplashForm : Form
{
    private readonly Label lblTitle;
    private readonly Label lblSubtitle;
    private readonly ModernProgressBar progressBar;
    private readonly Label lblStatus;

    public SplashForm()
    {
        // Form settings
        this.FormBorderStyle = FormBorderStyle.None;
        this.StartPosition = FormStartPosition.CenterScreen;
        this.ClientSize = new Size(480, 300);
        this.BackColor = Theme.BgBase;
        this.ShowInTaskbar = false;
        this.TopMost = true;
        this.DoubleBuffered = true;

        // Logo icon
        var lblLogo = new Label
        {
            Text = "DK",
            Font = new Font("Segoe UI", 24F, FontStyle.Bold),
            ForeColor = Color.White,
            BackColor = Theme.Primary,
            TextAlign = ContentAlignment.MiddleCenter,
            Size = new Size(60, 60),
            Location = new Point(210, 30),
        };
        // Round the logo
        var path = new System.Drawing.Drawing2D.GraphicsPath();
        path.AddEllipse(0, 0, 60, 60);
        lblLogo.Region = new Region(path);

        // Title
        lblTitle = new Label
        {
            Text = "부산 스마트 계량 시스템",
            Font = new Font("Segoe UI", 18F, FontStyle.Bold),
            ForeColor = Theme.TextPrimary,
            AutoSize = false,
            TextAlign = ContentAlignment.MiddleCenter,
            Size = new Size(480, 40),
            Location = new Point(0, 105),
        };

        // Subtitle
        lblSubtitle = new Label
        {
            Text = "BUSAN SMART WEIGHING SYSTEM",
            Font = new Font("Segoe UI", 9F),
            ForeColor = Theme.TextSecondary,
            AutoSize = false,
            TextAlign = ContentAlignment.MiddleCenter,
            Size = new Size(480, 25),
            Location = new Point(0, 145),
        };

        // Progress bar (modern)
        progressBar = new ModernProgressBar
        {
            Minimum = 0,
            Maximum = 100,
            Value = 0,
            BarColor = Theme.Primary,
            Location = new Point(60, 200),
            Size = new Size(360, 6),
        };

        // Status text
        lblStatus = new Label
        {
            Text = "시스템을 초기화하는 중...",
            Font = new Font("Segoe UI", 9F),
            ForeColor = Theme.Primary,
            AutoSize = false,
            TextAlign = ContentAlignment.MiddleCenter,
            Size = new Size(480, 25),
            Location = new Point(0, 220),
        };

        // Version
        var lblVersion = new Label
        {
            Text = "v1.0.0",
            Font = new Font("Segoe UI", 8F),
            ForeColor = Theme.TextMuted,
            AutoSize = false,
            TextAlign = ContentAlignment.MiddleCenter,
            Size = new Size(480, 20),
            Location = new Point(0, 265),
        };

        Controls.AddRange(new Control[] { lblLogo, lblTitle, lblSubtitle, progressBar, lblStatus, lblVersion });
    }

    /// <summary>
    /// Updates the splash screen progress.
    /// </summary>
    public void UpdateProgress(int percent, string status)
    {
        if (InvokeRequired)
        {
            BeginInvoke(() => UpdateProgress(percent, status));
            return;
        }

        progressBar.Value = Math.Min(100, percent);
        lblStatus.Text = status;
    }
}
