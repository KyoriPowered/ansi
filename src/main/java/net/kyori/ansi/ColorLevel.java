/*
 * This file is part of ansi, licensed under the MIT License.
 *
 * Copyright (c) 2021-2022 KyoriPowered
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
      // https://gitlab.gnome.org/GNOME/vte/-/blob/master/src/vte.cc#L2419
      // https://en.wikipedia.org/wiki/ANSI_escape_code#8-bit
      int idx = base256idx(rgbColor);
      if (idx == -1) { // match based on RGB
        final int r = Math.min(((rgbColor >> 16) & 0xff) / 42, 6);
        final int g = Math.min(((rgbColor >> 8) & 0xff) / 42, 6);
        final int b = Math.min((rgbColor & 0xff) / 42, 6);
        idx = 16 + 36 * r + 6 * g + b;
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
      switch(rgbColor) {
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
    } else if(TERM != null && TERM.contains("256color")) {
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

  private static int base256idx(final int color) {
      switch(color) {
        // Default MC colors
        case 0x000000 /* black */: return 0x0;
        case 0x0000aa /* dark_blue */: return 0x4;
        case 0x00aa00 /* dark_green */: return 0x2;
        case 0x00aaaa /* dark_aqua */: return 0x6;
        case 0xaa0000 /* dark_red */: return 0x1;
        case 0xaa00aa /* dark_purple */: return 0x5;
        case 0xffaa00 /* gold */: return 0x3;
        case 0xaaaaaa /* gray */: return 0x7;
        case 0x555555 /* dark_gray */: return 0x8;
        case 0x5555ff /* blue */: return 0xC;
        case 0x55ff55 /* green */: return 0xA;
        case 0x55ffff /* aqua */: return 0xE;
        case 0xff5555 /* red */: return 0x9;
        case 0xff55ff /* light_purple */: return 0xD;
        case 0xffff55 /* yellow */: return 0xB;
        case 0xffffff /* white */: return 0xF;
        default: return -1;
      }
  }
}
