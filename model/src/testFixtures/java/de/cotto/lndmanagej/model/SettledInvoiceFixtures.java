package de.cotto.lndmanagej.model;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_4;

public class SettledInvoiceFixtures {
    public static final ZonedDateTime SETTLE_DATE = ZonedDateTime.of(2021, 12, 2, 16, 4, 30, 0, ZoneOffset.UTC);
    public static final int ADD_INDEX = 2;
    public static final int ADD_INDEX_2 = 1;
    public static final int ADD_INDEX_3 = 3;
    public static final int SETTLE_INDEX = 1;
    public static final int SETTLE_INDEX_2 = 2;
    public static final int SETTLE_INDEX_3 = 3;
    public static final String HASH = "1234";
    public static final String HASH_2 = "aaa0";
    public static final String HASH_3 = "010101";
    public static final Coins AMOUNT_PAID = Coins.ofMilliSatoshis(123);
    public static final Coins AMOUNT_PAID_2 = Coins.ofMilliSatoshis(4_567);
    public static final String MEMO = "this is a memo";
    public static final String MEMO_2 = "yet another memo";
    public static final String KEYSEND_MESSAGE = "hello world";

    public static final SettledInvoice SETTLED_INVOICE = new SettledInvoice(
            ADD_INDEX,
            SETTLE_INDEX,
            SETTLE_DATE,
            HASH,
            AMOUNT_PAID,
            MEMO,
            Optional.empty(),
            Optional.of(CHANNEL_ID_2)
    );

    public static final SettledInvoice SETTLED_INVOICE_NO_CHANNEL_ID = new SettledInvoice(
            ADD_INDEX,
            SETTLE_INDEX,
            SETTLE_DATE,
            HASH,
            AMOUNT_PAID,
            MEMO,
            Optional.empty(),
            Optional.empty()
    );

    public static final SettledInvoice SETTLED_INVOICE_KEYSEND = new SettledInvoice(
            ADD_INDEX,
            SETTLE_INDEX,
            SETTLE_DATE,
            HASH,
            AMOUNT_PAID,
            MEMO,
            Optional.of(KEYSEND_MESSAGE),
            Optional.of(CHANNEL_ID_2)
    );

    public static final SettledInvoice SETTLED_INVOICE_2 = new SettledInvoice(
            ADD_INDEX_2,
            SETTLE_INDEX_2,
            SETTLE_DATE.plusSeconds(1),
            HASH_2,
            AMOUNT_PAID_2,
            MEMO_2,
            Optional.empty(),
            Optional.of(CHANNEL_ID)
    );

    public static final SettledInvoice SETTLED_INVOICE_3 = new SettledInvoice(
            ADD_INDEX_3,
            SETTLE_INDEX_3,
            SETTLE_DATE.plusSeconds(2),
            HASH_3,
            AMOUNT_PAID_2,
            MEMO_2,
            Optional.empty(),
            Optional.of(CHANNEL_ID_4)
    );
}
