using System.Drawing;

namespace WeighingCS.Controls;

/// <summary>
/// Centralized visual constants for the dark theme UI.
/// </summary>
public static class Theme
{
    // -- Background colors ---------------------------------------------------
    public static readonly Color BgBase = Color.FromArgb(11, 17, 32);        // #0B1120
    public static readonly Color BgSurface = Color.FromArgb(30, 41, 59);     // #1E293B
    public static readonly Color BgElevated = Color.FromArgb(15, 23, 42);    // #0F172A

    // -- Accent / semantic colors -------------------------------------------
    public static readonly Color Primary = Color.FromArgb(6, 182, 212);      // #06B6D4 cyan
    public static readonly Color Success = Color.FromArgb(16, 185, 129);     // #10B981 emerald
    public static readonly Color Warning = Color.FromArgb(245, 158, 11);     // #F59E0B amber
    public static readonly Color Error = Color.FromArgb(244, 63, 94);        // #F43E5E rose
    public static readonly Color Purple = Color.FromArgb(139, 92, 246);      // #8B5CF6

    // -- Text colors --------------------------------------------------------
    public static readonly Color TextPrimary = Color.FromArgb(248, 250, 252);  // #F8FAFC
    public static readonly Color TextSecondary = Color.FromArgb(148, 163, 184);// #94A3B8
    public static readonly Color TextMuted = Color.FromArgb(100, 116, 139);    // #64748B

    // -- Border / divider ---------------------------------------------------
    public static readonly Color Border = Color.FromArgb(51, 65, 85);        // #334155

    // -- Radius -------------------------------------------------------------
    public const int RadiusSmall = 6;
    public const int RadiusMedium = 8;
    public const int RadiusLarge = 12;

    // -- Spacing ------------------------------------------------------------
    public const int SpacingXs = 4;
    public const int SpacingSm = 8;
    public const int SpacingMd = 12;
    public const int SpacingLg = 16;
    public const int SpacingXl = 24;

    // -- Fonts (lazy-created, cached) ---------------------------------------
    private static Font? _fontBody;
    private static Font? _fontBodyBold;
    private static Font? _fontSmall;
    private static Font? _fontSmallBold;
    private static Font? _fontHeading;
    private static Font? _fontMono;
    private static Font? _fontMonoLarge;

    public static Font FontBody => _fontBody ??= new Font("Segoe UI", 9.5F);
    public static Font FontBodyBold => _fontBodyBold ??= new Font("Segoe UI", 9.5F, FontStyle.Bold);
    public static Font FontSmall => _fontSmall ??= new Font("Segoe UI", 8F);
    public static Font FontSmallBold => _fontSmallBold ??= new Font("Segoe UI", 8F, FontStyle.Bold);
    public static Font FontHeading => _fontHeading ??= new Font("Segoe UI", 11F, FontStyle.Bold);
    public static Font FontMono => _fontMono ??= new Font("Consolas", 10F);
    public static Font FontMonoLarge => _fontMonoLarge ??= new Font("Consolas", 48F, FontStyle.Bold);

    // -- Helpers ------------------------------------------------------------

    /// <summary>
    /// Returns a color with modified alpha (0–255).
    /// </summary>
    public static Color WithAlpha(Color c, int alpha) =>
        Color.FromArgb(Math.Clamp(alpha, 0, 255), c.R, c.G, c.B);

    /// <summary>
    /// Lightens a color by the given factor (0..1).
    /// </summary>
    public static Color Lighten(Color c, float factor)
    {
        factor = Math.Clamp(factor, 0f, 1f);
        int r = c.R + (int)((255 - c.R) * factor);
        int g = c.G + (int)((255 - c.G) * factor);
        int b = c.B + (int)((255 - c.B) * factor);
        return Color.FromArgb(c.A, Math.Min(r, 255), Math.Min(g, 255), Math.Min(b, 255));
    }

    /// <summary>
    /// Darkens a color by the given factor (0..1).
    /// </summary>
    public static Color Darken(Color c, float factor)
    {
        factor = Math.Clamp(factor, 0f, 1f);
        int r = (int)(c.R * (1 - factor));
        int g = (int)(c.G * (1 - factor));
        int b = (int)(c.B * (1 - factor));
        return Color.FromArgb(c.A, Math.Max(r, 0), Math.Max(g, 0), Math.Max(b, 0));
    }
}
