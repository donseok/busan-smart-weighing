using WeighingCS.Services;

namespace WeighingCS.Tests;

/// <summary>
/// IndicatorService.ParseWeight 정적 메서드 단위 테스트.
/// 다양한 계량기 출력 포맷의 파싱 정확성을 검증.
/// </summary>
public class IndicatorServiceTests
{
    [Theory]
    [InlineData("  1234.5 kg", 1234.5)]
    [InlineData("1234.5", 1234.5)]
    [InlineData("+  1234.5 kg", 1234.5)]
    [InlineData("ST,GS,+001234.5 kg", 1234.5)]
    [InlineData("US,GS,+000050.0 kg", 50.0)]
    [InlineData("NT,+000100.0 kg", 100.0)]
    [InlineData("  0.0 kg", 0.0)]
    public void ParseWeight_ValidFormats_ReturnsCorrectValue(string raw, decimal expected)
    {
        decimal result = IndicatorService.ParseWeight(raw);
        Assert.Equal(expected, result);
    }

    [Theory]
    [InlineData("")]
    [InlineData("   ")]
    [InlineData("invalid")]
    [InlineData("abc kg")]
    public void ParseWeight_InvalidInput_ReturnsZero(string raw)
    {
        decimal result = IndicatorService.ParseWeight(raw);
        Assert.Equal(0m, result);
    }

    [Fact]
    public void ParseWeight_NegativeValue_ReturnsNegative()
    {
        // ParseWeight strips '+' but not '-', so negative values are preserved.
        decimal result = IndicatorService.ParseWeight("-50.0 kg");
        Assert.Equal(-50.0m, result);
    }

    [Fact]
    public void ParseWeight_LargeValue_ReturnsCorrectly()
    {
        decimal result = IndicatorService.ParseWeight("ST,GS,+099999.9 kg");
        Assert.Equal(99999.9m, result);
    }

    [Fact]
    public void ParseWeight_DecimalPrecision_Preserved()
    {
        decimal result = IndicatorService.ParseWeight("  1234.56 kg");
        Assert.Equal(1234.56m, result);
    }
}
