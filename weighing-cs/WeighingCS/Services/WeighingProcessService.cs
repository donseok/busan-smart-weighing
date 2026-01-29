using WeighingCS.Models;

namespace WeighingCS.Services;

/// <summary>
/// FUNC-011/014/015: Main weighing process orchestrator.
/// Coordinates auto-weighing (LPR pipeline), manual weighing, and re-weighing flows.
/// </summary>
public sealed class WeighingProcessService : IDisposable
{
    private readonly IndicatorService _indicator;
    private readonly ApiService _api;
    private readonly DisplayBoardService _display;
    private readonly BarrierService _barrier;
    private readonly LocalCacheService _cache;
    private readonly int _scaleId;

    private bool _disposed;

    // -- Public state ---------------------------------------------------------

    public WeighingMode CurrentMode { get; private set; } = WeighingMode.Auto;
    public ProcessState CurrentState { get; private set; } = ProcessState.Idle;
    public WeighingRecord? ActiveRecord { get; private set; }
    public DispatchInfo? ActiveDispatch { get; private set; }
    public LprCaptureResult? ActiveLprResult { get; private set; }

    // -- Events ---------------------------------------------------------------

    /// <summary>Raised when the process state changes.</summary>
    public event EventHandler<ProcessStateChangedEventArgs>? StateChanged;

    /// <summary>Raised when a weighing transaction completes successfully.</summary>
    public event EventHandler<WeighingRecord>? WeighingCompleted;

    /// <summary>Raised when a process step fails and needs attention.</summary>
    public event EventHandler<ProcessErrorEventArgs>? ProcessError;

    /// <summary>Raised with status messages for the UI log.</summary>
    public event EventHandler<string>? StatusMessage;

    // -- Enums ----------------------------------------------------------------

    public enum WeighingMode
    {
        Auto,
        Manual
    }

    public enum ProcessState
    {
        Idle,
        WaitingSensor,
        LprCapture,
        AiVerification,
        DispatchMatch,
        Weighing,
        Stabilizing,
        Saving,
        PrintingSlip,
        OpeningBarrier,
        Completed,
        Error,
        ManualSelect,
        ManualConfirm
    }

    // -- Constructor -----------------------------------------------------------

    public WeighingProcessService(
        IndicatorService indicator,
        ApiService api,
        DisplayBoardService display,
        BarrierService barrier,
        LocalCacheService cache,
        int scaleId)
    {
        _indicator = indicator ?? throw new ArgumentNullException(nameof(indicator));
        _api = api ?? throw new ArgumentNullException(nameof(api));
        _display = display ?? throw new ArgumentNullException(nameof(display));
        _barrier = barrier ?? throw new ArgumentNullException(nameof(barrier));
        _cache = cache ?? throw new ArgumentNullException(nameof(cache));
        _scaleId = scaleId;
    }

    // -- Mode switching -------------------------------------------------------

    /// <summary>
    /// Switches between auto and manual weighing modes.
    /// Only allowed when the process is idle.
    /// </summary>
    public bool SwitchMode(WeighingMode mode)
    {
        if (CurrentState != ProcessState.Idle && CurrentState != ProcessState.Completed)
        {
            ProcessError?.Invoke(this, new ProcessErrorEventArgs(
                "Cannot switch mode while a weighing process is active.",
                CurrentState));
            return false;
        }

        CurrentMode = mode;
        SetState(ProcessState.Idle);
        StatusMessage?.Invoke(this, $"Mode switched to {mode}.");
        return true;
    }

    // -- Auto weighing flow (FUNC-011 / BR-011) --------------------------------

