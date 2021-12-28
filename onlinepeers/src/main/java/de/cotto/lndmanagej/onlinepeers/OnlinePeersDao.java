package de.cotto.lndmanagej.onlinepeers;

import de.cotto.lndmanagej.model.OnlineStatus;
import de.cotto.lndmanagej.model.Pubkey;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface OnlinePeersDao {
    void saveOnlineStatus(Pubkey pubkey, boolean online, ZonedDateTime timestamp);

    Optional<OnlineStatus> getMostRecentOnlineStatus(Pubkey pubkey);

    List<OnlineStatus> getAllForPeerUpToAgeInDays(Pubkey pubkey, int dayThreshold);
}
