package de.cotto.lndmanagej.ui.formatting;

import org.springframework.stereotype.Component;

import java.text.NumberFormat;

@Component
public class Formatter {
    public Formatter() {
        // default constructor
    }

    public String formatNumber(long value) {
        return NumberFormat.getInstance().format(value);
    }
}
