package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Pubkey;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public record PubkeysDto(List<String> pubkeys) {
    public PubkeysDto(Collection<Pubkey> pubkeys) {
        this(pubkeys.stream().map(Pubkey::toString).collect(Collectors.toList()));
    }
}
