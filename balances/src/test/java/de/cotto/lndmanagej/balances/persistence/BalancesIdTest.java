package de.cotto.lndmanagej.balances.persistence;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class BalancesIdTest {
    @Test
    void test_default_constructor() {
        // required for JPA
        Assertions.assertThat(new BalancesId()).isNotNull();
    }

    @Test
    void testEquals() {
        EqualsVerifier.simple().forClass(BalancesId.class).usingGetClass().verify();
    }
}