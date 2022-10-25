package de.cotto.lndmanagej.payments.persistence;

import de.cotto.lndmanagej.model.PaymentHop;
import de.cotto.lndmanagej.model.PaymentRoute;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Entity
@Table(name = "payment_routes")
public class PaymentRouteJpaDto {
    @Id
    @GeneratedValue
    @SuppressWarnings("unused")
    private long routeId;

    @Nullable
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
        List<PaymentHopJpaDto> hops = Stream.of(paymentRoute.firstHop(), paymentRoute.lastHop())
                .filter(Optional::isPresent)
                .flatMap(Optional::stream)
                .map(PaymentHopJpaDto::createFromModel)
                .toList();
        return new PaymentRouteJpaDto(hops);
    }

    public PaymentRoute toModel() {
        List<PaymentHop> convertedHops = Objects.requireNonNull(hops).stream().map(PaymentHopJpaDto::toModel).toList();
        Optional<PaymentHop> firstHop = convertedHops.stream().filter(PaymentHop::first).findFirst();
        Optional<PaymentHop> lastHop = convertedHops.stream().filter(PaymentHop::last).findFirst();
        return new PaymentRoute(firstHop, lastHop);
    }
}
