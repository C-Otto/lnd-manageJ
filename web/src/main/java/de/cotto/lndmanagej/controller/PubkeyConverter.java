package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class PubkeyConverter implements Converter<String, Pubkey> {
    public PubkeyConverter() {
        // default constructor
    }

    @Override
    public Pubkey convert(@Nonnull String source) {
        return Pubkey.create(source);
    }
}
