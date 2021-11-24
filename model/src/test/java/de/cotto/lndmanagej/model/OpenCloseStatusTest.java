package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.OpenCloseStatus.CLOSED;
import static de.cotto.lndmanagej.model.OpenCloseStatus.FORCE_CLOSING;
import static de.cotto.lndmanagej.model.OpenCloseStatus.OPEN;
import static de.cotto.lndmanagej.model.OpenCloseStatus.WAITING_CLOSE;
import static org.assertj.core.api.Assertions.assertThat;

class OpenCloseStatusTest {
    @Test
    @SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
    void testToString() {
        assertThat(OPEN).hasToString("OPEN");
        assertThat(WAITING_CLOSE).hasToString("WAITING_CLOSE");
        assertThat(FORCE_CLOSING).hasToString("FORCE_CLOSING");
        assertThat(CLOSED).hasToString("CLOSED");
    }
}