package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static de.cotto.lndmanagej.model.OnlineStatusFixtures.ONLINE_STATUS;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OnlineStatusTest {
    @Test
    void online() {
        assertThat(ONLINE_STATUS.online()).isTrue();
    }

    @Test
    void since() {
        assertThat(ONLINE_STATUS.since())
                .isEqualTo(ZonedDateTime.of(2021, 12, 23, 1, 2, 3, 0, ZoneOffset.UTC));
    }
}