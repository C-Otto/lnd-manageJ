package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Pubkey;

import java.util.Collection;
import java.util.List;

public record PubkeysDto(List<String> pubkeys) {
    public PubkeysDto(Collection<Pubkey> pubkeys) {
        this(pubkeys.stream().map(Pubkey::toString).toList());
    }
}
