package de.cotto.lndmanagej.selfpayments.persistence;

import de.cotto.lndmanagej.invoices.persistence.SettledInvoiceJpaDto;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.SelfPayment;
import de.cotto.lndmanagej.model.SelfPaymentRoute;
import de.cotto.lndmanagej.payments.persistence.PaymentJpaDto;
import de.cotto.lndmanagej.payments.persistence.PaymentRouteJpaDto;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static java.util.Objects.requireNonNull;

public record SelfPaymentJpaDto(
        SettledInvoiceJpaDto invoice,
        PaymentJpaDto payment
) {
    public SelfPayment toModel() {
        ZonedDateTime dateTime = LocalDateTime.ofEpochSecond(invoice.getSettleDate(), 0, UTC).atZone(UTC);
        return new SelfPayment(
                invoice.getMemo(),
                dateTime,
                Coins.ofMilliSatoshis(invoice.getAmountPaid()),
                Coins.ofMilliSatoshis(payment.getFees()),
                requireNonNull(payment.getRoutes()).stream()
                        .map(PaymentRouteJpaDto::toModel)
                        .map(SelfPaymentRoute::create)
                        .flatMap(Optional::stream)
                        .toList()
        );
    }

}
