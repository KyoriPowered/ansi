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

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A renderer converting a flattened component into ANSI.
 *
 * @param <S> style type
 * @since 1.0.0
 */
public interface ANSIComponentRenderer<S> {

  static <T> ToString<T> renderingToString(final StyleOps<T> ops) {
    return new ANSIComponentRendererImpl.ToString<>(ops);
  }

  static <T> ToStringBuilder<T> renderingToStringBuilder(final StyleOps<T> ops) {
    return new ANSIComponentRendererImpl.ToStringBuilder<>(ops);
  }

  @NonNull ANSIComponentRenderer<S> pushStyle(final @NonNull S style);

  @NonNull ANSIComponentRenderer<S> text(final @NonNull String text);

  @NonNull ANSIComponentRenderer<S> popStyle(final @NonNull S style);

  @NonNull ANSIComponentRenderer<S> complete();

  /**
   * A mutable sub-interface that allows targeting an existing string builder.
   *
   * @param <S> style type
   * @since 1.0.0
   */
  interface ToStringBuilder<S> extends ANSIComponentRenderer<S> {

    void builder(final @NonNull StringBuilder builder);

    @NonNull StringBuilder builder();

  }

  /**
   * A sub-interface that simply provides a string output.
   *
   * @param <S> style type
   * @since 1.0.0
   */
  interface ToString<S> extends ANSIComponentRenderer<S> {

    @NonNull String asString();

  }
}
