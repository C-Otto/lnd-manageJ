package de.cotto.lndmanagej.payments;

import de.cotto.lndmanagej.grpc.GrpcPayments;
import de.cotto.lndmanagej.model.Payment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
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
    public void refresh() {
        List<Payment> payments;
        do {
            payments = grpcPayments.getPaymentsAfter(dao.getIndexOffset()).orElse(List.of());
            dao.save(payments);
        } while (payments.size() == grpcPayments.getLimit());
    }
}