    /// <summary>
    /// Executes the full auto-weighing pipeline:
    /// sensor detect -> LPR capture -> AI verify -> dispatch match -> weigh -> slip -> barrier open.
    /// Target: 30 seconds end-to-end (BR-011).
    /// </summary>
    public async Task AutoWeighAsync(string plateNumber, double lprConfidence, string? imageUrl = null)
    {
        if (CurrentMode != WeighingMode.Auto)
        {
            ProcessError?.Invoke(this, new ProcessErrorEventArgs("Not in auto mode.", CurrentState));
            return;
        }

        try
        {
            // Step 1: LPR capture registration
            SetState(ProcessState.LprCapture);
            StatusMessage?.Invoke(this, $"LPR captured: {plateNumber} (confidence: {lprConfidence:P0})");

            ActiveLprResult = await _api.RegisterLprCaptureAsync(plateNumber, lprConfidence, imageUrl);
            if (ActiveLprResult is null)
            {
                await HandleAutoWeighFallback("LPR registration failed.");
                return;
            }

            // Step 2: AI verification
            SetState(ProcessState.AiVerification);
            StatusMessage?.Invoke(this, "AI verification in progress...");

            ActiveLprResult = await _api.VerifyAiAsync(ActiveLprResult.CaptureId);
            if (ActiveLprResult is null || ActiveLprResult.VerificationStatus != LprVerificationStatuses.Verified)
            {
                await HandleAutoWeighFallback("AI verification failed or confidence too low.");
                return;
            }

            // Step 3: Dispatch matching
            SetState(ProcessState.DispatchMatch);
            StatusMessage?.Invoke(this, "Matching dispatch...");

            ActiveLprResult = await _api.MatchDispatchAsync(ActiveLprResult.CaptureId);
            if (ActiveLprResult is null || !ActiveLprResult.IsAutoWeighEligible)
            {
                string reason = ActiveLprResult?.MatchResult == LprMatchResults.MultipleMatch
                    ? "Multiple dispatches found. Manual selection required."
                    : "No matching dispatch found.";
                await HandleAutoWeighFallback(reason);
                return;
            }

            ActiveDispatch = ActiveLprResult.Dispatches[0];
            await _display.ShowWeighingAsync(plateNumber);

            // Step 4: Wait for stable weight
            SetState(ProcessState.Weighing);
            StatusMessage?.Invoke(this, $"Weighing vehicle: {plateNumber} / Dispatch: {ActiveDispatch.DispatchId}");

            decimal stableWeight = await WaitForStableWeightAsync(TimeSpan.FromSeconds(30));

            // Step 5: Save weighing record
            await SaveWeighingRecordAsync(stableWeight, WeighingModes.Auto);

            // Step 6: Gate pass and barrier
            await FinalizeWeighingAsync();
        }
        catch (TimeoutException)
        {
            SetState(ProcessState.Error);
            ProcessError?.Invoke(this, new ProcessErrorEventArgs(
                "Weighing timed out waiting for stable weight.", ProcessState.Weighing));
            await _display.DisplayErrorAsync("계량 시간 초과");
        }
        catch (Exception ex)
        {
            SetState(ProcessState.Error);
            ProcessError?.Invoke(this, new ProcessErrorEventArgs(
                $"Auto weighing error: {ex.Message}", CurrentState));
            await _display.DisplayErrorAsync("시스템 오류");
        }
    }

    private async Task HandleAutoWeighFallback(string reason)
    {
        StatusMessage?.Invoke(this, $"Auto-weigh fallback: {reason}");
        ProcessError?.Invoke(this, new ProcessErrorEventArgs(reason, CurrentState));

        await _display.DisplayStatusAsync(DisplayBoardService.Messages.MobileAuthRequired);

        // Switch to manual mode so operator can intervene.
        SetState(ProcessState.Idle);
    }

    // -- Manual weighing flow (FUNC-014 / BR-014) ------------------------------

    /// <summary>
    /// Starts a manual weighing transaction for the selected dispatch.
    /// Operator confirms the weight on the touchscreen.
    /// </summary>
    public async Task ManualWeighAsync(DispatchInfo dispatch)
    {
        if (CurrentMode != WeighingMode.Manual && CurrentState != ProcessState.Idle)
        {
            ProcessError?.Invoke(this, new ProcessErrorEventArgs(
                "Cannot start manual weighing in current state.", CurrentState));
            return;
        }

        try
        {
            ActiveDispatch = dispatch;
            CurrentMode = WeighingMode.Manual;
            SetState(ProcessState.ManualSelect);

            await _display.ShowWeighingAsync(dispatch.PlateNumber);

            string expectedInfo = dispatch.ExpectedWeight.HasValue
                ? $" (예상중량: {dispatch.ExpectedWeight.Value:F1} kg)"
                : "";
            StatusMessage?.Invoke(this, $"Manual weighing: {dispatch.PlateNumber} / Dispatch: {dispatch.DispatchId}{expectedInfo}");

            if (dispatch.ExpectedWeight.HasValue)
            {
                await _display.DisplayStatusAsync($"예상중량: {dispatch.ExpectedWeight.Value:F1} kg");
            }

            SetState(ProcessState.Weighing);
        }
        catch (Exception ex)
        {
            SetState(ProcessState.Error);
            ProcessError?.Invoke(this, new ProcessErrorEventArgs(
                $"Manual weighing error: {ex.Message}", CurrentState));
        }
    }

    /// <summary>
    /// Called when the operator confirms the displayed weight in manual mode.
    /// </summary>
    public async Task ConfirmManualWeightAsync(decimal confirmedWeight)
    {
        if (CurrentState != ProcessState.Weighing && CurrentState != ProcessState.Stabilizing)
        {
            ProcessError?.Invoke(this, new ProcessErrorEventArgs(
                "No active weighing to confirm.", CurrentState));
            return;
        }

        try
        {
            SetState(ProcessState.ManualConfirm);
            StatusMessage?.Invoke(this, $"Operator confirmed weight: {confirmedWeight:F1} kg");

            await SaveWeighingRecordAsync(confirmedWeight, WeighingModes.Manual);
            await FinalizeWeighingAsync();
        }
        catch (Exception ex)
        {
            SetState(ProcessState.Error);
            ProcessError?.Invoke(this, new ProcessErrorEventArgs(
                $"Confirm weight error: {ex.Message}", CurrentState));
        }
    }

