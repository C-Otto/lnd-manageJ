delete from payment_route_hops h where not exists (select 1 from payments_routes where routes_route_id = h.payment_route_jpa_dto_route_id);
delete from payment_routes r where not exists (select 1 from payments_routes where routes_route_id = r.route_id);
