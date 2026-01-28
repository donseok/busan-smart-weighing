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
        Application.Run(new MainForm());
    }
}
