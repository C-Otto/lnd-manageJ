CREATE TABLE IF NOT EXISTS SETTLED_INVOICE_RECEIVED_VIA
(
    "settled_invoice_jpa_dto_add_index" BIGINT NOT NULL,
    "amount"                            BIGINT NOT NULL,
    "channel_id"                        BIGINT NOT NULL,
    CONSTRAINT fk_add_index
        FOREIGN KEY("settled_invoice_jpa_dto_add_index") REFERENCES SETTLED_INVOICES (add_index)

);

CREATE INDEX add_index_index ON SETTLED_INVOICE_RECEIVED_VIA ("settled_invoice_jpa_dto_add_index");

-- noinspection SqlWithoutWhere
DELETE FROM SETTLED_INVOICES;
