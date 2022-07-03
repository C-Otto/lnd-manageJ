package de.cotto.lndmanagej.ui.controller.param;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class SortByConverter implements Converter<String, SortBy> {

    public SortByConverter() {
        //default
    }

    @Override
    public SortBy convert(String source) {
        try {
            String name = source.replace("-", "_").toUpperCase(Locale.US);
            return SortBy.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return SortBy.DEFAULT_SORT;
        }
    }
}
