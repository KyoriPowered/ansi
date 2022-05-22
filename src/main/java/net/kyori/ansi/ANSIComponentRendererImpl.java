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

import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

abstract class ANSIComponentRendererImpl<S> implements ANSIComponentRenderer<S> {
  private static final int MAX_DEPTH = 32;
  private final StyleOps<S> ops;

  private final Style lastWritten = new Style();
  protected StringBuilder builder;
  @SuppressWarnings("unchecked")
  private S[] styles = (S[]) new Object[8];
  private int head = -1;

  protected ANSIComponentRendererImpl(final StyleOps<S> ops) {
    this.ops = requireNonNull(ops, "ops");
  }

  @Override
  public @NotNull ANSIComponentRenderer<S> pushStyle(final @NotNull S style) {
    final int idx = ++this.head;
    if(idx >= this.styles.length) {
      this.styles = Arrays.copyOf(this.styles, this.styles.length * 2);
    }
    // Push new style onto stack, growing if necessary
    // Calculate all changes between old and new, and write those ansi sequences out
    if(idx == 0) {
      this.styles[idx] = style;
    } else {
      this.styles[idx] = this.ops.merge(this.styles[this.head - 1], style);
    }

    return this;
  }

  @Override
  public @NotNull ANSIComponentRenderer<S> text(final @NotNull String text) {
    // Compute style difference
    // Then append the string
    this.builder.append(text);
    return this;
  }

  private void appendUpdatedStyle() {

  }

  @Override
  public @NotNull ANSIComponentRenderer<S> popStyle(final @NotNull S style) {
    // Pop style onto stack, update pointer, validate balance
    if(this.head-- < 0) {
      throw new IllegalStateException("Tried to pop beyond what was pushed!");
    }
    // Calculate all changes between old and new, and write those ansi sequences out
    return this;
  }

  private void printDifferences(final S oldStyle, final S newStyle) {
    final StringBuilder builder = this.builder;
    // Write ansi sequences
  }

  static final class Style {

    private int color;
    private boolean bold;
    private boolean italics;
    private boolean obfuscated;
    private boolean strikethrough;
    private boolean underlined;

    Style() {
    }

    <S> void update(final @NotNull S that, final @NotNull StyleOps<S> ops) {
      this.color = ops.color(that);
      this.bold = ops.bold(that);
      this.italics = ops.italics(that);
      this.obfuscated = ops.obfuscated(that);
      this.strikethrough = ops.strikethrough(that);
      this.underlined = ops.underlined(that);
    }

    public void clear() {
      this.color = -1;
      this.bold = false;
      this.italics = false;
      this.obfuscated = false;
      this.strikethrough = false;
      this.underlined = false;
    }

  }

  @Override
  public @NotNull ANSIComponentRenderer<S> complete() {
    if(this.head != -1) {
      throw new IllegalStateException("Ended build with unbalanced stack. Remaining items are: " + Arrays.toString(Arrays.copyOf(this.styles, this.head + 1)));
    }
    return this;
  }

  static final class ToStringBuilder<S> extends ANSIComponentRendererImpl<S> implements ANSIComponentRenderer.ToStringBuilder<S> {

    ToStringBuilder(final StyleOps<S> ops) {
      super(ops);
    }

    @Override
    public void builder(final @NotNull StringBuilder builder) {
      this.builder = requireNonNull(builder, "builder");
    }

    @Override
    public @NotNull StringBuilder builder() {
      if(this.builder == null) {
        throw new IllegalStateException("String builder has not yet been initialized");
      }
      return this.builder;
    }
  }

  static final class ToString<S> extends ANSIComponentRendererImpl<S> implements ANSIComponentRenderer.ToString<S> {

    ToString(final StyleOps<S> ops) {
      super(ops);
      this.builder = new StringBuilder();
    }

    @Override
    public String asString() {
      final String output = this.builder.toString();
      this.builder.delete(0, this.builder.length());
      return output;
    }
  }
}
