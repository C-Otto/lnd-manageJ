package de.cotto.lndmanagej.controller.dto;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static org.assertj.core.api.Assertions.assertThat;

class BalanceInformationDtoTest {
    @Test
    void createFrom() {
        BalanceInformationDto expected = new BalanceInformationDto(
                String.valueOf(BALANCE_INFORMATION.localBalance().satoshis()),
                String.valueOf(BALANCE_INFORMATION.localReserve().satoshis()),
                String.valueOf(BALANCE_INFORMATION.localAvailable().satoshis()),
                String.valueOf(BALANCE_INFORMATION.remoteBalance().satoshis()),
                String.valueOf(BALANCE_INFORMATION.remoteReserve().satoshis()),
                String.valueOf(BALANCE_INFORMATION.remoteAvailable().satoshis())
        );
        assertThat(BalanceInformationDto.createFrom(BALANCE_INFORMATION)).isEqualTo(expected);
    }
}