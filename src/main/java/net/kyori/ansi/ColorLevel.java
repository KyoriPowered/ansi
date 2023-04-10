/*
 * This file is part of ansi, licensed under the MIT License.
 *
 * Copyright (c) 2021-2023 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.kyori.ansi;

import org.jetbrains.annotations.NotNull;

/**
 * Supported color levels.
 *
 * @since 1.0.0
 */
public enum ColorLevel {
  /**
   * Full RGB color.
   *
   * <p>Not supported by some terminals</p>
   *
   * @since 1.0.0
   */
  TRUE_COLOR {
    @Override
    public @NotNull String determineEscape(final int rgb) {
      return "38;2;" + ((rgb >> 16) & 0xff) + ';' + ((rgb >> 8) & 0xff) + ';' + (rgb & 0xff);
    }
  },
  /**
   * 256 color.
   *
   * @since 1.0.0
   */
  INDEXED_256 {
    @Override
    public @NotNull String determineEscape(final int rgbColor) {
      // REFERENCES
      // https://gitlab.gnome.org/GNOME/vte/-/blob/19acc51708d9e75ef2b314aa026467570e0bd8ee/src/vte.cc#L2485
      // https://en.wikipedia.org/wiki/ANSI_escape_code#8-bit
      if (indexed256ColorTable == null) {
        indexed256ColorTable = new int[256];
        // No cache primed, build the table!
        for (int i = 0; i < indexed256ColorTable.length; i++) {
          int red;
          int green;
          int blue;

          if (i < 16) {
            // The 16 "standard" colors, same as INDEXED_16
            blue = (i & 4) != 0 ? 0xc000 : 0;
            green = (i & 2) != 0 ? 0xc000 : 0;
            red = (i & 1) != 0 ? 0xc000 : 0;
            if (i > 7) {
              blue += 0x3fff;
              green += 0x3fff;
              red += 0x3fff;
            }
          } else if (i < 232) {
            // 216 color palette, forming a 6x6x6 color cube.
            int j = i - 16;
            int r = j / 36;
            int g = (j / 6) % 6;
            int b = j % 6;
            red = (r == 0) ? 0 : r * 40 + 55;
            green = (g == 0) ? 0 : g * 40 + 55;
            blue = (b == 0) ? 0 : b * 40 + 55;
          } else {
            // 24 grayscale colors.
            int grayscale = 8 + (i - 232) * 10;
            red = grayscale;
            green = grayscale;
            blue = grayscale;
          }

          indexed256ColorTable[i] = (red << 16) | (green << 8) | blue;
        }
      }

      // Find the color index closest to the current color. This uses a simple Manhattan distance from the 3 RGB color
      // components, since there should be enough colors for this to be accurate enough, and it's fast.
      int idx = -1;
      int bestDistance = Integer.MAX_VALUE;
      int r1 = (rgbColor >> 16) & 0xff;
      int g1 = (rgbColor >> 8) & 0xff;
      int b1 = rgbColor & 0xff;
      for (int i = 0; i < indexed256ColorTable.length; i++) {
        int r2 = indexed256ColorTable[i] >> 16 & 0xff;
        int g2 = indexed256ColorTable[i] >> 8 & 0xff;
        int b2 = indexed256ColorTable[i] & 0xff;
        int distance = Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
        if (distance < bestDistance) {
          bestDistance = distance;
          idx = i;
        }
        if (distance == 0) {
          break; // It's an exact match, so no need to check any other entries
        }
      }

      return "38;5;" + idx;
    }
  },
  /**
   * 16 color.
   *
   * <p>This should be supported most everywhere.</p>
   *
   * @since 1.0.0
   */
  INDEXED_16 {
    @Override
    public @NotNull String determineEscape(final int rgbColor) {
      float matchedDistance = Float.MAX_VALUE;
      StandardColor match = StandardColor.BLACK;
      for (StandardColor potential : StandardColor.values()) {
        final float distance = HSV.fromRGB(rgbColor).distance(HSV.fromRGB(potential.color));
        if (distance < matchedDistance) {
          match = potential;
          matchedDistance = distance;
        }
        if (distance == 0) {
          break; // same colour! whoo!
        }
      }
      return match.index;
    }
  };

  private static final String COLORTERM = System.getenv("COLORTERM");
  private static final String TERM = System.getenv("TERM");
  private static int[] indexed256ColorTable = null;

