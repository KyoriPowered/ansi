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

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A renderer converting a flattened component into ANSI.
 *
 * <p>Renderer instances are reusable, but are not thread-safe.</p>
 *
 * @param <S> style type
 * @since 1.0.0
 */
@ApiStatus.NonExtendable
public interface ANSIComponentRenderer<S> {
  /**
   * Create a new renderer that produces a {@link String}, with a color level inferred from the current environment.
   *
   * @param <T> the style type
   * @param ops an implementation that can work on certain styles.
   * @return the renderer
   * @since 1.0.0
   */
  static <T> @NotNull ToString<T> toString(final @NotNull StyleOps<T> ops) {
    return toString(ops, ColorLevel.compute());
  }

  /**
   * Create a new renderer that produces a {@link String}.
   *
   * @param <T> the style type
   * @param ops an implementation that can work on certain styles.
   * @param colorLevel the color support level to emit to
   * @return the renderer
   * @since 1.0.0
   */
  static <T> @NotNull ToString<T> toString(final @NotNull StyleOps<T> ops, final @NotNull ColorLevel colorLevel) {
    return new ANSIComponentRendererImpl.ToString<>(ops, colorLevel);
  }

  /**
   * Create a new renderer that will write to a specific {@link StringBuilder}, with a color level inferred from the current environment.
   *
   * @param <T> the style type
   * @param ops an implementation that can work on the provided style type
   * @return the renderer
   * @since 1.0.0
   */
  static <T> @NotNull ToStringBuilder<T> toStringBuilder(final @NotNull StyleOps<T> ops) {
    return toStringBuilder(ops, ColorLevel.compute());
  }

  /**
   * Create a new renderer that will write to a specific {@link StringBuilder}.
   *
   * @param <T> the style type
   * @param ops an implementation that can work on the provided style type
   * @param colorLevel the color support level to emit to
   * @return the renderer
   * @since 1.0.0
   */
  static <T> @NotNull ToStringBuilder<T> toStringBuilder(final @NotNull StyleOps<T> ops, final @NotNull ColorLevel colorLevel) {
    return new ANSIComponentRendererImpl.ToStringBuilder<>(ops, colorLevel);
  }

  /**
   * Push a style onto the stack to format upcoming invocations of {@link #text(String)}.
   *
   * <p>This push must be balanced by an equivalent pop where this style ceases to apply.</p>
   *
   * @param style the style to apply
   * @return this renderer
   * @throws IllegalStateException if the style stack exceeds a depth of 1024
   * @since 1.0.0
   */
  @NotNull ANSIComponentRenderer<S> pushStyle(final @NotNull S style);

  /**
   * Append text to the buffer.
   *
   * <p>Any pending style will be emitted here as well.</p>
   *
   * @param text text to append
   * @return this renderer
   * @since 1.0.0
   */
  @NotNull ANSIComponentRenderer<S> text(final @NotNull String text);

  /**
   * Pop the top style from the stack, so it will no longer affect output.
   *
   * @param style the style to pop
   * @return this renderer
   * @throws IllegalStateException if attempting to pop when no style is remaining on the stack
   * @since 1.0.0
   */
  @NotNull ANSIComponentRenderer<S> popStyle(final @NotNull S style);

  /**
   * Indicate to the renderer that a complete component has been provided, and any finalizing output should be appended.
   *
   * @return this renderer
   * @since 1.0.0
   */
  @NotNull ANSIComponentRenderer<S> complete();

  /**
   * A mutable sub-interface that allows targeting an existing string builder.
   *
   * @param <S> style type
   * @since 1.0.0
   */
  interface ToStringBuilder<S> extends ANSIComponentRenderer<S> {
    /**
     * Set the {@link StringBuilder} that will be appended to.
     *
     * @param builder the builder to append further output to
     * @since 1.0.0
     */
    void builder(final @NotNull StringBuilder builder);

    /**
     * Get the builder that has been appended to.
     *
     * <p>This builder will not be cleared.</p>
     *
     * @return the builder
     * @since 1.0.0
     */
    @Contract(pure = true)
    @NotNull StringBuilder builder();
  }

  /**
   * A sub-interface that simply provides a string output.
   *
   * @param <S> style type
   * @since 1.0.0
   */
  interface ToString<S> extends ANSIComponentRenderer<S> {
    /**
     * Get the output and clear the buffer from a previous invocation.
     *
     * @return the produced output
     * @since 1.0.0
     */
    @Contract(pure = false)
    @NotNull String asString();
  }
}
