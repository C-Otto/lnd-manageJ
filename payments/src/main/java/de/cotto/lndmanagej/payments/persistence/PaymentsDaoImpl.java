package de.cotto.lndmanagej.payments.persistence;

import de.cotto.lndmanagej.model.Payment;
import de.cotto.lndmanagej.payments.PaymentsDao;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
@Transactional
public class PaymentsDaoImpl implements PaymentsDao {
    private final PaymentsRepository repository;

    public PaymentsDaoImpl(PaymentsRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Collection<Payment> payments) {
        List<PaymentJpaDto> converted = payments.stream()
                .map(PaymentJpaDto::createFromModel)
                .collect(toList());
        repository.saveAll(converted);
    }

    @Override
    public void save(Payment payment) {
        repository.save(PaymentJpaDto.createFromModel(payment));
    }

    @Override
    public long getIndexOffset() {
        return repository.getMaxIndex();
    }
}
