package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.ADD_INDEX;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.AMOUNT_PAID;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.HASH;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.KEYSEND_MESSAGE;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.MEMO;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE_KEYSEND;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLE_DATE;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLE_INDEX;
import static org.assertj.core.api.Assertions.assertThat;

class SettledInvoiceTest {
    @Test
    void invalid() {
        SettledInvoice expected = new SettledInvoice(
                -1,
                -1,
                LocalDateTime.MIN,
                "",
                Coins.NONE,
                "",
                Optional.empty(),
                Optional.empty()
        );
        assertThat(SettledInvoice.INVALID).isEqualTo(expected);
    }

    @Test
    void isValid() {
        assertThat(SETTLED_INVOICE.isValid()).isTrue();
        assertThat(SettledInvoice.INVALID.isValid()).isFalse();
    }

    @Test
    void addIndex() {
        assertThat(SETTLED_INVOICE.addIndex()).isEqualTo(ADD_INDEX);
    }

    @Test
    void settleIndex() {
        assertThat(SETTLED_INVOICE.settleIndex()).isEqualTo(SETTLE_INDEX);
    }

    @Test
    void settleDate() {
        assertThat(SETTLED_INVOICE.settleDate()).isEqualTo(SETTLE_DATE);
    }

    @Test
    void hash() {
        assertThat(SETTLED_INVOICE.hash()).isEqualTo(HASH);
    }

    @Test
    void amountPaid() {
        assertThat(SETTLED_INVOICE.amountPaid()).isEqualTo(AMOUNT_PAID);
    }

    @Test
    void memo() {
        assertThat(SETTLED_INVOICE.memo()).isEqualTo(MEMO);
    }

    @Test
    void keysendMessage_empty() {
        assertThat(SETTLED_INVOICE.keysendMessage()).isEmpty();
    }

    @Test
    void keysendMessage() {
        assertThat(SETTLED_INVOICE_KEYSEND.keysendMessage()).contains(KEYSEND_MESSAGE);
    }

    @Test
    void receivedVia() {
        assertThat(SETTLED_INVOICE.receivedVia()).contains(CHANNEL_ID);
    }
}