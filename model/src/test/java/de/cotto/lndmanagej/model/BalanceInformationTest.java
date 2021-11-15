package de.cotto.lndmanagej.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static org.assertj.core.api.Assertions.assertThat;

class BalanceInformationTest {
    @Test
    void availableLocalBalance() {
        assertThat(BALANCE_INFORMATION.availableLocalBalance()).isEqualTo(Coins.ofSatoshis(900));
    }

    @Test
    void availableLocalBalance_negative() {
        BalanceInformation balanceInformation =
                new BalanceInformation(Coins.ofSatoshis(100), Coins.ofSatoshis(200), Coins.NONE, Coins.NONE);
        assertThat(balanceInformation.availableLocalBalance()).isEqualTo(Coins.NONE);
    }

    @Test
    void availableRemoteBalance() {
        assertThat(BALANCE_INFORMATION.availableRemoteBalance()).isEqualTo(Coins.ofSatoshis(113));
    }

    @Test
    void availableRemoteBalance_negative() {
        BalanceInformation balanceInformation =
                new BalanceInformation(Coins.NONE, Coins.NONE, Coins.ofSatoshis(100), Coins.ofSatoshis(200));
        assertThat(balanceInformation.availableRemoteBalance()).isEqualTo(Coins.NONE);
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(BalanceInformation.class).usingGetClass().verify();
    }
}