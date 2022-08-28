package de.cotto.lndmanagej.invoices;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.SettledInvoice;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

public interface SettledInvoicesDao {
    void save(Collection<SettledInvoice> settledInvoices);

    void save(SettledInvoice settledInvoice);

    long getAddIndexOffset();

    long getSettleIndexOffset();

    List<SettledInvoice> getInvoicesWithoutSelfPaymentsPaidVia(ChannelId channelId, Duration maxAge);
}
