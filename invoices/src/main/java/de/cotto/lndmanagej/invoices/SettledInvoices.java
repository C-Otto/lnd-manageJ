package de.cotto.lndmanagej.invoices;

import de.cotto.lndmanagej.grpc.GrpcInvoices;
import de.cotto.lndmanagej.model.SettledInvoice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

@Component
public class SettledInvoices {
    private final GrpcInvoices grpcInvoices;
    private final SettledInvoicesDao dao;

    public SettledInvoices(GrpcInvoices grpcInvoices, SettledInvoicesDao dao) {
        this.grpcInvoices = grpcInvoices;
        this.dao = dao;
    }

    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void refresh() {
        List<SettledInvoice> settledInvoices;
        do {
            settledInvoices = grpcInvoices.getSettledInvoicesAfter(dao.getAddIndexOffset()).orElse(List.of());
            dao.save(settledInvoices.stream().filter(SettledInvoice::isValid).collect(toList()));
        } while (settledInvoices.size() == grpcInvoices.getLimit());

        grpcInvoices.getNewSettledInvoicesAfter(dao.getSettleIndexOffset())
                .filter(SettledInvoice::isValid)
                .forEach(dao::save);
    }
}