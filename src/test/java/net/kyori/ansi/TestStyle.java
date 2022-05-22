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
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

class TestStyle {
  static final TestStyle EMPTY = new TestStyle(StyleOps.COLOR_UNSET, StyleOps.State.UNSET, StyleOps.State.UNSET, StyleOps.State.UNSET, StyleOps.State.UNSET, StyleOps.State.UNSET, null);

  final int color;
  final StyleOps.State bold;
  final StyleOps.State italics;
  final StyleOps.State strikethrough;
  final StyleOps.State obfuscated;
  final StyleOps.State underlined;
  final String font;

  static StyleOps<TestStyle> ops() {
    return TestOps.INSTANCE;
  }

  TestStyle(final int color, final StyleOps.State bold, final StyleOps.State italics, final StyleOps.State strikethrough, final StyleOps.State obfuscated, final StyleOps.State underlined, final String font) {
    this.color = color;
    this.bold = bold;
    this.italics = italics;
    this.strikethrough = strikethrough;
    this.obfuscated = obfuscated;
    this.underlined = underlined;
    this.font = font;
  }

  TestStyle color(final int color) {
    return new TestStyle(color, this.bold, this.italics, this.strikethrough, this.obfuscated, this.underlined, this.font);
  }

  TestStyle bold(final StyleOps.State bold) {
    return new TestStyle(this.color, bold, this.italics, this.strikethrough, this.obfuscated, this.underlined, this.font);
  }

  TestStyle italics(final StyleOps.State italics) {
    return new TestStyle(this.color, this.bold, italics, this.strikethrough, this.obfuscated, this.underlined, this.font);
  }

  TestStyle strikethrough(final StyleOps.State strikethrough) {
    return new TestStyle(this.color, this.bold, this.italics, strikethrough, this.obfuscated, this.underlined, this.font);
  }

  TestStyle obfuscated(final StyleOps.State obfuscated) {
    return new TestStyle(this.color, this.bold, this.italics, this.strikethrough, obfuscated, this.underlined, this.font);
  }

  TestStyle underlined(final StyleOps.State underlined) {
    return new TestStyle(this.color, this.bold, this.italics, this.strikethrough, this.obfuscated, underlined, this.font);
  }

  TestStyle font(final String font) {
    return new TestStyle(this.color, this.bold, this.italics, this.strikethrough, this.obfuscated, this.underlined, font);
  }

  static class TestOps implements StyleOps<TestStyle> {
    static final TestOps INSTANCE = new TestOps();

    @Override
    public State bold(@NotNull final TestStyle style) {
      return style.bold;
    }

    @Override
    public State italics(@NotNull final TestStyle style) {
      return style.italics;
    }

    @Override
    public State underlined(@NotNull final TestStyle style) {
      return style.underlined;
    }

    @Override
    public State strikethrough(@NotNull final TestStyle style) {
      return style.strikethrough;
    }

    @Override
    public State obfuscated(@NotNull final TestStyle style) {
      return style.obfuscated;
    }

    @Override
    public @Range(from = -1, to = 16777215) int color(@NotNull final TestStyle style) {
      return style.color;
    }

    @Override
    public @Nullable String font(@NotNull final TestStyle style) {
      return style.font;
    }
  }
}
