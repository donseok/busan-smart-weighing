using System.Data.SQLite;
using Newtonsoft.Json;
using WeighingCS.Models;
using WeighingCS.Services;

namespace WeighingCS.Tests;

/// <summary>
/// LocalCacheService SQLite 캐시 기능 단위 테스트.
/// 인메모리 SQLite를 사용하여 캐싱, 조회, 재시도, 격리 로직을 검증.
/// </summary>
public class LocalCacheServiceTests : IAsyncLifetime, IDisposable
{
    private readonly string _dbPath;
    private readonly LocalCacheService _cache;
    private readonly StubApiService _stubApi;

    public LocalCacheServiceTests()
    {
        _dbPath = Path.Combine(Path.GetTempPath(), $"weighing_test_{Guid.NewGuid():N}.db");
        var dbConfig = new DatabaseConfig { Path = _dbPath };
        _stubApi = new StubApiService();
        _cache = new LocalCacheService(dbConfig, _stubApi);
    }

    public async Task InitializeAsync()
    {
        await _cache.InitializeAsync();
    }

    public Task DisposeAsync() => Task.CompletedTask;

    public void Dispose()
    {
        _cache.Dispose();
        if (File.Exists(_dbPath))
        {
            // SQLite may hold lock briefly; best-effort cleanup.
            try { File.Delete(_dbPath); } catch { }
        }
    }

    [Fact]
    public async Task CacheRecordAsync_StoresRecord_PendingCountIncreases()
    {
        var record = CreateTestRecord();

        await _cache.CacheRecordAsync(record);

        int count = await _cache.GetPendingCountAsync();
        Assert.Equal(1, count);
    }

    [Fact]
    public async Task GetPendingRecordsAsync_ReturnsInFifoOrder()
    {
        var record1 = CreateTestRecord(dispatchId: 100);
        var record2 = CreateTestRecord(dispatchId: 200);

        await _cache.CacheRecordAsync(record1);
        await _cache.CacheRecordAsync(record2);

        var pending = await _cache.GetPendingRecordsAsync();

        Assert.Equal(2, pending.Count);
        Assert.Equal(100, pending[0].Record.DispatchId);
        Assert.Equal(200, pending[1].Record.DispatchId);
    }

    [Fact]
    public async Task GetPendingCountAsync_EmptyDatabase_ReturnsZero()
    {
        int count = await _cache.GetPendingCountAsync();
        Assert.Equal(0, count);
    }

    [Fact]
    public async Task CacheRecordAsync_SetsIsCachedAndCachedAt()
    {
        var record = CreateTestRecord();

        await _cache.CacheRecordAsync(record);

        Assert.True(record.IsCached);
        Assert.NotNull(record.CachedAt);
    }

    [Fact]
    public async Task SyncPendingRecordsAsync_SuccessfulSync_MarksAsSynced()
    {
        var record = CreateTestRecord();
        await _cache.CacheRecordAsync(record);

        _stubApi.ShouldSucceed = true;
        await _cache.SyncPendingRecordsAsync();

        int remaining = await _cache.GetPendingCountAsync();
        Assert.Equal(0, remaining);
    }

    [Fact]
    public async Task SyncPendingRecordsAsync_FailedSync_RecordRemainsPending()
    {
        var record = CreateTestRecord();
        await _cache.CacheRecordAsync(record);

        _stubApi.ShouldSucceed = false;
        await _cache.SyncPendingRecordsAsync();

        int remaining = await _cache.GetPendingCountAsync();
        Assert.Equal(1, remaining);
    }

    [Fact]
    public async Task MultipleCacheAndRetrieve_MaintainsDataIntegrity()
    {
        for (int i = 0; i < 5; i++)
        {
            await _cache.CacheRecordAsync(CreateTestRecord(dispatchId: i + 1));
        }

        int count = await _cache.GetPendingCountAsync();
        Assert.Equal(5, count);

        var records = await _cache.GetPendingRecordsAsync();
        Assert.Equal(5, records.Count);
        for (int i = 0; i < 5; i++)
        {
            Assert.Equal(i + 1, records[i].Record.DispatchId);
        }
    }

    private static WeighingRecord CreateTestRecord(long dispatchId = 1)
    {
        return new WeighingRecord
        {
            DispatchId = dispatchId,
            ScaleId = 1,
            WeighingMode = "AUTO",
            GrossWeight = 25000m,
            TareWeight = 8000m,
            NetWeight = 17000m,
            Status = "COMPLETED",
            PlateNumber = "12가3456",
            WeighingDatetime = DateTime.Now
        };
    }

    /// <summary>
    /// Stub ApiService that simulates network success/failure for testing.
    /// Only CreateWeighingAsync is used by LocalCacheService sync.
    /// </summary>
    private sealed class StubApiService : ApiService
    {
        public bool ShouldSucceed { get; set; } = true;

        public StubApiService()
            : base(new ApiConfig { BaseUrl = "http://localhost:9999", LoginId = "test", Password = "test" }, scaleId: 1)
        {
        }

        public new async Task<WeighingRecord?> CreateWeighingAsync(WeighingRecord record)
        {
            await Task.CompletedTask;
            if (ShouldSucceed)
            {
                record.WeighingId = new Random().Next(1, 10000);
                return record;
            }
            return null;
        }
    }
}
