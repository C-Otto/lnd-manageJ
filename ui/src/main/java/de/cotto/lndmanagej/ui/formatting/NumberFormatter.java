package de.cotto.lndmanagej.ui.formatting;

import java.text.NumberFormat;

public final class NumberFormatter {
    private NumberFormatter() {
        // utility class
    }

    public static String format(long value) {
        return NumberFormat.getInstance().format(value);
    }
}
