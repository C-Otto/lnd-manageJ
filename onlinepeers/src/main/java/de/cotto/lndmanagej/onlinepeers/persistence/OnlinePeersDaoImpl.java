package de.cotto.lndmanagej.onlinepeers.persistence;

import de.cotto.lndmanagej.model.OnlineStatus;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.onlinepeers.OnlinePeersDao;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Component
@Transactional
class OnlinePeersDaoImpl implements OnlinePeersDao {
    private final OnlinePeersRepository repository;

    public OnlinePeersDaoImpl(OnlinePeersRepository repository) {
        this.repository = repository;
    }

    @Override
    public void saveOnlineStatus(Pubkey pubkey, boolean online, ZonedDateTime timestamp) {
        repository.save(new OnlinePeerJpaDto(pubkey, online, timestamp));
    }

    @Override
    public Optional<OnlineStatus> getMostRecentOnlineStatus(Pubkey pubkey) {
        return repository.findTopByPubkeyOrderByTimestampDesc(pubkey.toString()).map(OnlinePeerJpaDto::toModel);
    }

    @Override
    public List<OnlineStatus> getAllForPeerUpToAgeInDays(Pubkey pubkey, int maximumAgeInDays) {
        ZonedDateTime threshold = ZonedDateTime.now(ZoneOffset.UTC).minusDays(maximumAgeInDays);
        List<OnlineStatus> result = new ArrayList<>();
        Iterator<OnlineStatus> iterator = repository.findByPubkeyOrderByTimestampDesc(pubkey.toString())
                .map(OnlinePeerJpaDto::toModel)
                .iterator();
        while (iterator.hasNext()) {
            OnlineStatus onlineStatus = iterator.next();
            result.add(onlineStatus);
            if (onlineStatus.since().isBefore(threshold)) {
                break;
            }
        }
        return result;
    }
}
