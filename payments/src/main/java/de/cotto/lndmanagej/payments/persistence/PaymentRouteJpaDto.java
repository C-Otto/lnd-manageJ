package de.cotto.lndmanagej.payments.persistence;

import de.cotto.lndmanagej.model.PaymentHop;
import de.cotto.lndmanagej.model.PaymentRoute;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
