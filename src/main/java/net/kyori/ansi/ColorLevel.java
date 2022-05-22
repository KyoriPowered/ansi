/*
 * This file is part of ansi, licensed under the MIT License.
 *
 * Copyright (c) 2017-2021 KyoriPowered
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
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Supported color levels.
 *
 * @since 1.0.0
 */
public enum ColorLevel {
  TRUE_COLOR {
    @Override
    public @NotNull String determineEscape(final int rgb) {
      return "38;2;" + ((rgb >> 16) & 0xff) + ';' + ((rgb >> 8) & 0xff) + ';' + (rgb & 0xff);
    }
  },
  INDEXED_256 {
    @Override
    public @NotNull String determineEscape(final int rgbColor) {
      // TODO return "38;5;" + idx;
      return INDEXED_16.determineEscape(rgbColor);
    }
  },
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
        default:
          // TODO: Find nearest
      }
      return "39"; // Unknown, reset to default
    }
  };

  /**
   * Attempt to estimate the supported colour level of a terminal using the provided output.
   *
   * @param readLine a function that will read a line to the terminal
   * @param writeLine a function that will accept a string and write it to the console
   * @return the estimated color level
   * @since 1.0.0
   */
  public static @NotNull ColorLevel estimate(final Supplier<String> readLine, final Consumer<String> writeLine) {
    // TODO
    // https://gist.github.com/XVilka/8346728d
    // TODO: Add our own system property for override
    final @Nullable String colorterm = System.getenv("COLORTERM");
    final @Nullable String term = System.getenv("TERM");
    if(colorterm != null && (colorterm.equals("truecolor") || colorterm.equals("24bit"))) {
      return ColorLevel.TRUE_COLOR;
    } else if(term != null && term.contains("256color")) {
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
   * @return an appropriate escape sequence for the color.
   * @since 1.0.0
   */
  public abstract @NotNull String determineEscape(final int rgbColor);
}
