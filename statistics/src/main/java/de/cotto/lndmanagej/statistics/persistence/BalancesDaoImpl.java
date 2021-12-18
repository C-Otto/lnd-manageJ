package de.cotto.lndmanagej.statistics.persistence;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.statistics.Balances;
import de.cotto.lndmanagej.statistics.BalancesDao;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Optional;

@Component
@Transactional
class BalancesDaoImpl implements BalancesDao {
    private final BalancesRepository balancesRepository;

    public BalancesDaoImpl(BalancesRepository balancesRepository) {
        this.balancesRepository = balancesRepository;
    }

    @Override
    public void saveBalances(Balances balances) {
        balancesRepository.save(BalancesJpaDto.fromModel(balances));
    }

    @Override
    public Optional<Balances> getMostRecentBalances(ChannelId channelId) {
        return balancesRepository.findTopByChannelIdOrderByTimestampDesc(channelId.getShortChannelId())
                .map(BalancesJpaDto::toModel);
    }
}