    // -- Re-weighing flow (FUNC-015 / BR-015) ----------------------------------

    /// <summary>
    /// Initiates a re-weighing for an existing weighing record.
    /// Marks the original as RE_WEIGHING and creates a new record with RE_WEIGH mode.
    /// </summary>
    public async Task ReWeighAsync(long originalWeighingId, string reason)
    {
        if (CurrentState != ProcessState.Idle && CurrentState != ProcessState.Completed)
        {
            ProcessError?.Invoke(this, new ProcessErrorEventArgs(
                "Cannot start re-weighing while another process is active.", CurrentState));
            return;
        }

        try
        {
            SetState(ProcessState.Weighing);
            StatusMessage?.Invoke(this, $"Re-weighing record {originalWeighingId}: {reason}");

            // Mark original record as RE_WEIGHING.
            var original = await _api.GetWeighingAsync(originalWeighingId);
            if (original is not null)
            {
                original.Status = WeighingStatuses.ReWeighing;
                await _api.UpdateWeighingAsync(originalWeighingId, original);

                // Load dispatch info for display.
                if (original.DispatchId > 0)
                {
                    ActiveDispatch = await _api.GetDispatchAsync(original.DispatchId);
                }
            }

            StatusMessage?.Invoke(this, "Waiting for stable weight for re-weighing...");
            decimal stableWeight = await WaitForStableWeightAsync(TimeSpan.FromSeconds(60));

            // Create new re-weigh record.
            var reWeighRecord = new WeighingRecord
            {
                DispatchId = original?.DispatchId ?? 0,
                ScaleId = _scaleId,
                WeighingMode = WeighingModes.ReWeigh,
                GrossWeight = stableWeight,
                TareWeight = original?.TareWeight ?? 0,
                NetWeight = stableWeight - (original?.TareWeight ?? 0),
                Status = WeighingStatuses.Completed,
                PlateNumber = original?.PlateNumber ?? ActiveDispatch?.PlateNumber,
                VehicleId = original?.VehicleId,
                WeighingDatetime = DateTime.Now,
                ReWeighReason = reason,
                OriginalWeighingId = originalWeighingId
            };

            await SaveOrCacheRecordAsync(reWeighRecord);

            ActiveRecord = reWeighRecord;
            SetState(ProcessState.Completed);
            WeighingCompleted?.Invoke(this, reWeighRecord);
            StatusMessage?.Invoke(this, $"Re-weighing completed: {stableWeight:F1} kg");

            await _display.ShowCompletedAsync(stableWeight);
        }
        catch (TimeoutException)
        {
            SetState(ProcessState.Error);
            ProcessError?.Invoke(this, new ProcessErrorEventArgs(
                "Re-weighing timed out.", ProcessState.Weighing));
        }
        catch (Exception ex)
        {
            SetState(ProcessState.Error);
            ProcessError?.Invoke(this, new ProcessErrorEventArgs(
                $"Re-weighing error: {ex.Message}", CurrentState));
        }
    }

    // -- Shared finalization ---------------------------------------------------

    private async Task SaveWeighingRecordAsync(decimal weight, string mode)
    {
        SetState(ProcessState.Saving);

        decimal tare = ActiveDispatch?.TareWeight ?? 0m;
        ActiveRecord = new WeighingRecord
        {
            DispatchId = ActiveDispatch?.DispatchId ?? 0,
            ScaleId = _scaleId,
            WeighingMode = mode,
            GrossWeight = weight,
            TareWeight = tare,
            NetWeight = weight - tare,
            Status = WeighingStatuses.Completed,
            PlateNumber = ActiveDispatch?.PlateNumber,
            VehicleId = ActiveDispatch?.VehicleId,
            WeighingDatetime = DateTime.Now
        };

        await SaveOrCacheRecordAsync(ActiveRecord);
    }

    private async Task SaveOrCacheRecordAsync(WeighingRecord record)
    {
        try
        {
            var saved = await _api.CreateWeighingAsync(record);
            if (saved is not null)
            {
                record.WeighingId = saved.WeighingId;
                StatusMessage?.Invoke(this, $"Weighing saved (ID: {saved.WeighingId}).");
            }
            else
            {
                // API returned null; cache locally.
                await _cache.CacheRecordAsync(record);
                StatusMessage?.Invoke(this, "Weighing cached locally (API unavailable).");
            }
        }
        catch
        {
            // Network failure; cache locally (FUNC-016).
            await _cache.CacheRecordAsync(record);
            StatusMessage?.Invoke(this, "Weighing cached locally (network error).");
        }
    }

