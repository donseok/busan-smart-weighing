using Newtonsoft.Json;
using WeighingCS.Interfaces;
using WeighingCS.Models;
using WeighingCS.Services;
using WeighingCS.Simulators;

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

    // -- Simulators -----------------------------------------------------------

    private LprCameraSimulator? _lprSimulator;
    private VehicleSensorSimulator? _sensorSimulator;
    private VehicleDetectorSimulator? _detectorSimulator;

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

        AppendLog("부산 스마트 계량 시스템 시작 중...");
        AppendLog($"계량대 ID: {_settings.Scale.ScaleId} | COM: {_settings.Scale.ComPort} | 전송속도: {_settings.Scale.BaudRate}");

        InitializeSimulators();
        await InitializeCacheAsync();
        await ConnectServicesAsync();
    }

    /// <remarks>
    /// Service disposal is handled by <see cref="DisposeServices"/> called from
    /// <c>MainForm.Designer.cs</c> Dispose(bool) to ensure deterministic cleanup
    /// of serial ports, TCP connections, and HTTP clients.
    /// </remarks>
    protected override async void OnFormClosing(FormClosingEventArgs e)
    {
        base.OnFormClosing(e);

        AppendLog("시스템 종료 중...");

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
            AppendLog($"종료 경고: {ex.Message}");
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
                AppendLog("appsettings.json에서 설정을 불러왔습니다.");
            }
            else
            {
                AppendLog("appsettings.json 파일을 찾을 수 없습니다. 기본값 사용.");
            }
        }
        catch (Exception ex)
        {
            AppendLog($"설정 불러오기 실패: {ex.Message}. 기본값 사용.");
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
        _display.ErrorOccurred += (_, msg) => InvokeUI(() => AppendLog($"[전광판] {msg}"));

        _barrier.ConnectionStateChanged += (_, connected) => InvokeUI(() => SetConnectionIndicator(indBarrier, connected));
        _barrier.BarrierStateChanged += (_, open) => InvokeUI(() => AppendLog($"[차단기] {(open ? "열림" : "닫힘")}"));
        _barrier.ErrorOccurred += (_, msg) => InvokeUI(() => AppendLog($"[차단기] {msg}"));

        _api.NetworkStatusChanged += (_, available) => InvokeUI(() =>
        {
            SetConnectionIndicator(indNetwork, available);
            AppendLog(available ? "[네트워크] 연결됨" : "[네트워크] 오프라인 - 캐시 활성화");
        });
        _api.ApiError += (_, msg) => InvokeUI(() => AppendLog($"[API 오류] {msg}"));

        _cache.PendingSyncCountChanged += (_, count) => InvokeUI(() => AppendLog($"[캐시] 동기화 대기: {count}건"));
        _cache.SyncError += (_, msg) => InvokeUI(() => AppendLog($"[캐시] {msg}"));
        _cache.RecordSynced += (_, record) => InvokeUI(() => AppendLog($"[캐시] 계량 동기화 완료 {record.WeighingId}"));

        _process.StateChanged += OnProcessStateChanged;
        _process.WeighingCompleted += OnWeighingCompleted;
        _process.ProcessError += OnProcessError;
        _process.StatusMessage += (_, msg) => InvokeUI(() => AppendLog($"[프로세스] {msg}"));
    }

    // -- Simulator initialization ----------------------------------------------

    private void InitializeSimulators()
    {
        _lprSimulator = new LprCameraSimulator();
        _sensorSimulator = new VehicleSensorSimulator();
        _detectorSimulator = new VehicleDetectorSimulator();

        // Wire simulator pipeline: sensor detection -> LPR capture -> auto weigh
        _sensorSimulator.VehicleDetected += async (s, e) =>
        {
            InvokeUI(() => AppendLog($"[시뮬] 센서 {e.SensorId}에서 차량 감지 ({e.DetectedAt:HH:mm:ss})"));

            if (_lprSimulator is not null && _process is not null)
            {
                var lpr = await _lprSimulator.CaptureAsync();
                InvokeUI(() => AppendLog($"[시뮬] LPR 촬영: {lpr.PlateNumber} (신뢰도: {lpr.Confidence:P1})"));
                await _process.AutoWeighAsync(lpr.PlateNumber, lpr.Confidence, lpr.CaptureImageUrl);
            }
        };

        _detectorSimulator.PositionChanged += (s, e) =>
        {
            InvokeUI(() => AppendLog($"[시뮬] 차량 위치: {(e.IsInPosition ? "정위치" : "미정위치")}"));
        };

        _lprSimulator.PlateCaptured += (s, e) =>
        {
            InvokeUI(() => AppendLog($"[시뮬] LPR 번호판 이벤트: {e.PlateNumber} (신뢰도: {e.Confidence:P1})"));
        };

        // Wire simulator button events
        chkSimulatorMode.CheckedChanged += OnSimulatorModeChanged;
        btnSimSensor.Click += OnSimSensorClick;
        btnSimLpr.Click += OnSimLprClick;
        btnSimPosition.Click += OnSimPositionClick;
        btnSyncNow.Click += OnSyncNowClick;
    }

    private void OnSimulatorModeChanged(object? sender, EventArgs e)
    {
        bool enabled = chkSimulatorMode.Checked;
        btnSimSensor.Enabled = enabled;
        btnSimLpr.Enabled = enabled;
        btnSimPosition.Enabled = enabled;
        btnSyncNow.Enabled = enabled;
        AppendLog(enabled ? "[시뮬] 시뮬레이터 모드 활성화" : "[시뮬] 시뮬레이터 모드 비활성화");
    }

    private void OnSimSensorClick(object? sender, EventArgs e)
    {
        _sensorSimulator?.TriggerDetection();
    }

    private async void OnSimLprClick(object? sender, EventArgs e)
    {
        if (_lprSimulator is null) return;

        var result = await _lprSimulator.CaptureAsync();
        AppendLog($"[시뮬] 수동 LPR 촬영: {result.PlateNumber} (신뢰도: {result.Confidence:P1})");
    }

    private void OnSimPositionClick(object? sender, EventArgs e)
    {
        _detectorSimulator?.TogglePosition();
    }

    private async void OnSyncNowClick(object? sender, EventArgs e)
    {
        if (_cache is null) return;

        AppendLog("[캐시] 수동 동기화 시작...");
        await _cache.SyncPendingRecordsAsync();
    }

    private async Task InitializeCacheAsync()
    {
        try
        {
            if (_cache is not null)
            {
                await _cache.InitializeAsync();
                _cache.StartSyncLoop();
                AppendLog("로컬 캐시 초기화 완료.");
            }
        }
        catch (Exception ex)
        {
            AppendLog($"캐시 초기화 오류: {ex.Message}");
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
                AppendLog(loggedIn ? "API 인증 완료." : "API 인증 실패.");
            }
        }
        catch (Exception ex)
        {
            AppendLog($"API 로그인 오류: {ex.Message}");
            SetConnectionIndicator(indNetwork, false);
        }

        // Connect indicator
        try
        {
            if (_indicator is not null)
            {
                await _indicator.ConnectAsync();
                AppendLog($"계량기 연결 완료 ({_settings.Scale.ComPort}).");
            }
        }
        catch (Exception ex)
        {
            AppendLog($"계량기 연결 실패: {ex.Message}");
            SetConnectionIndicator(indIndicator, false);
        }

        // Connect display board
        try
        {
            if (_display is not null)
            {
                await _display.ConnectAsync();
                await _display.ShowWaitingAsync();
                AppendLog("전광판 연결 완료.");
            }
        }
        catch (Exception ex)
        {
            AppendLog($"전광판 연결 실패: {ex.Message}");
            SetConnectionIndicator(indDisplay, false);
        }

        // Connect barrier
        try
        {
            if (_barrier is not null)
            {
                await _barrier.ConnectAsync();
                AppendLog("차단기 연결 완료.");
            }
        }
        catch (Exception ex)
        {
            AppendLog($"차단기 연결 실패: {ex.Message}");
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
            AppendLog("자동 모드로 전환되었습니다.");
        }
        else
        {
            grpManual.Enabled = true;
            _process?.SwitchMode(WeighingProcessService.WeighingMode.Manual);
            AppendLog("수동 모드로 전환되었습니다.");
        }
    }

    private async void OnSearchClick(object? sender, EventArgs e)
    {
        string plate = txtSearchPlate.Text.Trim();
        if (string.IsNullOrEmpty(plate))
        {
            AppendLog("검색할 차량번호를 입력하세요.");
            return;
        }

        try
        {
            btnSearch.Enabled = false;
            AppendLog($"차량번호 {plate} 배차 검색 중...");

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
                AppendLog($"{_searchResults.Count}건의 배차를 찾았습니다.");
            }
            else
            {
                AppendLog("해당 차량번호의 배차 정보가 없습니다.");
            }
        }
        catch (Exception ex)
        {
            AppendLog($"검색 실패: {ex.Message}");
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
            AppendLog("먼저 배차를 선택하세요.");
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
                AppendLog("확인 불가: 중량이 0 이하입니다.");
                return;
            }

            await _process.ConfirmManualWeightAsync(weight);
        }
    }

    private async void OnReWeighClick(object? sender, EventArgs e)
    {
        if (_process is null) return;

        // Prompt for original weighing ID and reason.
        string? idInput = ShowInputDialog("재계량", "원래 계량 ID를 입력하세요:");
        if (string.IsNullOrWhiteSpace(idInput) || !long.TryParse(idInput, out long originalId))
        {
            AppendLog("재계량이 취소되었거나 잘못된 ID입니다.");
            return;
        }

        string? reason = ShowInputDialog("재계량 사유", "재계량 사유를 입력하세요:");
        if (string.IsNullOrWhiteSpace(reason))
        {
            AppendLog("재계량 취소: 사유가 입력되지 않았습니다.");
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
        AppendLog("프로세스가 초기화되었습니다.");
    }

    private async void OnBarrierOpenClick(object? sender, EventArgs e)
    {
        if (_barrier is null) return;

        try
        {
            bool opened = await _barrier.OpenAsync(vehiclePositionConfirmed: true);
            AppendLog(opened ? "차단기를 수동으로 열었습니다." : "차단기 열기 실패.");
        }
        catch (Exception ex)
        {
            AppendLog($"차단기 오류: {ex.Message}");
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
                lblStability.Text = "불안정";
                lblStability.BackColor = Color.Orange;
            }
        });
    }

    private void OnWeightStabilized(object? sender, WeightEventArgs e)
    {
        InvokeUI(() =>
        {
            lblWeight.Text = e.Weight.ToString("F1");
            lblStability.Text = "안정";
            lblStability.BackColor = Color.Green;
        });
    }

    private void OnIndicatorError(object? sender, CommunicationErrorEventArgs e)
    {
        InvokeUI(() =>
        {
            AppendLog($"[계량기] {e.Message}");
            lblStability.Text = "오류";
            lblStability.BackColor = Color.Red;
        });
    }

    private void OnProcessStateChanged(object? sender, ProcessStateChangedEventArgs e)
    {
        InvokeUI(() =>
        {
            lblProcessState.Text = $"상태: {e.NewState}";
            AppendLog($"프로세스 상태: {e.OldState} -> {e.NewState}");

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
            AppendLog($"계량 완료: {record.GrossWeight:F1} kg | 모드: {record.WeighingMode} | ID: {record.WeighingId}");
            AddHistoryEntry(record);
        });
    }

    private void OnProcessError(object? sender, ProcessErrorEventArgs e)
    {
        InvokeUI(() =>
        {
            AppendLog($"[오류] {e.Message} (상태: {e.State})");
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
        var btnOk = new Button { Text = "확인", Left = 160, Top = 80, Width = 80, DialogResult = DialogResult.OK };
        var btnCancel = new Button { Text = "취소", Left = 250, Top = 80, Width = 80, DialogResult = DialogResult.Cancel };

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
