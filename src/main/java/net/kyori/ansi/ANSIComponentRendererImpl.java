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
import org.jetbrains.annotations.Nullable;

import static java.util.Objects.requireNonNull;

abstract class ANSIComponentRendererImpl<S> implements ANSIComponentRenderer<S> {
  private static final int MAX_DEPTH = 128;
  private final StyleOps<S> ops;
  private final ColorLevel color;

  protected StringBuilder builder;
  @SuppressWarnings("unchecked")
  private final Frame active = new Frame();
  private Frame[] styles = new Frame[8];
  private int head = -1;
  private boolean stylePending;

  protected ANSIComponentRendererImpl(final StyleOps<S> ops, final ColorLevel colorLevel) {
    this.ops = requireNonNull(ops, "ops");
    this.color = requireNonNull(colorLevel, "colorLevel");
  }

  private @Nullable Frame peek() {
    if (this.head < 0) return null;

    return this.styles[this.head];
  }

  private Frame push() {
    final int idx = ++this.head;
    if (idx >= MAX_DEPTH) {
      throw new IllegalStateException("Too many styles! Maximum depth of " + MAX_DEPTH + " exceeded");
    } else if (idx >= this.styles.length) {
      this.styles = Arrays.copyOf(this.styles, this.styles.length * 2);
    }

    Frame frame = this.styles[idx];
    if (frame == null) {
      frame = this.styles[idx] = new Frame();
    }
    if (idx > 0) {
      frame.set(this.styles[idx - 1]);
    } else {
      frame.clear();
    }

    return frame;
  }

  private Frame pop() {
    // Pop style onto stack, update pointer, validate balance
    if (this.head < 0) {
      throw new IllegalStateException("Tried to pop beyond what was pushed!");
    }

    return this.styles[this.head--];
  }

  @Override
  public @NotNull ANSIComponentRenderer<S> pushStyle(final @NotNull S style) {
    final Frame frame = this.push();
    frame.apply(style, this.ops);
    this.stylePending = true;
    return this;
  }

  @Override
  public @NotNull ANSIComponentRenderer<S> text(final @NotNull String text) {
    // Compute style difference
    this.appendUpdatedStyle();
    // Then append the string
    this.builder.append(text);
    return this;
  }

  private void appendUpdatedStyle() {
    if (this.stylePending) {
      final Frame style = this.peek();
      this.printDifferences(this.active, style);
      if (style == null) {
        this.active.clear();
      } else {
        this.active.set(style);
      }
      this.stylePending = false;
    }
  }

  @Override
  public @NotNull ANSIComponentRenderer<S> popStyle(final @NotNull S style) {
    this.pop();
    this.stylePending = true;
    return this;
  }

  @Override
  public @NotNull ANSIComponentRenderer<S> complete() {
    if (this.head != -1) {
      throw new IllegalStateException("Ended build with unbalanced stack. Remaining items are: " + Arrays.toString(Arrays.copyOf(this.styles, this.head + 1)));
    }
    this.appendUpdatedStyle();
    return this;
  }

  private void printDifferences(final @NotNull Frame active, final @Nullable Frame target) {
    final StringBuilder builder = this.builder;
    if (target == null) {
      if (active.style != 0 || active.color != StyleOps.COLOR_UNSET)
        Formats.emit(Formats.reset(), builder);
    } else if (active.style != target.style || target.color == StyleOps.COLOR_UNSET) {
      // reset, emit everything
      if (active.style != 0) Formats.emit(Formats.reset(), builder);
      if ((target.style & Frame.BOLD) != 0) Formats.emit(Formats.bold(true), builder);
      if ((target.style & Frame.ITALICS) != 0) Formats.emit(Formats.italics(true), builder);
      if ((target.style & Frame.OBFUSCATED) != 0) Formats.emit(Formats.obfuscated(true), builder);
      if ((target.style & Frame.STRIKETHROUGH) != 0) Formats.emit(Formats.strikethrough(true), builder);
      if ((target.style & Frame.UNDERLINED) != 0) Formats.emit(Formats.underlined(true), builder);
      if (target.color != StyleOps.COLOR_UNSET) Formats.emit(this.color.determineEscape(target.color), builder);
    } else if (active.color != target.color) {
      Formats.emit(this.color.determineEscape(target.color), builder);
    }
  }

  static final class Frame {
    static final int BOLD = 1;
    static final int ITALICS = 1 << 1;
    static final int OBFUSCATED = 1 << 2;
    static final int STRIKETHROUGH = 1 << 3;
    static final int UNDERLINED = 1 << 4;

    int color;
    int style;

    Frame() {
    }

    void set(final @NotNull Frame other) {
      this.color = other.color;
      this.style = other.style;
    }

    <S> void apply(final @NotNull S that, final @NotNull StyleOps<S> ops) {
      final int color = ops.color(that);
      if (color != StyleOps.COLOR_UNSET) this.color = ops.color(that);

      this.apply(BOLD, ops.bold(that));
      this.apply(ITALICS, ops.italics(that));
      this.apply(OBFUSCATED, ops.obfuscated(that));
      this.apply(STRIKETHROUGH, ops.strikethrough(that));
      this.apply(UNDERLINED, ops.underlined(that));
    }

    private void apply(final int flag, final StyleOps.State state) {
      switch (state) {
        case TRUE:
          this.style |= flag;
          break;
        case FALSE:
          this.style &= ~flag;
          break;
        case UNSET: // fall-through
        default: // no-op
      }
    }

    public void clear() {
      this.color = -1;
      this.style = 0;
    }
  }

  static final class ToStringBuilder<S> extends ANSIComponentRendererImpl<S> implements ANSIComponentRenderer.ToStringBuilder<S> {
    ToStringBuilder(final StyleOps<S> ops, final ColorLevel colorLevel) {
      super(ops, colorLevel);
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
    ToString(final StyleOps<S> ops, final ColorLevel colorLevel) {
      super(ops, colorLevel);
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
