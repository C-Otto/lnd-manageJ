drop index channel_id_and_hop_order_index;
alter table PAYMENT_ROUTE_HOPS drop constraint constraint_4;
alter table PAYMENT_ROUTE_HOPS drop column HOPS_ORDER;
create index if not exists channel_id_and_first_index
    on payment_route_hops (channel_id, first);
drop index channel_id_index;
