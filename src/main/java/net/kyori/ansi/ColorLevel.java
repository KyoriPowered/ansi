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
      switch (rgbColor) {
        // Default MC colors
        case 0x000000 /* black */: return "30";
        case 0x0000aa /* dark_blue */: return "34";
        case 0x00aa00 /* dark_green */: return "32";
        case 0x00aaaa /* dark_aqua */: return "36";
        case 0xaa0000 /* dark_red */: return "31";
        case 0xaa00aa /* dark_purple */: return "35";
        case 0xffaa00 /* gold */: return "33";
        case 0xaaaaaa /* gray */: return "37";
        case 0x555555 /* dark_gray */: return "90";
        case 0x5555ff /* blue */: return "94";
        case 0x55ff55 /* green */: return "92";
        case 0x55ffff /* aqua */: return "96";
        case 0xff5555 /* red */: return "91";
        case 0xff55ff /* light_purple */: return "95";
        case 0xffff55 /* yellow */: return "93";
        case 0xffffff /* white */: return "97";
        default: return ColorLevel.TRUE_COLOR.determineEscape(rgbColor); // Not a default? just emit RGB ig (TODO downsample)
      }
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
}
