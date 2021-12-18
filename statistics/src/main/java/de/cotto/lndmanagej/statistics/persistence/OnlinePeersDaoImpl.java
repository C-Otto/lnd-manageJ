package de.cotto.lndmanagej.statistics.persistence;

import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.statistics.OnlinePeersDao;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@Transactional
class OnlinePeersDaoImpl implements OnlinePeersDao {
    private final OnlinePeersRepository repository;

    public OnlinePeersDaoImpl(OnlinePeersRepository repository) {
        this.repository = repository;
    }

    @Override
    public void saveOnlineStatus(Pubkey pubkey, boolean online, LocalDateTime timestamp) {
        repository.save(new OnlinePeerJpaDto(pubkey, online, timestamp));
    }

    @Override
    public Optional<Boolean> getMostRecentOnlineStatus(Pubkey pubkey) {
        return repository.findTopByPubkeyOrderByTimestampDesc(pubkey.toString()).map(OnlinePeerJpaDto::isOnline);
    }
}
