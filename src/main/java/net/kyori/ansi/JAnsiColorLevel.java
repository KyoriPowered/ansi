package net.kyori.ansi;

import org.fusesource.jansi.AnsiColors;
import org.fusesource.jansi.AnsiConsole;

public class JAnsiColorLevel {
  public static ColorLevel computeFromJAnsi() {
    AnsiColors colors = AnsiConsole.out().getColors();
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
