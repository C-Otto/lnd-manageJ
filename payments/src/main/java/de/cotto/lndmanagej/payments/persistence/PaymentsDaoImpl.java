package de.cotto.lndmanagej.payments.persistence;

import de.cotto.lndmanagej.model.Payment;
import de.cotto.lndmanagej.payments.PaymentsDao;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;

@Component
@Transactional
public class PaymentsDaoImpl implements PaymentsDao {
    private static final int ID_OF_SETTLED_INDEX_ENTITY = 0;

    private final PaymentsRepository repository;
    private final SettledPaymentIndexRepository settledPaymentIndexRepository;

    public PaymentsDaoImpl(PaymentsRepository repository, SettledPaymentIndexRepository settledPaymentIndexRepository) {
        this.repository = repository;
        this.settledPaymentIndexRepository = settledPaymentIndexRepository;
    }

    @Override
    public void save(Collection<Payment> payments) {
        List<PaymentJpaDto> converted = payments.stream()
                .map(PaymentJpaDto::createFromModel)
                .toList();
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

    @Override
    public long getAllSettledIndexOffset() {
        return settledPaymentIndexRepository.findByEntityId(ID_OF_SETTLED_INDEX_ENTITY)
                .map(SettledPaymentIndexJpaDto::getAllSettledIndexOffset)
                .orElse(0L);
    }

    @Override
    public void setAllSettledIndexOffset(long offset) {
        SettledPaymentIndexJpaDto entity = settledPaymentIndexRepository
                .findByEntityId(ID_OF_SETTLED_INDEX_ENTITY)
                .orElseGet(SettledPaymentIndexJpaDto::new);
        entity.setAllSettledIndexOffset(offset);
        settledPaymentIndexRepository.save(entity);
    }
}
