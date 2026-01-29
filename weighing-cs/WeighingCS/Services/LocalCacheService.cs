using System.Data.SQLite;
using Newtonsoft.Json;
using WeighingCS.Models;

namespace WeighingCS.Services;

/// <summary>
/// FUNC-016: SQLite-based local caching and offline mode service.
/// Detects network failure, caches weighing records locally, and auto-syncs on recovery (FIFO).
/// </summary>
public sealed class LocalCacheService : IDisposable
{
    private const int SyncIntervalMs = 10000; // 10 seconds
    private const int NetworkCheckIntervalMs = 15000; // 15 seconds
    private const int MaxRetryCount = 5;

    private readonly string _dbPath;
    private readonly string _connectionString;
    private readonly ApiService _apiService;

    private CancellationTokenSource? _cts;
    private Task? _syncTask;
    private bool _disposed;

    // -- Events ---------------------------------------------------------------

    public event EventHandler<int>? PendingSyncCountChanged;
    public event EventHandler<string>? SyncError;
    public event EventHandler<WeighingRecord>? RecordSynced;

    // -- Public state ---------------------------------------------------------

    public bool IsOfflineMode => !_apiService.IsNetworkAvailable;

    // -- Constructor -----------------------------------------------------------

    public LocalCacheService(DatabaseConfig dbConfig, ApiService apiService)
    {
        _dbPath = dbConfig?.Path ?? throw new ArgumentNullException(nameof(dbConfig));
        _connectionString = $"Data Source={_dbPath};Version=3;";
        _apiService = apiService ?? throw new ArgumentNullException(nameof(apiService));
    }

    // -- Initialization -------------------------------------------------------

    /// <summary>
    /// Creates the SQLite database and tables if they do not exist.
    /// </summary>
    public async Task InitializeAsync()
    {
        using var conn = new SQLiteConnection(_connectionString);
        await conn.OpenAsync();

        const string sql = @"
            CREATE TABLE IF NOT EXISTS cached_weighings (
                id              INTEGER PRIMARY KEY AUTOINCREMENT,
                weighing_json   TEXT    NOT NULL,
                cached_at       TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
                sync_status     TEXT    NOT NULL DEFAULT 'PENDING',
                retry_count     INTEGER NOT NULL DEFAULT 0,
                last_error      TEXT,
                last_retry_at   TEXT
            );

            CREATE INDEX IF NOT EXISTS idx_cached_weighings_status
                ON cached_weighings(sync_status);

            CREATE INDEX IF NOT EXISTS idx_cached_weighings_cached_at
                ON cached_weighings(cached_at);
        ";

        using var cmd = new SQLiteCommand(sql, conn);
        await cmd.ExecuteNonQueryAsync();
    }

    // -- Cache operations ------------------------------------------------------

