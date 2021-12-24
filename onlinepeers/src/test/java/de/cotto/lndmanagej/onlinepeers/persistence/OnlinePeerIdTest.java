package de.cotto.lndmanagej.onlinepeers.persistence;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OnlinePeerIdTest {
    @Test
    void test_default_constructor() {
        // required for JPA
        assertThat(new OnlinePeerId()).isNotNull();
    }

    @Test
    void testEquals() {
        EqualsVerifier.simple().forClass(OnlinePeerId.class).usingGetClass().verify();
    }
}