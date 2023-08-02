/*
 * This file is part of ansi, licensed under the MIT License.
 *
 * Copyright (c) 2023 KyoriPowered
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

import org.fusesource.jansi.AnsiColors;
import org.fusesource.jansi.AnsiConsole;

final class JAnsiColorLevel {
  private static final Throwable UNAVAILABILITY_CAUSE;

  static {
    Throwable cause = null;
    try {
      Class.forName("org.fusesource.jansi.AnsiConsole");
    } catch (final ClassNotFoundException classNotFoundException) {
      cause = classNotFoundException;
    }
    UNAVAILABILITY_CAUSE = cause;
  }

  private JAnsiColorLevel() {
  }

  static boolean isAvailable() {
    return UNAVAILABILITY_CAUSE == null;
  }

  static ColorLevel computeFromJAnsi() {
    final AnsiColors colors = AnsiConsole.out().getColors();
    if (colors == null) return ColorLevel.NONE;
    switch (colors) {
      case Colors16:
        return ColorLevel.INDEXED_16;
      case Colors256:
        return ColorLevel.INDEXED_256;
      case TrueColor:
        return ColorLevel.TRUE_COLOR;
      default:
        return ColorLevel.NONE;
    }
  }
}
