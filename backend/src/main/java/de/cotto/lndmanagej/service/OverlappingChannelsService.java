package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.OptionalInt;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Component
public class OverlappingChannelsService {
    private static final long EXPECTED_MINUTES_PER_BLOCK = 10L;

    private final ChannelService channelService;
    private final OwnNodeService ownNodeService;

    public OverlappingChannelsService(ChannelService channelService, OwnNodeService ownNodeService) {
        this.channelService = channelService;
        this.ownNodeService = ownNodeService;
    }

    public Set<ChannelId> getTransitiveOpenChannels(Pubkey peer) {
        Set<ChannelId> openChannelIds = channelService.getOpenChannelsWith(peer).stream()
                .map(Channel::getId)
                .collect(toSet());
        if (openChannelIds.isEmpty()) {
            return Set.of();
        }

        Set<ChannelId> result = new LinkedHashSet<>(openChannelIds);
        boolean addedChannel;
        do {
            int earliestOpenHeight = getEarliestOpenHeight(result).orElseThrow();
            Set<ChannelId> overlappingClosedChannels = getClosedChannelsClosedAtOrAfter(peer, earliestOpenHeight);
            addedChannel = result.addAll(overlappingClosedChannels);
        } while (addedChannel);

        return result;
    }

    public Duration getAgeOfEarliestOpenHeight(Set<ChannelId> candidates) {
        int earliestOpenHeight = getEarliestOpenHeight(candidates).orElseThrow();
        return getAgeOfBlock(earliestOpenHeight);
    }

    private OptionalInt getEarliestOpenHeight(Collection<ChannelId> channels) {
        return channels.stream().mapToInt(ChannelId::getBlockHeight).min();
    }

    private Duration getAgeOfBlock(int blockHeight) {
        int ageInBlocks = ownNodeService.getBlockHeight() - blockHeight;
        return Duration.ofMinutes(ageInBlocks * EXPECTED_MINUTES_PER_BLOCK);
    }

    private Set<ChannelId> getClosedChannelsClosedAtOrAfter(Pubkey peer, int blockHeight) {
        return channelService.getClosedChannelsWith(peer).stream()
                .filter(c -> c.getCloseHeight() >= blockHeight)
                .map(Channel::getId)
                .collect(toSet());
    }
}
