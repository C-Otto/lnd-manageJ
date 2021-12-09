package de.cotto.lndmanagej.invoices.persistence;

import de.cotto.lndmanagej.model.SettledInvoice;
import de.cotto.lndmanagej.model.SettledInvoiceFixtures;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.KEYSEND_MESSAGE;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE_KEYSEND;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE_NO_CHANNEL_ID;
import static org.assertj.core.api.Assertions.assertThat;

class SettledSettledInvoiceJpaDtoTest {
    @Test
    @SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
    void createFromModel() {
        SettledInvoiceJpaDto jpaDto = SettledInvoiceJpaDto.createFromModel(SETTLED_INVOICE);
        assertThat(jpaDto.getAddIndex()).isEqualTo(SETTLED_INVOICE.addIndex());
        assertThat(jpaDto.getSettleIndex()).isEqualTo(SETTLED_INVOICE.settleIndex());
        assertThat(jpaDto.getSettleDate())
                .isEqualTo(SETTLED_INVOICE.settleDate().toEpochSecond(ZoneOffset.UTC));
        assertThat(jpaDto.getHash()).isEqualTo(SETTLED_INVOICE.hash());
        assertThat(jpaDto.getAmountPaid()).isEqualTo(SETTLED_INVOICE.amountPaid().milliSatoshis());
        assertThat(jpaDto.getMemo()).isEqualTo(SETTLED_INVOICE.memo());
        assertThat(jpaDto.getKeysendMessage()).isNull();
        assertThat(jpaDto.getReceivedVia()).isEqualTo(CHANNEL_ID.getShortChannelId());
    }

    @Test
    void createFromModel_with_keysend_message() {
        SettledInvoiceJpaDto jpaDto = SettledInvoiceJpaDto.createFromModel(SETTLED_INVOICE_KEYSEND);
        assertThat(jpaDto.getKeysendMessage()).isEqualTo(KEYSEND_MESSAGE);
    }

    @Test
    void createFromModel_truncates_too_long_keysend_message() {
        String longMessage = longMessage(5_001);
        SettledInvoiceJpaDto jpaDto = getDtoWithMessage(longMessage);
        assertThat(jpaDto.getKeysendMessage()).isEqualTo(longMessage.substring(0, 5_000));
    }

    @Test
    void createFromModel_with_keysend_message_at_length_limit() {
        String longMessage = longMessage(5_000);
        SettledInvoiceJpaDto jpaDto = getDtoWithMessage(longMessage);
        assertThat(jpaDto.getKeysendMessage()).isEqualTo(longMessage);
    }

    @Test
    void createFromModel_with_keysend_message_below_length_limit() {
        String longMessage = longMessage(4_999);
        SettledInvoiceJpaDto jpaDto = getDtoWithMessage(longMessage);
        assertThat(jpaDto.getKeysendMessage()).isEqualTo(longMessage);
    }

    @Test
    void createFromModel_without_receivedVia_channel() {
        SettledInvoiceJpaDto jpaDto = SettledInvoiceJpaDto.createFromModel(SETTLED_INVOICE_NO_CHANNEL_ID);
        assertThat(jpaDto.getReceivedVia()).isEqualTo(0L);
    }

    @Test
    void toModel() {
        assertThat(SettledInvoiceJpaDto.createFromModel(SETTLED_INVOICE).toModel())
                .isEqualTo(SETTLED_INVOICE);
    }

    @Test
    void toModel_without_receivedVia_channel() {
        assertThat(SettledInvoiceJpaDto.createFromModel(SETTLED_INVOICE_NO_CHANNEL_ID).toModel())
                .isEqualTo(SETTLED_INVOICE_NO_CHANNEL_ID);
    }

    @Test
    void toModel_with_keysend_message() {
        assertThat(SettledInvoiceJpaDto.createFromModel(SETTLED_INVOICE_KEYSEND).toModel())
                .isEqualTo(SETTLED_INVOICE_KEYSEND);
    }

    private String longMessage(int length) {
        return "x".repeat(length);
    }

    private SettledInvoiceJpaDto getDtoWithMessage(String longMessage) {
        return SettledInvoiceJpaDto.createFromModel(new SettledInvoice(
                SettledInvoiceFixtures.ADD_INDEX,
                SettledInvoiceFixtures.SETTLE_INDEX,
                SettledInvoiceFixtures.SETTLE_DATE,
                SettledInvoiceFixtures.HASH,
                SettledInvoiceFixtures.AMOUNT_PAID,
                SettledInvoiceFixtures.MEMO,
                Optional.of(longMessage),
                Optional.of(CHANNEL_ID)
        ));
    }
}

