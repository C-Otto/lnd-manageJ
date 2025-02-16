package de.cotto.lndmanagej.balances.persistence;

import de.cotto.lndmanagej.balances.Balances;
import de.cotto.lndmanagej.balances.BalancesDao;
import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.CoinsAndDuration;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

    @Override
    public Optional<Coins> getLocalBalanceMinimum(ChannelId channelId, int days) {
        return getLocalBalance(
                balancesRepository.findTopByChannelIdAndTimestampAfterOrderByLocalBalance(
                        channelId.getShortChannelId(),
                        getTimestamp(days)
                )
        );
    }

    @Override
    public Optional<Coins> getLocalBalanceMaximum(ChannelId channelId, int days) {
        long timestamp = getTimestamp(days);
        return getLocalBalance(
                balancesRepository.findTopByChannelIdAndTimestampAfterOrderByLocalBalanceDesc(
                        channelId.getShortChannelId(),
                        timestamp
                )
        );
    }

    @Override
    public Optional<CoinsAndDuration> getLocalBalanceAverageOpenChannel(ChannelId channelId, int days) {
        return getLocalBalanceAverage(channelId, days, true);
    }

    @Override
    public Optional<CoinsAndDuration> getLocalBalanceAverageClosedChannel(ChannelId channelId, int days) {
        return getLocalBalanceAverage(channelId, days, false);
    }

    private Optional<CoinsAndDuration> getLocalBalanceAverage(ChannelId channelId, int days, boolean open) {
        List<Balances> entries = getEntries(channelId, days);
        long totalSatoshis = 0;
        long totalMinutes = 0;
        LocalDateTime previous = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime maxAge = previous.minusDays(days);
        boolean first = true;
        for (Balances balances : entries) {
            LocalDateTime timestamp = balances.timestamp();
            if (timestamp.isBefore(maxAge)) {
                timestamp = maxAge;
            }
            if (!first || open) {
                long satoshis = balances.balanceInformation().localBalance().satoshis();
                Duration duration = Duration.between(timestamp, previous);
                long minutes = duration.toSeconds() / 60;
                totalSatoshis += satoshis * minutes;
                totalMinutes += minutes;
            }
            first = false;
            previous = timestamp;
        }
        if (totalMinutes == 0) {
            return Optional.empty();
        }
        Coins average = Coins.ofSatoshis(totalSatoshis / totalMinutes);
        return Optional.of(new CoinsAndDuration(average, Duration.ofMinutes(totalMinutes)));
    }

    private List<Balances> getEntries(ChannelId channelId, int days) {
        long shortChannelId = channelId.getShortChannelId();
        LocalDateTime threshold = LocalDateTime.now(ZoneOffset.UTC).minusDays(days);
        List<Balances> entries = new ArrayList<>();
        Iterator<Balances> iterator = balancesRepository.findByChannelIdOrderByTimestampDesc(shortChannelId)
                .map(BalancesJpaDto::toModel)
                .iterator();
        while (iterator.hasNext()) {
            Balances balances = iterator.next();
            entries.add(balances);
            if (balances.timestamp().isBefore(threshold)) {
                break;
            }
        }
        return entries;
    }

    private Optional<Coins> getLocalBalance(Optional<BalancesJpaDto> balances) {
        return balances
                .map(BalancesJpaDto::toModel)
                .map(Balances::balanceInformation)
                .map(BalanceInformation::localBalance);
    }

    private long getTimestamp(int daysInPast) {
        return ZonedDateTime.now(ZoneOffset.UTC).minusDays(daysInPast).toEpochSecond();
    }
}
