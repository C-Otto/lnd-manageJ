package de.cotto.lndmanagej.feerates.persistence;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeeRatesIdTest {
    @Test
    void test_default_constructor() {
        // required for JPA
        assertThat(new FeeRatesId()).isNotNull();
    }

    @Test
    void testEquals() {
        EqualsVerifier.simple().forClass(FeeRatesId.class).usingGetClass().verify();
    }
}