  /**
   * Attempt to estimate the supported color level of the current terminal using the active environment
   *
   * @return the estimated color level
   * @since 1.0.0
   */
  public static @NotNull ColorLevel compute() {
    // TODO
    // https://github.com/termstandard/colors
    // TODO: Add our own system property for override
    // See https://github.com/Minecrell/TerminalConsoleAppender/#supported-environments for other system properties to be aware of
    if (COLORTERM != null && (COLORTERM.equals("truecolor") || COLORTERM.equals("24bit"))) {
      return ColorLevel.TRUE_COLOR;
    } else if (TERM != null && TERM.contains("256color")) {
      return ColorLevel.INDEXED_256;
    }

    // Fallback
    return ColorLevel.INDEXED_16;
  }

  /**
   * Provide an ANSI escape sequence for the provided color.
   *
   * <p>The leading {@code \e[} and trailing {@code m} should not be provided. Those will be added by the renderer.</p>
   *
   * @param rgbColor the packed RGB value of the color
   * @return an appropriate escape sequence for the color
   * @since 1.0.0
   */
  public abstract @NotNull String determineEscape(final int rgbColor);

  private enum StandardColor {
    BLACK(0x00_00_00, "30"),
    DARK_BLUE(0x00_00_aa, "34"),
    DARK_GREEN(0x00_aa_00, "32"),
    DARK_AQUA(0x00_aa_aa, "36"),
    DARK_RED(0xaa_00_00, "31"),
    DARK_PURPLE(0xaa_00_aa, "35"),
    GOLD(0xff_aa_00, "33"),
    GRAY(0xaa_aa_aa, "37"),
    DARK_GRAY(0x55_55_55, "90"),
    BLUE(0x55_55_ff, "94"),
    GREEN(0x55_ff_55, "92"),
    AQUA(0x55_ff_ff, "96"),
    RED(0xff_55_55, "91"),
    LIGHT_PURPLE(0xff_55_ff, "95"),
    YELLOW(0xff_ff_55, "93"),
    WHITE(0xff_ff_ff, "97");

    final int color;
    final String index;

    StandardColor(int color, String index) {
      this.color = color;
      this.index = index;
    }
  }

  private static class HSV {
    float h;
    float s;
    float v;

    HSV(float h, float s, float v) {
      this.h = h;
      this.s = s;
      this.v = v;
    }

    private float distance(final @NotNull HSV other) {
      // Copied from https://github.com/KyoriPowered/adventure/blob/a7e7bc68c684fb4d332ec9fbbb27e12f871bcfa8/api/src/main/java/net/kyori/adventure/text/format/NamedTextColor.java#L267
      // weight hue more heavily than saturation and brightness. kind of magic numbers, but is fine for our use case of downsampling to a set of colors
      final float hueDistance = 3 * Math.min(Math.abs(this.h - other.h), 1f - Math.abs(this.h - other.h));
      final float saturationDiff = this.s - other.s;
      final float valueDiff = this.v - other.v;
      return hueDistance * hueDistance + saturationDiff * saturationDiff + valueDiff * valueDiff;
    }

    private static HSV fromRGB(int rgb) {
      // Copied from https://github.com/KyoriPowered/adventure/blob/a7e7bc68c684fb4d332ec9fbbb27e12f871bcfa8/api/src/main/java/net/kyori/adventure/util/HSVLike.java#L79
      final float r = ((rgb >> 16) & 0xff) / 255.0f;
      final float g = ((rgb >> 8) & 0xff) / 255.0f;
      final float b = (rgb & 0xff) / 255.0f;

      final float min = Math.min(r, Math.min(g, b));
      final float max = Math.max(r, Math.max(g, b)); // v
      final float delta = max - min;

      final float s;
      if (max != 0) {
        s = delta / max; // s
      } else {
        // r = g = b = 0
        s = 0;
      }
      if (s == 0) { // s = 0, h is undefined
        return new HSV(0, s, max);
      }

      float h;
      if (r == max) {
        h = (g - b) / delta; // between yellow & magenta
      } else if (g == max) {
        h = 2 + (b - r) / delta; // between cyan & yellow
      } else {
        h = 4 + (r - g) / delta; // between magenta & cyan
      }
      h *= 60; // degrees
      if (h < 0) {
        h += 360;
      }

      return new HSV(h / 360.0f, s, max);
    }
  }
}
