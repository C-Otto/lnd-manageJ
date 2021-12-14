package de.cotto.lndmanagej.payments.persistence;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Payment;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "payments", indexes = {@Index(columnList = "hash")})
public class PaymentJpaDto {
    @Id
    private long paymentIndex;

    @Nullable
    private String hash;
    private long timestamp;
    private long value;
    private long fees;

    @Nullable
    @OneToMany(cascade = CascadeType.ALL)
    private List<PaymentRouteJpaDto> routes;

    public PaymentJpaDto() {
        // for JPA
    }

    public static PaymentJpaDto createFromModel(Payment payment) {
        PaymentJpaDto jpaDto = new PaymentJpaDto();
        jpaDto.paymentIndex = payment.index();
        jpaDto.hash = payment.paymentHash();
        jpaDto.timestamp = payment.creationDateTime().toInstant(ZoneOffset.UTC).toEpochMilli();
        jpaDto.value = payment.value().milliSatoshis();
        jpaDto.fees = payment.fees().milliSatoshis();
        jpaDto.routes = payment.routes().stream().map(PaymentRouteJpaDto::createFromModel).toList();
        return jpaDto;
    }

    public Payment toModel() {
        long epochSecond = timestamp / 1_000;
        int milliseconds = (int) (timestamp % 1_000);
        int nanoseconds = milliseconds * 1_000_000;
        return new Payment(
                paymentIndex,
                Objects.requireNonNull(hash),
                LocalDateTime.ofEpochSecond(epochSecond, nanoseconds, ZoneOffset.UTC),
                Coins.ofMilliSatoshis(value),
                Coins.ofMilliSatoshis(fees),
                Objects.requireNonNull(routes).stream().map(PaymentRouteJpaDto::toModel).toList()
        );
    }

    public long getPaymentIndex() {
        return paymentIndex;
    }

    public String getHash() {
        return Objects.requireNonNull(hash);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getValue() {
        return value;
    }

    public long getFees() {
        return fees;
    }

    @Nullable
    public List<PaymentRouteJpaDto> getRoutes() {
        return routes;
    }
}
