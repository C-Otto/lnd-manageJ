package de.cotto.lndmanagej.payments;

import de.cotto.lndmanagej.model.Payment;

import java.util.Collection;

public interface PaymentsDao {
    void save(Collection<Payment> payments);

    void save(Payment payment);

    long getIndexOffset();

    long getAllSettledIndexOffset();

    void setAllSettledIndexOffset(long offset);
}
