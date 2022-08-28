package de.cotto.lndmanagej.payments;

import de.cotto.lndmanagej.grpc.GrpcPayments;
import de.cotto.lndmanagej.model.Payment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class Payments {
    private final GrpcPayments grpcPayments;
    private final PaymentsDao dao;

    public Payments(GrpcPayments grpcPayments, PaymentsDao dao) {
        this.grpcPayments = grpcPayments;
        this.dao = dao;
    }

    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void loadNewSettledPayments() {
        List<Payment> payments;
        do {
            payments = grpcPayments.getPaymentsAfter(dao.getIndexOffset()).orElse(List.of());
            dao.save(payments);
        } while (payments.size() == grpcPayments.getLimit());
    }

    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void loadOldSettledPayments() {
        List<Optional<Payment>> paymentOptionals;
        do {
            long offsetSettledPayments = dao.getAllSettledIndexOffset();
            long offsetKnownPayments = dao.getIndexOffset();
            if (offsetKnownPayments == offsetSettledPayments) {
                return;
            }
            paymentOptionals = grpcPayments.getAllPaymentsAfter(offsetSettledPayments).orElse(List.of());
            long maxIndex = getMaxIndexAllSettled(paymentOptionals);
            dao.save(paymentOptionals.stream().flatMap(Optional::stream).toList());
            dao.setAllSettledIndexOffset(maxIndex);
        } while (paymentOptionals.size() == grpcPayments.getLimit());

    }

    private static long getMaxIndexAllSettled(List<Optional<Payment>> paymentOptionals) {
        return paymentOptionals.stream()
                .takeWhile(Optional::isPresent)
                .flatMap(Optional::stream)
                .mapToLong(Payment::index)
                .max()
                .orElse(0L);
    }
}
