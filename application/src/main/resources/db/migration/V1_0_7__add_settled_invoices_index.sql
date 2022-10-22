create table SETTLED_INVOICES_INDEX
(
    ENTITY_ID                BIGINT not null
        primary key,
    ALL_SETTLED_INDEX_OFFSET BIGINT not null
);

