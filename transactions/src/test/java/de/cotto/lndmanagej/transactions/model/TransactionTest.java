package de.cotto.lndmanagej.transactions.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.BLOCK_HEIGHT;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.FEES;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.POSITION_IN_BLOCK;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.TRANSACTION;
import static org.assertj.core.api.Assertions.assertThat;

class TransactionTest {

    @Test
    void getHash() {
        assertThat(TRANSACTION.hash()).isEqualTo(TRANSACTION_HASH);
    }

    @Test
    void getFees() {
        assertThat(TRANSACTION.fees()).isEqualTo(FEES);
    }

    @Test
    void getPositionInBlock() {
        assertThat(TRANSACTION.positionInBlock()).isEqualTo(POSITION_IN_BLOCK);
    }

    @Test
    void getBlockHeight() {
        assertThat(TRANSACTION.blockHeight()).isEqualTo(BLOCK_HEIGHT);
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(Transaction.class).usingGetClass().verify();
    }
}