    /// <summary>
    /// Stores a weighing record in the local cache for later synchronization.
    /// </summary>
    public async Task CacheRecordAsync(WeighingRecord record)
    {
        record.IsCached = true;
        record.CachedAt = DateTime.Now;

        string json = JsonConvert.SerializeObject(record);

        using var conn = new SQLiteConnection(_connectionString);
        await conn.OpenAsync();

        const string sql = @"
            INSERT INTO cached_weighings (weighing_json, cached_at, sync_status)
            VALUES (@json, @cachedAt, 'PENDING')";

        using var cmd = new SQLiteCommand(sql, conn);
        cmd.Parameters.AddWithValue("@json", json);
        cmd.Parameters.AddWithValue("@cachedAt", record.CachedAt?.ToString("yyyy-MM-dd HH:mm:ss") ?? DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss"));
        await cmd.ExecuteNonQueryAsync();

        int count = await GetPendingCountAsync();
        PendingSyncCountChanged?.Invoke(this, count);
    }

    /// <summary>
    /// Returns the number of records awaiting synchronization.
    /// </summary>
    public async Task<int> GetPendingCountAsync()
    {
        using var conn = new SQLiteConnection(_connectionString);
        await conn.OpenAsync();

        const string sql = "SELECT COUNT(*) FROM cached_weighings WHERE sync_status = 'PENDING'";
        using var cmd = new SQLiteCommand(sql, conn);
        object? result = await cmd.ExecuteScalarAsync();
        return Convert.ToInt32(result);
    }

    /// <summary>
    /// Retrieves all pending records in FIFO order (BR-016).
    /// </summary>
    public async Task<List<(long Id, WeighingRecord Record)>> GetPendingRecordsAsync()
    {
        var results = new List<(long, WeighingRecord)>();

        using var conn = new SQLiteConnection(_connectionString);
        await conn.OpenAsync();

        const string sql = @"
            SELECT id, weighing_json
            FROM cached_weighings
            WHERE sync_status = 'PENDING'
            ORDER BY cached_at ASC";

        using var cmd = new SQLiteCommand(sql, conn);
        using var reader = await cmd.ExecuteReaderAsync();

        while (await reader.ReadAsync())
        {
            long id = reader.GetInt64(0);
            string json = reader.GetString(1);
            var record = JsonConvert.DeserializeObject<WeighingRecord>(json);
            if (record is not null)
            {
                results.Add((id, record));
            }
        }

        return results;
    }

    // -- Sync operations -------------------------------------------------------

    /// <summary>
    /// Starts the background synchronization loop.
    /// </summary>
    public void StartSyncLoop()
    {
        if (_cts is not null) return;

        _cts = new CancellationTokenSource();
        _syncTask = Task.Run(() => SyncLoopAsync(_cts.Token), _cts.Token);
    }

    /// <summary>
    /// Stops the background synchronization loop.
    /// </summary>
    public async Task StopSyncLoopAsync()
    {
        if (_cts is null) return;

        await _cts.CancelAsync();
        if (_syncTask is not null)
        {
            try { await _syncTask; }
            catch (OperationCanceledException) { /* expected */ }
        }

        _cts.Dispose();
        _cts = null;
    }

    private const int BackoffBaseMs = 10000; // 10 seconds
    private const int BackoffMaxMs = 120000; // 2 minutes

    private async Task SyncLoopAsync(CancellationToken ct)
    {
        int consecutiveFailures = 0;

        while (!ct.IsCancellationRequested)
        {
            try
            {
                int delay = consecutiveFailures == 0
                    ? SyncIntervalMs
                    : Math.Min(BackoffBaseMs * (1 << Math.Min(consecutiveFailures - 1, 10)), BackoffMaxMs);

                await Task.Delay(delay, ct);

                if (!_apiService.IsNetworkAvailable)
                {
                    await _apiService.CheckNetworkAsync();
                    if (!_apiService.IsNetworkAvailable)
                    {
                        consecutiveFailures++;
                        continue;
                    }
                }

                int pendingBefore = await GetPendingCountAsync();
                await SyncPendingRecordsAsync();
                int pendingAfter = await GetPendingCountAsync();

                if (pendingAfter < pendingBefore || pendingBefore == 0)
                {
                    consecutiveFailures = 0;
                }
                else
                {
                    consecutiveFailures++;
                }
            }
            catch (OperationCanceledException)
            {
                break;
            }
            catch (Exception ex)
            {
                consecutiveFailures++;
                SyncError?.Invoke(this, $"Sync loop error: {ex.Message}");
            }
        }
    }

    /// <summary>
    /// Attempts to sync all pending records in FIFO order.
    /// Stops on first failure to maintain ordering.
    /// </summary>
    public async Task SyncPendingRecordsAsync()
    {
        var pending = await GetPendingRecordsAsync();

        foreach (var (id, record) in pending)
        {
            try
            {
                var created = await _apiService.CreateWeighingAsync(record);

                if (created is not null)
                {
                    await MarkSyncedAsync(id);
                    RecordSynced?.Invoke(this, created);
                }
                else
                {
                    await MarkRetryAsync(id, "API returned null response");
                    break; // FIFO: stop on failure to preserve ordering
                }
            }
            catch (Exception ex)
            {
                await MarkRetryAsync(id, ex.Message);
                break; // FIFO: stop on failure
            }
        }

        int remaining = await GetPendingCountAsync();
        PendingSyncCountChanged?.Invoke(this, remaining);
    }

    private async Task MarkSyncedAsync(long id)
    {
        using var conn = new SQLiteConnection(_connectionString);
        await conn.OpenAsync();

        const string sql = "UPDATE cached_weighings SET sync_status = 'SYNCED' WHERE id = @id";
        using var cmd = new SQLiteCommand(sql, conn);
        cmd.Parameters.AddWithValue("@id", id);
        await cmd.ExecuteNonQueryAsync();
    }

    private async Task MarkRetryAsync(long id, string error)
    {
        using var conn = new SQLiteConnection(_connectionString);
        await conn.OpenAsync();

        // Check current retry count to decide whether to quarantine.
        const string selectSql = "SELECT retry_count FROM cached_weighings WHERE id = @id";
        using var selectCmd = new SQLiteCommand(selectSql, conn);
        selectCmd.Parameters.AddWithValue("@id", id);
        var currentCount = Convert.ToInt32(await selectCmd.ExecuteScalarAsync());

        if (currentCount + 1 >= MaxRetryCount)
        {
            // Quarantine: max retries reached.
            const string quarantineSql = @"
                UPDATE cached_weighings
                SET sync_status = 'QUARANTINED',
                    retry_count = retry_count + 1,
                    last_error = @error,
                    last_retry_at = datetime('now','localtime')
                WHERE id = @id";

            using var quarantineCmd = new SQLiteCommand(quarantineSql, conn);
            quarantineCmd.Parameters.AddWithValue("@id", id);
            quarantineCmd.Parameters.AddWithValue("@error", error);
            await quarantineCmd.ExecuteNonQueryAsync();

            SyncError?.Invoke(this, $"Cache record {id} quarantined after {MaxRetryCount} retries: {error}");
        }
        else
        {
            const string retrySql = @"
                UPDATE cached_weighings
                SET retry_count = retry_count + 1,
                    last_error = @error,
                    last_retry_at = datetime('now','localtime')
                WHERE id = @id";

            using var retryCmd = new SQLiteCommand(retrySql, conn);
            retryCmd.Parameters.AddWithValue("@id", id);
            retryCmd.Parameters.AddWithValue("@error", error);
            await retryCmd.ExecuteNonQueryAsync();

            SyncError?.Invoke(this, $"Cache record {id} retry failed: {error}");
        }
    }

    // -- Quarantine operations -------------------------------------------------

    /// <summary>
    /// Returns the number of records that have been quarantined after exceeding max retries.
    /// </summary>
    public async Task<int> GetQuarantinedCountAsync()
    {
        using var conn = new SQLiteConnection(_connectionString);
        await conn.OpenAsync();
        using var cmd = new SQLiteCommand("SELECT COUNT(*) FROM cached_weighings WHERE sync_status = 'QUARANTINED'", conn);
        var result = await cmd.ExecuteScalarAsync();
        return Convert.ToInt32(result);
    }

    /// <summary>
    /// Requeues all quarantined records back to PENDING status with reset retry counts.
    /// </summary>
    public async Task RequeueQuarantinedAsync()
    {
        using var conn = new SQLiteConnection(_connectionString);
        await conn.OpenAsync();
        using var cmd = new SQLiteCommand(
            "UPDATE cached_weighings SET sync_status = 'PENDING', retry_count = 0, last_error = NULL WHERE sync_status = 'QUARANTINED'",
            conn);
        await cmd.ExecuteNonQueryAsync();
        SyncError?.Invoke(this, "[Cache] Quarantined records requeued for sync");
    }

    // -- IDisposable -----------------------------------------------------------

    public void Dispose()
    {
        if (_disposed) return;
        _disposed = true;

        _cts?.Cancel();
        _cts?.Dispose();
    }
}
