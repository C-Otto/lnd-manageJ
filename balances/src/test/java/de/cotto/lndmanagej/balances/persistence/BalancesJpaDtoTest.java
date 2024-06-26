package de.cotto.lndmanagej.balances.persistence;

import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;

import static de.cotto.lndmanagej.balances.BalancesFixtures.BALANCES;
import static de.cotto.lndmanagej.balances.BalancesFixtures.TIMESTAMP;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static org.assertj.core.api.Assertions.assertThat;

class BalancesJpaDtoTest {
    @Test
    void fromModel() {
        BalancesJpaDto jpaDto = BalancesJpaDto.fromModel(BALANCES);
        assertThat(jpaDto.getTimestamp()).isEqualTo(TIMESTAMP.toEpochSecond(ZoneOffset.UTC));
        assertThat(jpaDto.getChannelId()).isEqualTo(CHANNEL_ID.getShortChannelId());
        assertThat(jpaDto.getLocalBalance()).isEqualTo(BALANCE_INFORMATION.localBalance().satoshis());
        assertThat(jpaDto.getLocalReserved()).isEqualTo(BALANCE_INFORMATION.localReserve().satoshis());
        assertThat(jpaDto.getRemoteBalance()).isEqualTo(BALANCE_INFORMATION.remoteBalance().satoshis());
        assertThat(jpaDto.getRemoteReserved()).isEqualTo(BALANCE_INFORMATION.remoteReserve().satoshis());
    }

    @Test
    void toModel() {
        assertThat(BalancesJpaDto.fromModel(BALANCES).toModel()).isEqualTo(BALANCES);
    }
}
