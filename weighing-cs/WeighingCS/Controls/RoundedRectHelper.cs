using System.Drawing;
using System.Drawing.Drawing2D;

namespace WeighingCS.Controls;

/// <summary>
/// Shared utility for creating rounded-rectangle <see cref="GraphicsPath"/> instances.
/// </summary>
public static class RoundedRectHelper
{
    /// <summary>
    /// Creates a rounded-rectangle path with uniform corner radius.
    /// </summary>
    public static GraphicsPath Create(RectangleF rect, int radius)
    {
        var path = new GraphicsPath();
        float d = radius * 2f;

        if (d > rect.Width) d = rect.Width;
        if (d > rect.Height) d = rect.Height;

        if (d <= 0)
        {
            path.AddRectangle(rect);
            return path;
        }

        path.AddArc(rect.X, rect.Y, d, d, 180, 90);
        path.AddArc(rect.Right - d, rect.Y, d, d, 270, 90);
        path.AddArc(rect.Right - d, rect.Bottom - d, d, d, 0, 90);
        path.AddArc(rect.X, rect.Bottom - d, d, d, 90, 90);
        path.CloseFigure();
        return path;
    }

    /// <summary>
    /// Creates a rounded-rectangle path from integer <see cref="Rectangle"/>.
    /// </summary>
    public static GraphicsPath Create(Rectangle rect, int radius) =>
        Create(new RectangleF(rect.X, rect.Y, rect.Width, rect.Height), radius);
}
