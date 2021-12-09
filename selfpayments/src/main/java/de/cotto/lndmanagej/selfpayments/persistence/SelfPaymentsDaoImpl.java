package de.cotto.lndmanagej.selfpayments.persistence;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.SelfPayment;
import de.cotto.lndmanagej.selfpayments.SelfPaymentsDao;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;

@Component
@Transactional
public class SelfPaymentsDaoImpl implements SelfPaymentsDao {
    private final SelfPaymentsRepository repository;

    public SelfPaymentsDaoImpl(SelfPaymentsRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<SelfPayment> getAllSelfPayments() {
        return toModel(repository.getAllSelfPayments());
    }

    @Override
    public List<SelfPayment> getSelfPaymentsToChannel(ChannelId channelId) {
        return toModel(repository.getSelfPaymentsToChannel(channelId.getShortChannelId()));
    }

    @Override
    public List<SelfPayment> getSelfPaymentsFromChannel(ChannelId channelId) {
        return toModel(repository.getSelfPaymentsFromChannel(channelId.getShortChannelId()));
    }

    private List<SelfPayment> toModel(List<SelfPaymentJpaDto> selfPayments) {
        return selfPayments.stream().map(SelfPaymentJpaDto::toModel).toList();
    }
}
