package de.cotto.lndmanagej.model;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public record SelfPayment(
        String memo,
        ZonedDateTime settleDate,
        Coins amountPaid,
        Coins fees,
        List<SelfPaymentRoute> routes
) {
    public SelfPayment(Payment payment, SettledInvoice settledInvoice) {
        this(
                settledInvoice.memo(),
                settledInvoice.settleDate(),
                settledInvoice.amountPaid(),
                payment.fees(),
                getRoutes(payment)
        );
    }

    private static List<SelfPaymentRoute> getRoutes(Payment payment) {
        return payment.routes().stream()
                .map(SelfPaymentRoute::create)
                .flatMap(Optional::stream)
                .toList();
    }
}
