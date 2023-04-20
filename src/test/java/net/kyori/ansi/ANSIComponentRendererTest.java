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

import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ANSIComponentRendererTest {
  @Test
  void testPlain() {
    assertEquals("hello world", this.render(r -> r.text("hello world")));
  }

  @Test
  void testBold() {
    assertEquals("\u001b[1mi'm bold\u001b[0m", this.render(r -> {
      final TestStyle bold = TestStyle.EMPTY.bold(StyleOps.State.TRUE);
      r.pushStyle(bold);
      r.text("i'm bold");
      r.popStyle(bold);
    }));
  }

  @Test
  void testColor() {
    Consumer<ANSIComponentRenderer<TestStyle>> redAction = r -> {
      final TestStyle red = TestStyle.EMPTY.color(0xff_55_55);
      r.pushStyle(red);
      r.text("i'm red");
      r.popStyle(red);
    };
    assertEquals("\u001B[38;2;255;85;85mi'm red\u001b[0m", this.render(redAction, ColorLevel.TRUE_COLOR));
    assertEquals("\u001B[38;5;9mi'm red\u001b[0m", this.render(redAction, ColorLevel.INDEXED_256));
    assertEquals("\u001B[91mi'm red\u001b[0m", this.render(redAction, ColorLevel.INDEXED_16));
    assertEquals("i'm red", this.render(redAction, ColorLevel.NONE));

    Consumer<ANSIComponentRenderer<TestStyle>> pureRedAction = r -> {
      final TestStyle red = TestStyle.EMPTY.color(0xff_00_00);
      r.pushStyle(red);
      r.text("i'm very red");
      r.popStyle(red);
    };
    assertEquals("\u001B[38;2;255;0;0mi'm very red\u001b[0m", this.render(pureRedAction, ColorLevel.TRUE_COLOR));
    assertEquals("\u001B[38;5;196mi'm very red\u001b[0m", this.render(pureRedAction, ColorLevel.INDEXED_256));
    assertEquals("\u001B[31mi'm very red\u001b[0m", this.render(pureRedAction, ColorLevel.INDEXED_16));
    assertEquals("i'm very red", this.render(pureRedAction, ColorLevel.NONE));
  }

  @Test
  void testDeepColor() {
    assertEquals("\u001B[38;2;0;255;0mg\u001B[38;2;0;0;255mb\u001B[38;2;0;255;0mg\u001B[38;2;0;0;255mb\u001B[38;2;0;255;0mg\u001B[38;2;0;0;255mb\u001B[38;2;0;255;0mg\u001B[38;2;0;0;255mb\u001B[38;2;0;255;0mg\u001B[38;2;0;0;255mb\u001B[38;2;0;255;0mg\u001B[38;2;0;0;255mb\u001B[0m", this.render(r -> {
      final TestStyle g = TestStyle.EMPTY.color(0x00_ff_00);
      final TestStyle b = TestStyle.EMPTY.color(0x00_00_ff);
      for (int i = 0; i < 6; i++) {
        r.pushStyle(g);
        r.text("g");
        r.pushStyle(b);
        r.text("b");
      }
      for (int i = 0; i < 6; i++) {
        r.popStyle(b);
        r.popStyle(g);
      }
    }));
  }

  @Test
  void testStringBuilder() {
    StringBuilder sb = new StringBuilder();
    sb.append("hello ");
    final ANSIComponentRenderer.ToStringBuilder<TestStyle> renderer = ANSIComponentRenderer.toStringBuilder(TestStyle.ops(), ColorLevel.TRUE_COLOR);
    renderer.builder(sb);
    final TestStyle magenta = TestStyle.EMPTY.color(0xff_55_ff);
    renderer.pushStyle(magenta);
    renderer.text("colorful");
    renderer.popStyle(magenta);
    renderer.complete();
    sb.append(" world");

    assertEquals("hello \u001B[38;2;255;85;255mcolorful\u001B[0m world", sb.toString());
  }

  @Test
  void testFalseDecoration() {
    assertEquals("\u001B[1mon\u001B[0moff\u001B[1mon\u001B[0m", this.render(r -> {
      final TestStyle boldOn = TestStyle.EMPTY.bold(StyleOps.State.TRUE);
      final TestStyle boldOff = TestStyle.EMPTY.bold(StyleOps.State.FALSE);

      r.pushStyle(boldOn);
      r.text("on");
      r.pushStyle(boldOff);
      r.text("off");
      r.popStyle(boldOff);
      r.text("on");
      r.popStyle(boldOff);
    }));
  }

  @Test
  void testSystemProperty() {
    System.setProperty(ColorLevel.COLOR_LEVEL_PROPERTY_NAME, "truecolor");
    assertEquals(ColorLevel.compute(), ColorLevel.TRUE_COLOR);
  }

  private String render(final Consumer<ANSIComponentRenderer<TestStyle>> action) {
    return this.render(action, ColorLevel.TRUE_COLOR);
  }

  private String render(final Consumer<ANSIComponentRenderer<TestStyle>> action, final ColorLevel level) {
    final ANSIComponentRenderer.ToString<TestStyle> renderer = ANSIComponentRenderer.toString(TestStyle.ops(), level);
    action.accept(renderer);
    renderer.complete();
    return renderer.asString();
  }
}
