package de.cotto.lndmanagej.selfpayments.persistence;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.SelfPayment;
import de.cotto.lndmanagej.selfpayments.SelfPaymentsDao;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
    public List<SelfPayment> getSelfPaymentsToChannel(ChannelId channelId, Duration maxAge) {
        long minimumEpochSecond = getMinimumEpochSecond(maxAge);
        return toModel(repository.getSelfPaymentsToChannel(channelId.getShortChannelId(), minimumEpochSecond));
    }

    @Override
    public List<SelfPayment> getSelfPaymentsFromChannel(ChannelId channelId, Duration maxAge) {
        long minimumEpochSecond = getMinimumEpochSecond(maxAge);
        return toModel(repository.getSelfPaymentsFromChannel(channelId.getShortChannelId(), minimumEpochSecond));
    }

    private List<SelfPayment> toModel(List<SelfPaymentJpaDto> selfPayments) {
        return selfPayments.stream().map(SelfPaymentJpaDto::toModel).distinct().toList();
    }

    private long getMinimumEpochSecond(Duration maxAge) {
        return ZonedDateTime.now(ZoneOffset.UTC).minus(maxAge).toEpochSecond();
    }
}
