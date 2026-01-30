namespace WeighingCS;

static class Program
{
    /// <summary>
    /// The main entry point for the Busan Smart Weighing CS application.
    /// </summary>
    [STAThread]
    static void Main()
    {
        ApplicationConfiguration.Initialize();

        // Load saved theme preference (dark/light mode)
        WeighingCS.Controls.Theme.LoadPreference();

        // Show splash screen during initialization
        var splash = new SplashForm();
        splash.Show();
        Application.DoEvents();

        splash.UpdateProgress(20, "설정을 불러오는 중...");
        Application.DoEvents();
        Thread.Sleep(300);

        splash.UpdateProgress(50, "서비스를 초기화하는 중...");
        Application.DoEvents();
        Thread.Sleep(300);

        splash.UpdateProgress(80, "화면을 준비하는 중...");
        Application.DoEvents();

        var mainForm = new MainForm();

        splash.UpdateProgress(100, "시작합니다!");
        Application.DoEvents();
        Thread.Sleep(200);

        splash.Close();
        splash.Dispose();

        Application.Run(mainForm);
    }
}
