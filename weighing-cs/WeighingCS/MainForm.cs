using Newtonsoft.Json;
using WeighingCS.Models;
using WeighingCS.Services;

namespace WeighingCS;

/// <summary>
/// Main application form for the Busan Smart Weighing Client Station.
/// Manages UI updates, event wiring, and service lifecycle.
/// </summary>
public partial class MainForm : Form
{
    // -- Services -------------------------------------------------------------

    private AppSettings _settings = new();
    private IndicatorService? _indicator;
    private ApiService? _api;
    private DisplayBoardService? _display;
    private BarrierService? _barrier;
    private LocalCacheService? _cache;
    private WeighingProcessService? _process;

    // -- Dispatch search results cache ----------------------------------------
    private List<DispatchInfo> _searchResults = new();

    // -- Constructor -----------------------------------------------------------

    public MainForm()
    {
        InitializeComponent();
        WireEvents();
    }

    // -- Form lifecycle -------------------------------------------------------

    protected override async void OnLoad(EventArgs e)
    {
        base.OnLoad(e);

        LoadSettings();
        InitializeServices();

        AppendLog("Busan Smart Weighing CS starting...");
        AppendLog($"Scale ID: {_settings.Scale.ScaleId} | COM: {_settings.Scale.ComPort} | Baud: {_settings.Scale.BaudRate}");

        await InitializeCacheAsync();
        await ConnectServicesAsync();
    }

    protected override async void OnFormClosing(FormClosingEventArgs e)
    {
        base.OnFormClosing(e);

        AppendLog("Shutting down...");

        try
        {
            if (_cache is not null)
            {
                await _cache.StopSyncLoopAsync();
            }

            if (_indicator is not null)
            {
                await _indicator.DisconnectAsync();
            }

            _display?.Disconnect();
            _barrier?.Disconnect();
        }
        catch (Exception ex)
        {
            AppendLog($"Shutdown warning: {ex.Message}");
        }
    }

    // -- Settings -------------------------------------------------------------

