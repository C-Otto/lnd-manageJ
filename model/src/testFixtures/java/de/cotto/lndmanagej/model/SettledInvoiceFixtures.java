package de.cotto.lndmanagej.model;

import java.time.LocalDateTime;

public class SettledInvoiceFixtures {
    public static final LocalDateTime SETTLE_DATE = LocalDateTime.of(2021, 12, 2, 16, 4, 30);
    public static final int ADD_INDEX = 2;
    public static final int ADD_INDEX_2 = 1;
    public static final int SETTLE_INDEX = 1;
    public static final int SETTLE_INDEX_2 = 2;
    public static final String HASH = "1234";
    public static final String HASH_2 = "aaa0";
    public static final Coins AMOUNT_PAID = Coins.ofMilliSatoshis(123);
    public static final Coins AMOUNT_PAID_2 = Coins.ofMilliSatoshis(4_567);
    public static final String MEMO = "this is a memo";
    public static final String MEMO_2 = "yet another memo";

    public static final SettledInvoice SETTLED_INVOICE = new SettledInvoice(
            ADD_INDEX,
            SETTLE_INDEX,
            SETTLE_DATE,
            HASH,
            AMOUNT_PAID,
            MEMO
    );

    public static final SettledInvoice SETTLED_INVOICE_2 = new SettledInvoice(
            ADD_INDEX_2,
            SETTLE_INDEX_2,
            SETTLE_DATE.plusSeconds(1),
            HASH_2,
            AMOUNT_PAID_2,
            MEMO_2
    );
}
