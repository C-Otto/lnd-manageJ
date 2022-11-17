package de.cotto.lndmanagej.payments;

import de.cotto.lndmanagej.grpc.GrpcPayments;
import de.cotto.lndmanagej.model.Payment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;

@Component
public class Payments {
    private final GrpcPayments grpcPayments;
    private final PaymentsDao dao;
    private static final Object SAVE_PAYMENTS_LOCK = new Object();

    public Payments(GrpcPayments grpcPayments, PaymentsDao dao) {
        this.grpcPayments = grpcPayments;
        this.dao = dao;
    }

    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void loadNewSettledPayments() {
        List<Payment> payments;
        do {
            synchronized (SAVE_PAYMENTS_LOCK) {
                payments = grpcPayments.getCompletePaymentsAfter(dao.getIndexOffset()).orElse(List.of());
                dao.save(payments);
            }
        } while (payments.size() == grpcPayments.getLimit());
    }

    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void loadOldSettledPayments() {
        List<Optional<Payment>> paymentOptionals;
        OptionalLong maxIndex;
        do {
            synchronized (SAVE_PAYMENTS_LOCK) {
                long offsetSettledPayments = dao.getAllSettledIndexOffset();
                long offsetKnownPayments = dao.getIndexOffset();
                if (offsetKnownPayments == offsetSettledPayments) {
                    return;
                }
                paymentOptionals =
                        grpcPayments.getCompleteAndPendingPaymentsAfter(offsetSettledPayments).orElse(List.of());
                maxIndex = getMaxIndexAllSettled(paymentOptionals);
                dao.save(paymentOptionals.stream().flatMap(Optional::stream).toList());
                maxIndex.ifPresent(dao::setAllSettledIndexOffset);
            }
        } while (paymentOptionals.size() == grpcPayments.getLimit() && maxIndex.isPresent());
    }

    private static OptionalLong getMaxIndexAllSettled(List<Optional<Payment>> paymentOptionals) {
        return paymentOptionals.stream()
                .takeWhile(Optional::isPresent)
                .flatMap(Optional::stream)
                .mapToLong(Payment::index)
                .max();
    }
}
