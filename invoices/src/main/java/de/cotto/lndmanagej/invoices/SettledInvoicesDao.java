package de.cotto.lndmanagej.invoices;

import de.cotto.lndmanagej.model.SettledInvoice;

import java.util.Collection;

public interface SettledInvoicesDao {
    void save(Collection<SettledInvoice> settledInvoices);

    void save(SettledInvoice settledInvoice);

    long getAddIndexOffset();

    long getSettleIndexOffset();
}
