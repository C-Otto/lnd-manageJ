package de.cotto.lndmanagej.service;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.SelfPayment;
import org.springframework.stereotype.Component;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Component
public class RebalanceService {
    private final SelfPaymentsService selfPaymentsService;
    private final ChannelService channelService;

    public RebalanceService(SelfPaymentsService selfPaymentsService, ChannelService channelService) {
        this.selfPaymentsService = selfPaymentsService;
        this.channelService = channelService;
    }

    @Timed
    public Set<SelfPayment> getRebalancesFromChannel(ChannelId channelId) {
        return selfPaymentsService.getSelfPaymentsFromChannel(channelId).stream()
                .filter(selfPayment -> memoMentionsChannel(selfPayment, channelId))
                .collect(toSet());
    }

    @Timed
    public Set<SelfPayment> getRebalancesFromPeer(Pubkey pubkey) {
        return channelService.getAllChannelsWith(pubkey).parallelStream()
                .map(Channel::getId)
                .map(this::getRebalancesFromChannel)
                .flatMap(Set::stream)
                .collect(toSet());
    }

    @Timed
    public Set<SelfPayment> getRebalancesToChannel(ChannelId channelId) {
        return selfPaymentsService.getSelfPaymentsToChannel(channelId).stream()
                .filter(this::memoDoesNotMentionFirstHopChannel)
                .collect(toSet());
    }

    @Timed
    public Set<SelfPayment> getRebalancesToPeer(Pubkey pubkey) {
        return channelService.getAllChannelsWith(pubkey).parallelStream()
                .map(Channel::getId)
                .map(this::getRebalancesToChannel)
                .flatMap(Set::stream)
                .collect(toSet());
    }

    private boolean memoMentionsChannel(SelfPayment selfPayment, ChannelId expectedChannel) {
        String memo = selfPayment.memo();
        return memo.contains(String.valueOf(expectedChannel.getShortChannelId()))
                || memo.contains(expectedChannel.getCompactForm())
                || memo.contains(expectedChannel.getCompactFormLnd());
    }

    private boolean memoDoesNotMentionFirstHopChannel(SelfPayment selfPayment) {
        ChannelId firstChannel = selfPayment.firstChannel().orElse(null);
        if (firstChannel == null) {
            return true;
        }
        return !memoMentionsChannel(selfPayment, firstChannel);
    }
}
