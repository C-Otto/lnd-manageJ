package de.cotto.lndmanagej.onlinepeers.persistence;

import de.cotto.lndmanagej.model.OnlineStatus;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;

class OnlinePeerJpaDtoTest {
    @Test
    void toModel() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2021, 12, 23, 1, 2, 3, 0, ZoneOffset.UTC);
        OnlinePeerJpaDto onlinePeerJpaDto = new OnlinePeerJpaDto(PUBKEY, true, zonedDateTime);
        assertThat(onlinePeerJpaDto.toModel()).isEqualTo(new OnlineStatus(true, zonedDateTime));
    }
}