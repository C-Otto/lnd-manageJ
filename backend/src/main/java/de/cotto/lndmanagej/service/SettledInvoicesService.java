package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.invoices.SettledInvoicesDao;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.SettledInvoice;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class SettledInvoicesService {
    private final SettledInvoicesDao dao;

    public SettledInvoicesService(SettledInvoicesDao dao) {
        this.dao = dao;
    }

    public Coins getAmountReceivedViaChannel(ChannelId channelId, Duration maxAge) {
        return dao.getInvoicesPaidVia(channelId, maxAge).stream()
                .map(SettledInvoice::receivedVia)
                .map(receivedVia -> receivedVia.getOrDefault(channelId, Coins.NONE))
                .reduce(Coins.NONE, Coins::add);
    }
}
