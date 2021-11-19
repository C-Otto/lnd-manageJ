package de.cotto.lndmanagej.controller;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import de.cotto.lndmanagej.model.Pubkey;

public record NodeDetailsDto(
        @JsonSerialize(using = ToStringSerializer.class) Pubkey pubkey,
        String alias,
        boolean online
) {
}
