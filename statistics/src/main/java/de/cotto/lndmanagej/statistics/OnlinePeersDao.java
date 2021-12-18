package de.cotto.lndmanagej.statistics;

import de.cotto.lndmanagej.model.Pubkey;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OnlinePeersDao {
    void saveOnlineStatus(Pubkey pubkey, boolean online, LocalDateTime timestamp);

    Optional<Boolean> getMostRecentOnlineStatus(Pubkey pubkey);
}
