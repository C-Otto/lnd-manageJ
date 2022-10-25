alter table payment_route_hops add column first boolean;
delete from payment_route_hops where hops_order != 0 and hops_order != (select max(hops_order) from payment_route_hops b where payment_route_jpa_dto_route_id = b.payment_route_jpa_dto_route_id);
update payment_route_hops set first = true where hops_order = 0;
update payment_route_hops set first = false where hops_order != 0;
ALTER TABLE payment_route_hops ALTER COLUMN first SET NOT NULL;

