using Newtonsoft.Json;
using WeighingCS.Controls;
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

    // -- History sort state ------------------------------------------------
    private int _lastSortColumn = -1;
    private SortOrder _lastSortOrder = SortOrder.None;

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
        _indicator.ConnectionStateChanged += (_, connected) => InvokeUI(() =>
            connectionBar.SetDeviceStatus(ConnectionStatusPanel.DeviceType.Indicator, connected));

        _display.ConnectionStateChanged += (_, connected) => InvokeUI(() =>
            connectionBar.SetDeviceStatus(ConnectionStatusPanel.DeviceType.Display, connected));
        _display.ErrorOccurred += (_, msg) => InvokeUI(() => AppendLog($"[전광판] {msg}"));

        _barrier.ConnectionStateChanged += (_, connected) => InvokeUI(() =>
            connectionBar.SetDeviceStatus(ConnectionStatusPanel.DeviceType.Barrier, connected));
        _barrier.BarrierStateChanged += (_, open) => InvokeUI(() => AppendLog($"[차단기] {(open ? "열림" : "닫힘")}"));
        _barrier.ErrorOccurred += (_, msg) => InvokeUI(() => AppendLog($"[차단기] {msg}"));

        _api.NetworkStatusChanged += (_, available) => InvokeUI(() =>
        {
            connectionBar.SetDeviceStatus(ConnectionStatusPanel.DeviceType.Network, available);
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
                connectionBar.SetDeviceStatus(ConnectionStatusPanel.DeviceType.Network, loggedIn);
                AppendLog(loggedIn ? "API 인증 완료." : "API 인증 실패.");
            }
        }
        catch (Exception ex)
        {
            AppendLog($"API 로그인 오류: {ex.Message}");
            connectionBar.SetDeviceStatus(ConnectionStatusPanel.DeviceType.Network, false);
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
            connectionBar.SetDeviceStatus(ConnectionStatusPanel.DeviceType.Indicator, false);
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
            connectionBar.SetDeviceStatus(ConnectionStatusPanel.DeviceType.Display, false);
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
            connectionBar.SetDeviceStatus(ConnectionStatusPanel.DeviceType.Barrier, false);
        }
    }

    // -- Event wiring ---------------------------------------------------------

    private void WireEvents()
    {
        modeToggle.ModeChanged += OnModeChanged;
        btnSearch.Click += OnSearchClick;
        btnConfirmWeight.Click += OnConfirmWeightClick;
        btnReWeigh.Click += OnReWeighClick;
        btnReset.Click += OnResetClick;
        btnBarrierOpen.Click += OnBarrierOpenClick;
        txtSearchPlate.TextChanged += OnSearchPlateTextChanged;
        lvHistory.ColumnClick += OnHistoryColumnClick;
        this.Resize += OnFormResize;
    }

    private void OnFormResize(object? sender, EventArgs e)
    {
        // Adjust splitter proportionally
        if (splitMain.Width > 0)
        {
            int targetDistance = (int)(splitMain.Width * 0.35);
            targetDistance = Math.Max(300, Math.Min(500, targetDistance));
            if (Math.Abs(splitMain.SplitterDistance - targetDistance) > 20)
            {
                splitMain.SplitterDistance = targetDistance;
            }
        }
    }

    // -- UI event handlers ----------------------------------------------------

    private void OnModeChanged(object? sender, EventArgs e)
    {
        if (modeToggle.IsAutoMode)
        {
            cardManual.Enabled = false;
            _process?.SwitchMode(WeighingProcessService.WeighingMode.Auto);
            AppendLog("자동 모드로 전환되었습니다.");
        }
        else
        {
            cardManual.Enabled = true;
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
            txtSearchPlate.Focus();
            return;
        }

        if (!IsValidPlateNumber(plate))
        {
            AppendLog($"[경고] 차량번호 형식이 올바르지 않습니다: {plate} (예: 12가1234, 서울12가1234)");
            txtSearchPlate.Focus();
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

    private void OnSearchPlateTextChanged(object? sender, EventArgs e)
    {
        string plate = txtSearchPlate.Text.Trim();
        if (string.IsNullOrEmpty(plate))
        {
            lblPlateValidation.Text = "";
            return;
        }

        if (IsValidPlateNumber(plate))
        {
            lblPlateValidation.Text = "✓ 유효한 형식";
            lblPlateValidation.ForeColor = Theme.Success;
        }
        else
        {
            lblPlateValidation.Text = "형식: 12가1234, 서울12가1234";
            lblPlateValidation.ForeColor = Theme.Warning;
        }
    }

    // -- Service event handlers (thread-safe) ---------------------------------

    private void OnWeightReceived(object? sender, WeightEventArgs e)
    {
        InvokeUI(() =>
        {
            weightDisplay.WeightValue = e.Weight.ToString("F1");

            if (!e.IsStable)
            {
                weightDisplay.Stability = WeightDisplayPanel.StabilityState.Unstable;
            }
        });
    }

    private void OnWeightStabilized(object? sender, WeightEventArgs e)
    {
        InvokeUI(() =>
        {
            weightDisplay.WeightValue = e.Weight.ToString("F1");
            weightDisplay.Stability = WeightDisplayPanel.StabilityState.Stable;

            // Audio alert: system beep for weight stabilization
            System.Media.SystemSounds.Beep.Play();
        });
    }

    private void OnIndicatorError(object? sender, CommunicationErrorEventArgs e)
    {
        InvokeUI(() =>
        {
            AppendLog($"[계량기] {e.Message}");
            weightDisplay.Stability = WeightDisplayPanel.StabilityState.Error;
        });
    }

    private void OnProcessStateChanged(object? sender, ProcessStateChangedEventArgs e)
    {
        InvokeUI(() =>
        {
            // Map process state to step bar
            int step = e.NewState switch
            {
                WeighingProcessService.ProcessState.Idle => 0,
                WeighingProcessService.ProcessState.WaitingSensor or
                WeighingProcessService.ProcessState.LprCapture or
                WeighingProcessService.ProcessState.AiVerification or
                WeighingProcessService.ProcessState.DispatchMatch or
                WeighingProcessService.ProcessState.ManualSelect or
                WeighingProcessService.ProcessState.ManualConfirm => 0,
                WeighingProcessService.ProcessState.Weighing => 1,
                WeighingProcessService.ProcessState.Stabilizing => 2,
                WeighingProcessService.ProcessState.Saving or
                WeighingProcessService.ProcessState.PrintingSlip or
                WeighingProcessService.ProcessState.OpeningBarrier or
                WeighingProcessService.ProcessState.Completed => 3,
                WeighingProcessService.ProcessState.Error => -1,
                _ => 0,
            };

            processStepBar.CurrentStep = step;
            processStepBar.StatusTag = e.NewState.ToString();
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

    private void OnHistoryColumnClick(object? sender, ColumnClickEventArgs e)
    {
        if (e.Column == _lastSortColumn)
        {
            _lastSortOrder = _lastSortOrder == SortOrder.Ascending ? SortOrder.Descending : SortOrder.Ascending;
        }
        else
        {
            _lastSortColumn = e.Column;
            _lastSortOrder = SortOrder.Ascending;
        }

        lvHistory.ListViewItemSorter = new ListViewItemComparer(e.Column, _lastSortOrder);
        lvHistory.Sort();
    }

    private void OnExportCsvClick(object? sender, EventArgs e)
    {
        if (lvHistory.Items.Count == 0)
        {
            AppendLog("내보낼 계량 기록이 없습니다.");
            return;
        }

        using var dialog = new SaveFileDialog();
        dialog.Filter = "CSV 파일 (*.csv)|*.csv";
        dialog.FileName = $"계량기록_{DateTime.Now:yyyyMMdd_HHmmss}.csv";

        if (dialog.ShowDialog() == DialogResult.OK)
        {
            try
            {
                using var writer = new System.IO.StreamWriter(dialog.FileName, false, System.Text.Encoding.UTF8);
                writer.WriteLine("시간,차량번호,중량(kg),모드,상태");

                foreach (ListViewItem item in lvHistory.Items)
                {
                    var values = new string[item.SubItems.Count];
                    for (int i = 0; i < item.SubItems.Count; i++)
                    {
                        values[i] = item.SubItems[i].Text;
                    }
                    writer.WriteLine(string.Join(",", values));
                }

                AppendLog($"CSV 내보내기 완료: {dialog.FileName}");
            }
            catch (Exception ex)
            {
                AppendLog($"CSV 내보내기 실패: {ex.Message}");
            }
        }
    }

    private void OnPrintHistoryClick(object? sender, EventArgs e)
    {
        AppendLog("[정보] 인쇄 기능은 추후 업데이트 예정입니다.");
    }

    private void AppendLog(string message)
    {
        var level = TerminalLogPanel.DetectLevel(message);
        terminalLog.AppendLog(message, level);
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

    private static string? ShowInputDialog(string title, string prompt)
    {
        using var form = new Form();
        form.Text = title;
        form.ClientSize = new Size(400, 180);
        form.StartPosition = FormStartPosition.CenterParent;
        form.FormBorderStyle = FormBorderStyle.FixedDialog;
        form.MaximizeBox = false;
        form.MinimizeBox = false;
        form.BackColor = Theme.BgBase;
        form.ForeColor = Theme.TextPrimary;

        var label = new Label
        {
            Text = prompt,
            Left = 20,
            Top = 20,
            AutoSize = true,
            ForeColor = Theme.TextSecondary,
            Font = Theme.FontBody,
        };

        var textBox = new TextBox
        {
            Left = 20,
            Top = 50,
            Width = 355,
            Height = 32,
            Font = Theme.FontHeading,
            BackColor = Theme.BgElevated,
            ForeColor = Theme.TextPrimary,
            BorderStyle = BorderStyle.FixedSingle,
        };

        var btnOk = new ModernButton
        {
            Text = "확인",
            Left = 190,
            Top = 100,
            Width = 90,
            Height = 36,
            Variant = ModernButton.ButtonVariant.Primary,
            Font = Theme.FontBodyBold,
        };
        btnOk.Click += (_, _) => { form.DialogResult = DialogResult.OK; form.Close(); };

        var btnCancel = new ModernButton
        {
            Text = "취소",
            Left = 290,
            Top = 100,
            Width = 85,
            Height = 36,
            Variant = ModernButton.ButtonVariant.Secondary,
            Font = Theme.FontBody,
        };
        btnCancel.Click += (_, _) => { form.DialogResult = DialogResult.Cancel; form.Close(); };

        form.Controls.AddRange(new Control[] { label, textBox, btnOk, btnCancel });
        form.AcceptButton = null; // ModernButton is not a System.Windows.Forms.Button

        return form.ShowDialog() == DialogResult.OK ? textBox.Text : null;
    }

    /// <summary>
    /// Validates a Korean vehicle plate number format.
    /// Supports formats: 12가1234, 서울12가1234, 123가1234
    /// </summary>
    private static bool IsValidPlateNumber(string plate)
    {
        if (string.IsNullOrWhiteSpace(plate)) return false;
        // Korean plate formats:
        // Standard: 12가1234 (2digits + Korean + 4digits)
        // Regional: 서울12가1234 (region + 2digits + Korean + 4digits)
        // New: 123가1234 (3digits + Korean + 4digits)
        return System.Text.RegularExpressions.Regex.IsMatch(
            plate.Trim(),
            @"^([가-힣]{2})?\d{2,3}[가-힣]\d{4}$"
        );
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

/// <summary>
/// ListView column sorter for history list.
/// </summary>
internal class ListViewItemComparer : System.Collections.IComparer
{
    private readonly int _column;
    private readonly SortOrder _order;

    public ListViewItemComparer(int column, SortOrder order)
    {
        _column = column;
        _order = order;
    }

    public int Compare(object? x, object? y)
    {
        if (x is not ListViewItem itemX || y is not ListViewItem itemY)
            return 0;

        string textX = itemX.SubItems[_column].Text;
        string textY = itemY.SubItems[_column].Text;

        int result;
        // Try numeric comparison for weight column (index 2)
        if (_column == 2 && decimal.TryParse(textX, out decimal numX) && decimal.TryParse(textY, out decimal numY))
        {
            result = numX.CompareTo(numY);
        }
        else
        {
            result = string.Compare(textX, textY, StringComparison.CurrentCulture);
        }

        return _order == SortOrder.Descending ? -result : result;
    }
}
