drop index idxf6mchjaiqb65pytncc9l5fiw0;

create index if not exists idxf6mchjaiqb65pytncc9l5fiw0
    on forwarding_events (channel_incoming, timestamp);

drop index idx7ki7iilyupdjktdx80pd347au;

create index if not exists idx7ki7iilyupdjktdx80pd347au
    on forwarding_events (channel_outgoing, timestamp);