    private void LoadSettings()
    {
        try
        {
            string settingsPath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "appsettings.json");

            if (File.Exists(settingsPath))
            {
                string json = File.ReadAllText(settingsPath);
                _settings = JsonConvert.DeserializeObject<AppSettings>(json) ?? new AppSettings();
                AppendLog("Configuration loaded from appsettings.json");
            }
            else
            {
                AppendLog("appsettings.json not found. Using defaults.");
            }
        }
        catch (Exception ex)
        {
            AppendLog($"Failed to load settings: {ex.Message}. Using defaults.");
        }
    }

    // -- Service initialization ------------------------------------------------

    private void InitializeServices()
    {
        _indicator = new IndicatorService(_settings.Scale);
        _api = new ApiService(_settings.Api, _settings.Scale.ScaleId);
        _display = new DisplayBoardService(_settings.DisplayBoard);
        _barrier = new BarrierService(_settings.Barrier);
        _cache = new LocalCacheService(_settings.Database, _api);
        _process = new WeighingProcessService(_indicator, _api, _display, _barrier, _cache, _settings.Scale.ScaleId);

        // Wire service events
        _indicator.WeightReceived += OnWeightReceived;
        _indicator.WeightStabilized += OnWeightStabilized;
        _indicator.CommunicationError += OnIndicatorError;
        _indicator.ConnectionStateChanged += (_, connected) => InvokeUI(() => SetConnectionIndicator(indIndicator, connected));

        _display.ConnectionStateChanged += (_, connected) => InvokeUI(() => SetConnectionIndicator(indDisplay, connected));
        _display.ErrorOccurred += (_, msg) => InvokeUI(() => AppendLog($"[DISPLAY] {msg}"));

        _barrier.ConnectionStateChanged += (_, connected) => InvokeUI(() => SetConnectionIndicator(indBarrier, connected));
        _barrier.BarrierStateChanged += (_, open) => InvokeUI(() => AppendLog($"[BARRIER] {(open ? "OPENED" : "CLOSED")}"));
        _barrier.ErrorOccurred += (_, msg) => InvokeUI(() => AppendLog($"[BARRIER] {msg}"));

        _api.NetworkStatusChanged += (_, available) => InvokeUI(() =>
        {
            SetConnectionIndicator(indNetwork, available);
            AppendLog(available ? "[NETWORK] Connected" : "[NETWORK] Offline - caching enabled");
        });
        _api.ApiError += (_, msg) => InvokeUI(() => AppendLog($"[API] {msg}"));

        _cache.PendingSyncCountChanged += (_, count) => InvokeUI(() => AppendLog($"[CACHE] Pending sync: {count} records"));
        _cache.SyncError += (_, msg) => InvokeUI(() => AppendLog($"[CACHE] {msg}"));
        _cache.RecordSynced += (_, record) => InvokeUI(() => AppendLog($"[CACHE] Synced weighing {record.WeighingId}"));

        _process.StateChanged += OnProcessStateChanged;
        _process.WeighingCompleted += OnWeighingCompleted;
        _process.ProcessError += OnProcessError;
        _process.StatusMessage += (_, msg) => InvokeUI(() => AppendLog($"[PROCESS] {msg}"));
    }

    private async Task InitializeCacheAsync()
    {
        try
        {
            if (_cache is not null)
            {
                await _cache.InitializeAsync();
                _cache.StartSyncLoop();
                AppendLog("Local cache initialized.");
            }
        }
        catch (Exception ex)
        {
            AppendLog($"Cache initialization error: {ex.Message}");
        }
    }

    private async Task ConnectServicesAsync()
    {
        // Authenticate API
        try
        {
            if (_api is not null)
            {
                bool loggedIn = await _api.LoginAsync();
                SetConnectionIndicator(indNetwork, loggedIn);
                AppendLog(loggedIn ? "API authenticated." : "API authentication failed.");
            }
        }
        catch (Exception ex)
        {
            AppendLog($"API login error: {ex.Message}");
            SetConnectionIndicator(indNetwork, false);
        }

        // Connect indicator
        try
        {
            if (_indicator is not null)
            {
                await _indicator.ConnectAsync();
                AppendLog($"Indicator connected on {_settings.Scale.ComPort}.");
            }
        }
        catch (Exception ex)
        {
            AppendLog($"Indicator connection failed: {ex.Message}");
            SetConnectionIndicator(indIndicator, false);
        }

        // Connect display board
        try
        {
            if (_display is not null)
            {
                await _display.ConnectAsync();
                await _display.ShowWaitingAsync();
                AppendLog("Display board connected.");
            }
        }
        catch (Exception ex)
        {
            AppendLog($"Display board connection failed: {ex.Message}");
            SetConnectionIndicator(indDisplay, false);
        }

        // Connect barrier
        try
        {
            if (_barrier is not null)
            {
                await _barrier.ConnectAsync();
                AppendLog("Barrier connected.");
            }
        }
        catch (Exception ex)
        {
            AppendLog($"Barrier connection failed: {ex.Message}");
            SetConnectionIndicator(indBarrier, false);
        }
    }

    // -- Event wiring ---------------------------------------------------------

    private void WireEvents()
    {
        rbAuto.CheckedChanged += OnModeChanged;
        rbManual.CheckedChanged += OnModeChanged;
        btnSearch.Click += OnSearchClick;
        btnConfirmWeight.Click += OnConfirmWeightClick;
        btnReWeigh.Click += OnReWeighClick;
        btnReset.Click += OnResetClick;
        btnBarrierOpen.Click += OnBarrierOpenClick;
    }

    // -- UI event handlers ----------------------------------------------------

    private void OnModeChanged(object? sender, EventArgs e)
    {
        if (rbAuto.Checked)
        {
            grpManual.Enabled = false;
            _process?.SwitchMode(WeighingProcessService.WeighingMode.Auto);
            AppendLog("Switched to AUTO mode.");
        }
        else
        {
            grpManual.Enabled = true;
            _process?.SwitchMode(WeighingProcessService.WeighingMode.Manual);
            AppendLog("Switched to MANUAL mode.");
        }
    }

    private async void OnSearchClick(object? sender, EventArgs e)
    {
        string plate = txtSearchPlate.Text.Trim();
        if (string.IsNullOrEmpty(plate))
        {
            AppendLog("Enter a plate number to search.");
            return;
        }

        try
        {
            btnSearch.Enabled = false;
            AppendLog($"Searching dispatches for plate: {plate}...");

            var dispatches = await _api!.GetDispatchesAsync(plateNumber: plate);
            _searchResults = dispatches ?? new List<DispatchInfo>();

            cboDispatches.Items.Clear();
            foreach (var d in _searchResults)
            {
                cboDispatches.Items.Add($"[{d.DispatchId}] {d.ItemName} - {d.CompanyName}");
            }

            if (_searchResults.Count > 0)
            {
                cboDispatches.SelectedIndex = 0;
                AppendLog($"Found {_searchResults.Count} dispatch(es).");
            }
            else
            {
                AppendLog("No dispatches found for this plate.");
            }
        }
        catch (Exception ex)
        {
            AppendLog($"Search failed: {ex.Message}");
        }
        finally
        {
            btnSearch.Enabled = true;
        }
    }

    private async void OnConfirmWeightClick(object? sender, EventArgs e)
    {
        if (_process is null || _indicator is null) return;

        if (cboDispatches.SelectedIndex < 0 || cboDispatches.SelectedIndex >= _searchResults.Count)
        {
            AppendLog("Select a dispatch first.");
            return;
        }

        var dispatch = _searchResults[cboDispatches.SelectedIndex];

        if (_process.CurrentState == WeighingProcessService.ProcessState.Idle)
        {
            // Start the manual weighing flow.
            await _process.ManualWeighAsync(dispatch);
        }
        else if (_process.CurrentState == WeighingProcessService.ProcessState.Weighing ||
                 _process.CurrentState == WeighingProcessService.ProcessState.Stabilizing)
        {
            // Confirm the current weight.
            decimal weight = _indicator.CurrentWeight;
            if (weight <= 0)
            {
                AppendLog("Cannot confirm: weight is zero or negative.");
                return;
            }

            await _process.ConfirmManualWeightAsync(weight);
        }
    }

    private async void OnReWeighClick(object? sender, EventArgs e)
    {
        if (_process is null) return;

        // Prompt for original weighing ID and reason.
        string? idInput = ShowInputDialog("Re-Weigh", "Enter the original Weighing ID:");
        if (string.IsNullOrWhiteSpace(idInput) || !long.TryParse(idInput, out long originalId))
        {
            AppendLog("Re-weigh cancelled or invalid ID.");
            return;
        }

        string? reason = ShowInputDialog("Re-Weigh Reason", "Enter the reason for re-weighing:");
        if (string.IsNullOrWhiteSpace(reason))
        {
            AppendLog("Re-weigh cancelled: no reason provided.");
            return;
        }

        await _process.ReWeighAsync(originalId, reason);
    }

    private async void OnResetClick(object? sender, EventArgs e)
    {
        if (_process is not null)
        {
            await _process.ResetAsync();
        }

        ClearVehicleInfo();
        AppendLog("Process reset.");
    }

    private async void OnBarrierOpenClick(object? sender, EventArgs e)
    {
        if (_barrier is null) return;

        try
        {
            bool opened = await _barrier.OpenAsync(vehiclePositionConfirmed: true);
            AppendLog(opened ? "Barrier opened manually." : "Barrier open failed.");
        }
        catch (Exception ex)
        {
            AppendLog($"Barrier error: {ex.Message}");
        }
    }

    // -- Service event handlers (thread-safe) ---------------------------------

    private void OnWeightReceived(object? sender, WeightEventArgs e)
    {
        InvokeUI(() =>
        {
            lblWeight.Text = e.Weight.ToString("F1");

            if (!e.IsStable)
            {
                lblStability.Text = "UNSTABLE";
                lblStability.BackColor = Color.Orange;
            }
        });
    }

    private void OnWeightStabilized(object? sender, WeightEventArgs e)
    {
        InvokeUI(() =>
        {
            lblWeight.Text = e.Weight.ToString("F1");
            lblStability.Text = "STABLE";
            lblStability.BackColor = Color.Green;
        });
    }

    private void OnIndicatorError(object? sender, CommunicationErrorEventArgs e)
    {
        InvokeUI(() =>
        {
            AppendLog($"[INDICATOR] {e.Message}");
            lblStability.Text = "ERROR";
            lblStability.BackColor = Color.Red;
        });
    }

    private void OnProcessStateChanged(object? sender, ProcessStateChangedEventArgs e)
    {
        InvokeUI(() =>
        {
            lblProcessState.Text = $"State: {e.NewState}";
            AppendLog($"Process state: {e.OldState} -> {e.NewState}");

            // Update vehicle info when dispatch is set.
            if (_process?.ActiveDispatch is not null)
            {
                UpdateVehicleInfo(_process.ActiveDispatch);
            }
        });
    }

    private void OnWeighingCompleted(object? sender, WeighingRecord record)
    {
        InvokeUI(() =>
        {
            AppendLog($"Weighing completed: {record.GrossWeight:F1} kg | Mode: {record.WeighingMode} | ID: {record.WeighingId}");
            AddHistoryEntry(record);
        });
    }

    private void OnProcessError(object? sender, ProcessErrorEventArgs e)
    {
        InvokeUI(() =>
        {
            AppendLog($"[ERROR] {e.Message} (state: {e.State})");
        });
    }

    // -- UI helpers -----------------------------------------------------------

    private void UpdateVehicleInfo(DispatchInfo dispatch)
    {
        lblPlateValue.Text = dispatch.PlateNumber;
        lblCompanyValue.Text = dispatch.CompanyName ?? dispatch.CompanyId.ToString();
        lblItemValue.Text = $"{dispatch.ItemType} - {dispatch.ItemName}";
        lblDispatchValue.Text = dispatch.DispatchId.ToString();
        lblDriverValue.Text = dispatch.DriverName ?? "-";
    }

    private void ClearVehicleInfo()
    {
        lblPlateValue.Text = "-";
        lblCompanyValue.Text = "-";
        lblItemValue.Text = "-";
        lblDispatchValue.Text = "-";
        lblDriverValue.Text = "-";
    }

    private void SetConnectionIndicator(Panel indicator, bool connected)
    {
        indicator.BackColor = connected ? Color.LimeGreen : Color.Red;
    }

    private void AddHistoryEntry(WeighingRecord record)
    {
        var item = new ListViewItem(record.WeighingDatetime.ToString("HH:mm:ss"));
        item.SubItems.Add(record.PlateNumber ?? "-");
        item.SubItems.Add(record.GrossWeight.ToString("F1"));
        item.SubItems.Add(record.WeighingMode);
        item.SubItems.Add(record.Status);

        lvHistory.Items.Insert(0, item);

        // Keep last 50 entries.
        while (lvHistory.Items.Count > 50)
        {
            lvHistory.Items.RemoveAt(lvHistory.Items.Count - 1);
        }
    }

    private void AppendLog(string message)
    {
        string line = $"[{DateTime.Now:HH:mm:ss}] {message}";

        if (txtLog.InvokeRequired)
        {
            txtLog.BeginInvoke(() => AppendLogInternal(line));
        }
        else
        {
            AppendLogInternal(line);
        }
    }

    private void AppendLogInternal(string line)
    {
        txtLog.AppendText(line + Environment.NewLine);

        // Keep log from growing unbounded.
        if (txtLog.TextLength > 50000)
        {
            txtLog.Text = txtLog.Text[20000..];
            txtLog.SelectionStart = txtLog.TextLength;
            txtLog.ScrollToCaret();
        }
    }

    /// <summary>
    /// Invokes an action on the UI thread if required, ensuring thread safety.
    /// </summary>
    private void InvokeUI(Action action)
    {
        if (IsDisposed) return;

        if (InvokeRequired)
        {
            try
            {
                BeginInvoke(action);
            }
            catch (ObjectDisposedException)
            {
                // Form is closing, ignore.
            }
        }
        else
        {
            action();
        }
    }

    /// <summary>
    /// Shows a simple input dialog and returns the user's input.
    /// </summary>
    private static string? ShowInputDialog(string title, string prompt)
    {
        using var form = new Form();
        form.Text = title;
        form.ClientSize = new Size(350, 130);
        form.StartPosition = FormStartPosition.CenterParent;
        form.FormBorderStyle = FormBorderStyle.FixedDialog;
        form.MaximizeBox = false;
        form.MinimizeBox = false;

        var label = new Label { Text = prompt, Left = 10, Top = 15, AutoSize = true };
        var textBox = new TextBox { Left = 10, Top = 40, Width = 320 };
        var btnOk = new Button { Text = "OK", Left = 160, Top = 80, Width = 80, DialogResult = DialogResult.OK };
        var btnCancel = new Button { Text = "Cancel", Left = 250, Top = 80, Width = 80, DialogResult = DialogResult.Cancel };

        form.Controls.AddRange(new Control[] { label, textBox, btnOk, btnCancel });
        form.AcceptButton = btnOk;
        form.CancelButton = btnCancel;

        return form.ShowDialog() == DialogResult.OK ? textBox.Text : null;
    }

    // -- Service disposal -----------------------------------------------------

    private void DisposeServices()
    {
        _process?.Dispose();
        _cache?.Dispose();
        _barrier?.Dispose();
        _display?.Dispose();
        _indicator?.Dispose();
        _api?.Dispose();
    }
}
