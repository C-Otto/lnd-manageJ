package de.cotto.lndmanagej.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.BalanceInformation.EMPTY;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION_2;
import static org.assertj.core.api.Assertions.assertThat;

class BalanceInformationTest {
    @Test
    void empty() {
        assertThat(EMPTY).isEqualTo(new BalanceInformation(Coins.NONE, Coins.NONE, Coins.NONE, Coins.NONE));
    }

    @Test
    void add_empty_to_empty() {
        assertThat(EMPTY.add(EMPTY)).isEqualTo(EMPTY);
    }

    @Test
    void add_empty_to_something() {
        assertThat(BALANCE_INFORMATION.add(EMPTY)).isEqualTo(BALANCE_INFORMATION);
    }

    @Test
    void add() {
        BalanceInformation expected = new BalanceInformation(
                BALANCE_INFORMATION.localBalance().add(BALANCE_INFORMATION_2.localBalance()),
                BALANCE_INFORMATION.localReserve().add(BALANCE_INFORMATION_2.localReserve()),
                BALANCE_INFORMATION.remoteBalance().add(BALANCE_INFORMATION_2.remoteBalance()),
                BALANCE_INFORMATION.remoteReserve().add(BALANCE_INFORMATION_2.remoteReserve())
        );
        assertThat(BALANCE_INFORMATION.add(BALANCE_INFORMATION_2)).isEqualTo(expected);
    }

    @Test
    void add_with_zero_available_due_to_reserve() {
        BalanceInformation zeroAvailableBalance = new BalanceInformation(
                Coins.ofSatoshis(50),
                Coins.ofSatoshis(100),
                Coins.ofSatoshis(20),
                Coins.ofSatoshis(100)
        );
        BalanceInformation sum = BALANCE_INFORMATION.add(zeroAvailableBalance);
        assertThat(sum.localAvailable()).isEqualTo(BALANCE_INFORMATION.localAvailable());
        assertThat(sum.remoteAvailable()).isEqualTo(BALANCE_INFORMATION.remoteAvailable());
    }

    @Test
    void localAvailableBalance() {
        assertThat(BALANCE_INFORMATION.localAvailable()).isEqualTo(Coins.ofSatoshis(900));
    }

    @Test
    void localAvailableBalance_negative() {
        BalanceInformation balanceInformation =
                new BalanceInformation(Coins.ofSatoshis(100), Coins.ofSatoshis(200), Coins.NONE, Coins.NONE);
        assertThat(balanceInformation.localAvailable()).isEqualTo(Coins.NONE);
    }

    @Test
    void remoteAvailableBalance() {
        assertThat(BALANCE_INFORMATION.remoteAvailable()).isEqualTo(Coins.ofSatoshis(113));
    }

    @Test
    void remoteAvailableBalance_negative() {
        BalanceInformation balanceInformation =
                new BalanceInformation(Coins.NONE, Coins.NONE, Coins.ofSatoshis(100), Coins.ofSatoshis(200));
        assertThat(balanceInformation.remoteAvailable()).isEqualTo(Coins.NONE);
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(BalanceInformation.class).usingGetClass().verify();
    }
}