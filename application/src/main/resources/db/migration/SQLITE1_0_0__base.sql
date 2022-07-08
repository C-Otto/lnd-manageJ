create table if not exists balances
(
    channel_id      bigint not null,
    timestamp       bigint not null,
    local_balance   bigint not null,
    local_reserved  bigint not null,
    remote_balance  bigint not null,
    remote_reserved bigint not null,
    primary key (channel_id, timestamp)
);

create table if not exists dummy
(
    dummy_id bigint not null
        primary key
);

create table if not exists forwarding_events
(
    event_index      integer not null
        primary key,
    amount_incoming  bigint  not null,
    amount_outgoing  bigint  not null,
    channel_incoming bigint  not null,
    channel_outgoing bigint  not null,
    timestamp        bigint  not null
);

create index if not exists idxf6mchjaiqb65pytncc9l5fiw0
    on forwarding_events (channel_incoming);

create index if not exists idx7ki7iilyupdjktdx80pd347au
    on forwarding_events (channel_outgoing);

create table if not exists online_peers
(
    pubkey    varchar(255) not null,
    timestamp bigint       not null,
    online    boolean      not null,
    primary key (pubkey, timestamp)
);

create table if not exists payment_routes
(
    route_id bigint not null
        primary key
);

create table if not exists payment_route_hops
(
    payment_route_jpa_dto_route_id bigint  not null
        constraint fkqitidfjnebt7wmd163yrerqi7
            references payment_routes,
    amount                         bigint  not null,
    channel_id                     bigint  not null,
    hops_order                     integer not null,
    primary key (payment_route_jpa_dto_route_id, hops_order)
);

create table if not exists payments
(
    payment_index bigint not null
        primary key,
    fees          bigint not null,
    hash          varchar(255),
    timestamp     bigint not null,
    value         bigint not null
);

create index if not exists idx8fs5vewf99rse8wl890u7ruuw
    on payments (hash);

create table if not exists payments_routes
(
    payment_jpa_dto_payment_index bigint  not null
        constraint fks3qip03boj5c7b72542q1xbch
            references payments,
    routes_route_id               bigint  not null
        constraint uk_r327ov1rqxv7wri8bpjgnkch1
            unique
        constraint fk9itn1dvdn75y6ifc1k7cjwjxv
            references payment_routes,
    routes_order                  integer not null,
    primary key (payment_jpa_dto_payment_index, routes_order)
);

create table if not exists private_channels
(
    channel_id bigint  not null
        primary key,
    is_private boolean not null
);

create table if not exists settled_invoices
(
    add_index       bigint not null
        primary key,
    amount_paid     bigint not null,
    hash            varchar(255),
    keysend_message varchar(5000),
    memo            varchar(255),
    received_via    bigint not null,
    settle_date     bigint not null,
    settle_index    bigint not null
        constraint uk4xcaoj9cf5dd0l9ldjeo376r6
            unique
);

create index if not exists idxmwacjiud4yhc2etybe1ql767c
    on settled_invoices (hash);

create index if not exists idx94fge22n3rfhpqd5m2l9np722
    on settled_invoices (settle_date);

create table if not exists transactions
(
    hash              varchar(255) not null
        primary key,
    block_height      integer      not null,
    fees              bigint       not null,
    position_in_block integer      not null
);

