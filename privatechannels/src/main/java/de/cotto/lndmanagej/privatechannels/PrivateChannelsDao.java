package de.cotto.lndmanagej.privatechannels;

import de.cotto.lndmanagej.model.ChannelId;

import java.util.Optional;

public interface PrivateChannelsDao {
    @SuppressWarnings("PMD.LinguisticNaming")
    Optional<Boolean> isPrivate(ChannelId channelId);

    void savePrivateFlag(ChannelId channelId, boolean isPrivate);
}