    private const int GatePassMaxRetries = 3;
    private const int GatePassRetryDelayMs = 1000;

    private async Task FinalizeWeighingAsync()
    {
        // Print slip / create gate pass.
        SetState(ProcessState.PrintingSlip);
        StatusMessage?.Invoke(this, "Creating gate pass...");

        if (ActiveRecord?.WeighingId > 0)
        {
            bool gatePassCreated = false;
            for (int attempt = 1; attempt <= GatePassMaxRetries; attempt++)
            {
                try
                {
                    gatePassCreated = await _api.CreateGatePassAsync(ActiveRecord.WeighingId);
                    if (gatePassCreated) break;
                }
                catch (Exception ex)
                {
                    StatusMessage?.Invoke(this, $"Gate pass attempt {attempt}/{GatePassMaxRetries} failed: {ex.Message}");
                }

                if (attempt < GatePassMaxRetries)
                {
                    await Task.Delay(GatePassRetryDelayMs * attempt);
                }
            }

            if (!gatePassCreated)
            {
                StatusMessage?.Invoke(this, "Gate pass creation failed after all retries. Proceeding with barrier.");
            }
        }

        // Open barrier (BR-013).
        SetState(ProcessState.OpeningBarrier);
        StatusMessage?.Invoke(this, "Opening barrier...");
        await _barrier.AutoOpenAsync(vehiclePositionConfirmed: true);

        // Show completed display.
        await _display.ShowCompletedAsync(ActiveRecord?.GrossWeight ?? 0);

        SetState(ProcessState.Completed);
        if (ActiveRecord is not null)
        {
            WeighingCompleted?.Invoke(this, ActiveRecord);
        }
        StatusMessage?.Invoke(this, "Weighing process completed.");
    }

    // -- Weight stabilization helper -------------------------------------------

    /// <summary>
    /// Waits for the indicator to report a stable weight within the given timeout.
    /// </summary>
    private async Task<decimal> WaitForStableWeightAsync(TimeSpan timeout)
    {
        SetState(ProcessState.Stabilizing);

        var tcs = new TaskCompletionSource<decimal>();
        using var cts = new CancellationTokenSource(timeout);

        void OnStabilized(object? sender, WeightEventArgs e)
        {
            tcs.TrySetResult(e.Weight);
        }

        cts.Token.Register(() => tcs.TrySetException(
            new TimeoutException("Weight did not stabilize within the timeout period.")));

        _indicator.WeightStabilized += OnStabilized;
        try
        {
            // If already stable, return immediately.
            if (_indicator.IsStable && _indicator.CurrentWeight > 0)
            {
                return _indicator.CurrentWeight;
            }

            return await tcs.Task;
        }
        finally
        {
            _indicator.WeightStabilized -= OnStabilized;
        }
    }

    // -- Reset ----------------------------------------------------------------

    /// <summary>
    /// Resets the process to idle state, clearing all active data.
    /// </summary>
    public async Task ResetAsync()
    {
        ActiveRecord = null;
        ActiveDispatch = null;
        ActiveLprResult = null;
        _indicator.ResetStability();

        SetState(ProcessState.Idle);
        StatusMessage?.Invoke(this, "Process reset to idle.");

        try
        {
            await _display.ShowWaitingAsync();
        }
        catch
        {
            // Non-critical display error.
        }
    }

    // -- Helpers ---------------------------------------------------------------

    private void SetState(ProcessState newState)
    {
        var oldState = CurrentState;
        CurrentState = newState;
        StateChanged?.Invoke(this, new ProcessStateChangedEventArgs(oldState, newState));
    }

    // -- IDisposable -----------------------------------------------------------

    public void Dispose()
    {
        if (_disposed) return;
        _disposed = true;
        // Services are owned by the form and disposed there.
    }
}

// -- Event arg classes --------------------------------------------------------

public class ProcessStateChangedEventArgs : EventArgs
{
    public WeighingProcessService.ProcessState OldState { get; }
    public WeighingProcessService.ProcessState NewState { get; }

    public ProcessStateChangedEventArgs(
        WeighingProcessService.ProcessState oldState,
        WeighingProcessService.ProcessState newState)
    {
        OldState = oldState;
        NewState = newState;
    }
}

public class ProcessErrorEventArgs : EventArgs
{
    public string Message { get; }
    public WeighingProcessService.ProcessState State { get; }
    public DateTime Timestamp { get; }

    public ProcessErrorEventArgs(string message, WeighingProcessService.ProcessState state)
    {
        Message = message;
        State = state;
        Timestamp = DateTime.Now;
    }
}
