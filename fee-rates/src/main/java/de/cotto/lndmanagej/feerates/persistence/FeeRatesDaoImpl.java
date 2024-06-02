package de.cotto.lndmanagej.feerates.persistence;

import de.cotto.lndmanagej.feerates.FeeRates;
import de.cotto.lndmanagej.feerates.FeeRatesDao;
import de.cotto.lndmanagej.model.ChannelId;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Transactional
class FeeRatesDaoImpl implements FeeRatesDao {
    private final FeeRatesRepository feeRatesRepository;

    public FeeRatesDaoImpl(FeeRatesRepository feeRatesRepository) {
        this.feeRatesRepository = feeRatesRepository;
    }

    @Override
    public void saveFeeRates(FeeRates feeRates) {
        feeRatesRepository.save(FeeRatesJpaDto.fromModel(feeRates));
    }

    @Override
    public Optional<FeeRates> getMostRecentFeeRates(ChannelId channelId) {
        return feeRatesRepository.findTopByChannelIdOrderByTimestampDesc(channelId.getShortChannelId())
                .map(FeeRatesJpaDto::toModel);
    }
}
