delete from payment_route_hops where not exists (select 1 from payments_routes where routes_route_id = payment_route_jpa_dto_route_id);
delete from payment_routes where not exists (select 1 from payments_routes where routes_route_id = route_id);
