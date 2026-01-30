using System.Drawing;

namespace WeighingCS.Controls;

/// <summary>
/// Centralized visual design tokens with dark/light theme support.
/// Inspired by Tailwind CSS Slate palette for a web-like appearance.
/// </summary>
public static class Theme
{
    // ── Theme state ───────────────────────────────────────────────────
    private static bool _isDarkMode = true;
    public static bool IsDarkMode => _isDarkMode;

    // ── Scale factors ─────────────────────────────────────────────────
    public static readonly float FontScale = 1.5f;
    public static readonly float LayoutScale = 1.25f;

    // ── Theme changed event ───────────────────────────────────────────
    public static event EventHandler? ThemeChanged;

    // ── Background hierarchy (darkest -> lightest) ────────────────────
    public static Color BgDarkest => _isDarkMode
        ? Color.FromArgb(6, 13, 27)      // #060D1B
        : Color.FromArgb(226, 232, 240);  // #E2E8F0

    public static Color BgBase => _isDarkMode
        ? Color.FromArgb(11, 17, 32)      // #0B1120
        : Color.FromArgb(248, 250, 252);  // #F8FAFC

    public static Color BgElevated => _isDarkMode
        ? Color.FromArgb(15, 23, 42)      // #0F172A
        : Color.FromArgb(255, 255, 255);  // #FFFFFF

    public static Color BgSurface => _isDarkMode
        ? Color.FromArgb(30, 41, 59)      // #1E293B
        : Color.FromArgb(241, 245, 249);  // #F1F5F9

    public static Color BgHover => _isDarkMode
        ? Color.FromArgb(40, 53, 72)      // #283548
        : Color.FromArgb(226, 232, 240);  // #E2E8F0

    // ── Accent / semantic colors (same in both modes) ─────────────────
    public static readonly Color Primary = Color.FromArgb(6, 182, 212);         // #06B6D4  cyan
    public static readonly Color PrimaryLight = Color.FromArgb(34, 211, 238);   // #22D3EE
    public static readonly Color PrimaryDark = Color.FromArgb(8, 145, 178);     // #0891B2
    public static readonly Color Success = Color.FromArgb(16, 185, 129);        // #10B981  emerald
    public static readonly Color Warning = Color.FromArgb(245, 158, 11);        // #F59E0B  amber
    public static readonly Color Error = Color.FromArgb(244, 63, 94);           // #F43E5E  rose
    public static readonly Color Purple = Color.FromArgb(139, 92, 246);         // #8B5CF6
    public static readonly Color Blue = Color.FromArgb(59, 130, 246);           // #3B82F6

    // ── Text colors ───────────────────────────────────────────────────
    public static Color TextPrimary => _isDarkMode
        ? Color.FromArgb(248, 250, 252)   // #F8FAFC
        : Color.FromArgb(15, 23, 42);     // #0F172A

    public static Color TextSecondary => _isDarkMode
        ? Color.FromArgb(148, 163, 184)   // #94A3B8
        : Color.FromArgb(71, 85, 105);    // #475569

    public static Color TextMuted => _isDarkMode
        ? Color.FromArgb(100, 116, 139)   // #64748B
        : Color.FromArgb(148, 163, 184);  // #94A3B8

    public static Color TextDisabled => _isDarkMode
        ? Color.FromArgb(71, 85, 105)     // #475569
        : Color.FromArgb(203, 213, 225);  // #CBD5E1

    // ── Border / divider ──────────────────────────────────────────────
    public static Color Border => _isDarkMode
        ? Color.FromArgb(51, 65, 85)      // #334155
        : Color.FromArgb(203, 213, 225);  // #CBD5E1

    public static Color BorderLight => _isDarkMode
        ? Color.FromArgb(71, 85, 105)     // #475569
        : Color.FromArgb(148, 163, 184);  // #94A3B8

    public static Color BorderFocus => Primary;

