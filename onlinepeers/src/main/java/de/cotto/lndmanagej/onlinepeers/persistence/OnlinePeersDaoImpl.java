package de.cotto.lndmanagej.onlinepeers.persistence;

import de.cotto.lndmanagej.model.OnlineStatus;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.onlinepeers.OnlinePeersDao;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
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
}
