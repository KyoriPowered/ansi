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

/**
 * Data query operations that can be performed on a style-containing object.
 *
 * <p>Each chat component has a style, and any unset values are inherited from that component's parent.</p>
 *
 * @param <S> style type
 * @since 1.0.0
 */
public interface StyleOps<S> {
  /**
   * Represents possible states for a decoration.
   *
   * <p>{@code UNSET} will inherit the parent style.</p>
   *
   * @since 1.0.0
   */
  enum State {
    TRUE, FALSE, UNSET;
  }

  /**
   * Indicates that the queried style has no color set.
   *
   * @since 1.0.0
   */
  int COLOR_UNSET = -1;

  /**
   * Whether the style indicates a component is bold or not.
   *
   * @param style the active style
   * @return if bold
   * @since 1.0.0
   */
  State bold(final @NotNull S style);

  /**
   * Get the italic value from this style.
   *
   * @param style the active style
   * @return if italic
   * @since 1.0.0
   */
  State italics(final @NotNull S style);

  /**
   * Get the underlined value from this style.
   *
   * @param style the active style
   * @return if underlined
   * @since 1.0.0
   */
  State underlined(final @NotNull S style);

  /**
   * Get the strikethrough value from this style.
   *
   * @param style the active style
   * @return if struck-through
   * @since 1.0.0
   */
  State strikethrough(final @NotNull S style);

  /**
   * Get the obfuscated value from this style.
   *
   * @param style the active style
   * @return if obfuscated ('magic' text)
   * @since 1.0.0
   */
  State obfuscated(final @NotNull S style);

  /**
   * Get the color as an int-packed RGB value.
   *
   * @param style the active style
   * @return the text color, or {@value #COLOR_UNSET} if no color is set
   * @since 1.0.0
   */
  @Range(from = -1, to = 0xffffff) int color(final @NotNull S style);

  /**
   * Get the style's declared font as a string representing a resource location.
   *
   * @param style the active style
   * @return the font, if any is present
   * @since 1.0.0
   */
  @Nullable String font(final @NotNull S style);
}
