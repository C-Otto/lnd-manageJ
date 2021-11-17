package de.cotto.lndmanagej.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ClosedOrClosingChannelTest {
    @Test
    void testEquals() {
        EqualsVerifier.forClass(ClosedOrClosingChannel.class).usingGetClass().verify();
    }
}