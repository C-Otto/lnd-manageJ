package de.cotto.lndmanagej.payments.persistence;

import de.cotto.lndmanagej.model.PaymentRoute;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "payment_routes")
public class PaymentRouteJpaDto {
    @Id
    @GeneratedValue
    @SuppressWarnings("unused")
    private long routeId;

    @Nullable
    @OrderColumn
    @ElementCollection
    @CollectionTable(name = "payment_route_hops")
    private List<PaymentHopJpaDto> hops;

    @SuppressWarnings("unused")
    public PaymentRouteJpaDto() {
        // for JPA
    }

    public PaymentRouteJpaDto(@Nonnull List<PaymentHopJpaDto> hops) {
        this.hops = hops;
    }

    public static PaymentRouteJpaDto createFromModel(PaymentRoute paymentRoute) {
        List<PaymentHopJpaDto> hops = paymentRoute.hops().stream().map(PaymentHopJpaDto::createFromModel).toList();
        return new PaymentRouteJpaDto(hops);
    }

    public PaymentRoute toModel() {
        return new PaymentRoute(Objects.requireNonNull(hops).stream().map(PaymentHopJpaDto::toModel).toList());
    }
}