    // ── Corner radius ─────────────────────────────────────────────────
    public static int RadiusXs => (int)(4 * LayoutScale);
    public static int RadiusSmall => (int)(6 * LayoutScale);
    public static int RadiusMedium => (int)(8 * LayoutScale);
    public static int RadiusLarge => (int)(12 * LayoutScale);
    public static int RadiusXl => (int)(16 * LayoutScale);
    public static int RadiusRound => 999;

    // ── Spacing ───────────────────────────────────────────────────────
    public static int SpacingXs => (int)(4 * LayoutScale);
    public static int SpacingSm => (int)(8 * LayoutScale);
    public static int SpacingMd => (int)(12 * LayoutScale);
    public static int SpacingLg => (int)(16 * LayoutScale);
    public static int SpacingXl => (int)(24 * LayoutScale);
    public static int SpacingXxl => (int)(32 * LayoutScale);

    // ── Layout constants ──────────────────────────────────────────────
    public static int HeaderHeight => (int)(56 * LayoutScale);
    public static int FooterHeight => (int)(32 * LayoutScale);
    public static int InputHeight => (int)(36 * LayoutScale);

    // ── Fonts (lazy-created, cached, with FontScale applied) ──────────
    private static Font? _fontCaption;
    private static Font? _fontSmall;
    private static Font? _fontSmallBold;
    private static Font? _fontBody;
    private static Font? _fontBodyBold;
    private static Font? _fontMedium;
    private static Font? _fontMediumBold;
    private static Font? _fontHeading;
    private static Font? _fontTitle;
    private static Font? _fontMono;
    private static Font? _fontMonoSmall;
    private static Font? _fontMonoLarge;
    private static Font? _fontWeightTitle;
    private static Font? _fontBadge;
    private static Font? _fontUnitLabel;
    private static Font? _fontClockLarge;
    private static Font? _fontLogoText;
    private static Font? _fontEmojiIcon;
    private static Font? _fontStatusTag;

    public static Font FontCaption => _fontCaption ??= new Font("Segoe UI", 7.5F * FontScale);
    public static Font FontSmall => _fontSmall ??= new Font("Segoe UI", 8F * FontScale);
    public static Font FontSmallBold => _fontSmallBold ??= new Font("Segoe UI", 8F * FontScale, FontStyle.Bold);
    public static Font FontBody => _fontBody ??= new Font("Segoe UI", 9.5F * FontScale);
    public static Font FontBodyBold => _fontBodyBold ??= new Font("Segoe UI", 9.5F * FontScale, FontStyle.Bold);
    public static Font FontMedium => _fontMedium ??= new Font("Segoe UI", 10.5F * FontScale);
    public static Font FontMediumBold => _fontMediumBold ??= new Font("Segoe UI", 10.5F * FontScale, FontStyle.Bold);
    public static Font FontHeading => _fontHeading ??= new Font("Segoe UI", 11F * FontScale, FontStyle.Bold);
    public static Font FontTitle => _fontTitle ??= new Font("Segoe UI", 14F * FontScale, FontStyle.Bold);
    public static Font FontMono => _fontMono ??= new Font("Consolas", 10F * FontScale);
    public static Font FontMonoSmall => _fontMonoSmall ??= new Font("Consolas", 8.5F * FontScale);
    public static Font FontMonoLarge => _fontMonoLarge ??= new Font("Consolas", 48F * FontScale, FontStyle.Bold);
    public static Font FontWeightTitle => _fontWeightTitle ??= new Font("Segoe UI", 9F * FontScale, FontStyle.Bold);
    public static Font FontBadge => _fontBadge ??= new Font("Segoe UI", 7.5F * FontScale, FontStyle.Bold);
    public static Font FontUnitLabel => _fontUnitLabel ??= new Font("Segoe UI", 16F * FontScale, FontStyle.Bold);
    public static Font FontClockLarge => _fontClockLarge ??= new Font("Consolas", 13F * FontScale, FontStyle.Bold);
    public static Font FontLogoText => _fontLogoText ??= new Font("Segoe UI", 11F * FontScale, FontStyle.Bold);
    public static Font FontEmojiIcon => _fontEmojiIcon ??= new Font("Segoe UI Emoji", 12F * FontScale);
    public static Font FontStatusTag => _fontStatusTag ??= new Font("Segoe UI", 8F * FontScale, FontStyle.Bold);

