package de.cotto.lndmanagej.invoices.persistence;

import de.cotto.lndmanagej.invoices.SettledInvoicesDao;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.SettledInvoice;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Component
@Transactional
public class SettledInvoicesDaoImpl implements SettledInvoicesDao {
    private static final int ID_OF_SETTLED_INDEX_ENTITY = 0;

    private final SettledInvoicesRepository repository;
    private final SettledInvoicesIndexRepository settledInvoicesIndexRepository;

    public SettledInvoicesDaoImpl(
            SettledInvoicesRepository repository,
            SettledInvoicesIndexRepository settledInvoicesIndexRepository
    ) {
        this.repository = repository;
        this.settledInvoicesIndexRepository = settledInvoicesIndexRepository;
    }

    @Override
    public void save(Collection<SettledInvoice> settledInvoices) {
        List<SettledInvoiceJpaDto> converted = settledInvoices.stream()
                .map(SettledInvoiceJpaDto::createFromModel)
                .toList();
        repository.saveAll(converted);
        updateSettleIndexOffset();
    }

    @Override
    public void save(SettledInvoice settledInvoice) {
        repository.save(SettledInvoiceJpaDto.createFromModel(settledInvoice));
        updateSettleIndexOffset();
    }

    @Override
    public long getAddIndexOffset() {
        return repository.getMaxAddIndex();
    }

    @Override
    public long getSettleIndexOffset() {
        return repository.getMaxSettledIndexWithoutGaps(getKnownSettledIndex());
    }

    @Override
    public List<SettledInvoice> getInvoicesWithoutSelfPaymentsPaidVia(ChannelId channelId, Duration maxAge) {
        return repository.getInvoicesWithoutSelfPaymentsPaidVia(
                        channelId.getShortChannelId(),
                        getAfterEpochSeconds(maxAge)
                ).stream().map(SettledInvoiceJpaDto::toModel).toList();
    }

    private long getKnownSettledIndex() {
        return settledInvoicesIndexRepository.findByEntityId(ID_OF_SETTLED_INDEX_ENTITY)
                .map(SettledInvoicesIndexJpaDto::getAllSettledIndexOffset)
                .orElse(0L);
    }

    private void updateSettleIndexOffset() {
        SettledInvoicesIndexJpaDto entity = settledInvoicesIndexRepository
                .findByEntityId(ID_OF_SETTLED_INDEX_ENTITY)
                .orElseGet(SettledInvoicesIndexJpaDto::new);
        long newIndex = repository.getMaxSettledIndexWithoutGaps(entity.getAllSettledIndexOffset());
        entity.setAllSettledIndexOffset(newIndex);
        settledInvoicesIndexRepository.save(entity);
    }

    private long getAfterEpochSeconds(Duration maxAge) {
        return Instant.now().toEpochMilli() / 1_000 - maxAge.getSeconds();
    }
}
