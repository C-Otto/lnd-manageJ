create index if not exists channel_id_and_hop_order_index
    ON payment_route_hops (channel_id, hops_order);