    // ── Font cache invalidation ───────────────────────────────────────

    public static void InvalidateFontCache()
    {
        // Do NOT dispose old fonts here — controls may still hold references
        // to them and would crash on Paint with "Parameter is not valid".
        // Let GC collect the old Font objects after controls update their refs.
        _fontCaption = null;
        _fontSmall = null;
        _fontSmallBold = null;
        _fontBody = null;
        _fontBodyBold = null;
        _fontMedium = null;
        _fontMediumBold = null;
        _fontHeading = null;
        _fontTitle = null;
        _fontMono = null;
        _fontMonoSmall = null;
        _fontMonoLarge = null;
        _fontWeightTitle = null;
        _fontBadge = null;
        _fontUnitLabel = null;
        _fontClockLarge = null;
        _fontLogoText = null;
        _fontEmojiIcon = null;
        _fontStatusTag = null;
    }

    // ── Theme toggle ──────────────────────────────────────────────────

    public static void ToggleTheme()
    {
        _isDarkMode = !_isDarkMode;
        InvalidateFontCache();
        SavePreference();
        ThemeChanged?.Invoke(null, EventArgs.Empty);
    }

    // ── Theme persistence ─────────────────────────────────────────────

    private static string PreferenceFilePath =>
        Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "theme.dat");

    public static void SavePreference()
    {
        try
        {
            File.WriteAllText(PreferenceFilePath, _isDarkMode ? "dark" : "light");
        }
        catch
        {
            // Ignore file write errors silently.
        }
    }

    public static void LoadPreference()
    {
        try
        {
            if (File.Exists(PreferenceFilePath))
            {
                string content = File.ReadAllText(PreferenceFilePath).Trim().ToLowerInvariant();
                _isDarkMode = content != "light";
                InvalidateFontCache();
            }
        }
        catch
        {
            // Ignore file read errors, keep default (dark).
        }
    }

    // ── Color helpers ─────────────────────────────────────────────────

    public static Color WithAlpha(Color c, int alpha) =>
        Color.FromArgb(Math.Clamp(alpha, 0, 255), c.R, c.G, c.B);

    public static Color Lighten(Color c, float factor)
    {
        factor = Math.Clamp(factor, 0f, 1f);
        int r = c.R + (int)((255 - c.R) * factor);
        int g = c.G + (int)((255 - c.G) * factor);
        int b = c.B + (int)((255 - c.B) * factor);
        return Color.FromArgb(c.A, Math.Min(r, 255), Math.Min(g, 255), Math.Min(b, 255));
    }

    public static Color Darken(Color c, float factor)
    {
        factor = Math.Clamp(factor, 0f, 1f);
        int r = (int)(c.R * (1 - factor));
        int g = (int)(c.G * (1 - factor));
        int b = (int)(c.B * (1 - factor));
        return Color.FromArgb(c.A, Math.Max(r, 0), Math.Max(g, 0), Math.Max(b, 0));
    }

    public static Color Blend(Color c1, Color c2, float amount)
    {
        amount = Math.Clamp(amount, 0f, 1f);
        int r = (int)(c1.R + (c2.R - c1.R) * amount);
        int g = (int)(c1.G + (c2.G - c1.G) * amount);
        int b = (int)(c1.B + (c2.B - c1.B) * amount);
        return Color.FromArgb(Math.Clamp(r, 0, 255), Math.Clamp(g, 0, 255), Math.Clamp(b, 0, 255));
    }
}
