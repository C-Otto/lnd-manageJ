package de.cotto.lndmanagej.service;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.SelfPayment;
import org.springframework.stereotype.Component;

@Component
public class OffChainCostService {
    private final SelfPaymentsService selfPaymentsService;
    private final ChannelService channelService;

    public OffChainCostService(SelfPaymentsService selfPaymentsService, ChannelService channelService) {
        this.selfPaymentsService = selfPaymentsService;
        this.channelService = channelService;
    }

    @Timed
    public Coins getRebalanceSourceCostsForChannel(ChannelId channelId) {
        return selfPaymentsService.getSelfPaymentsFromChannel(channelId).stream()
                .filter(selfPayment -> memoMentionsChannel(selfPayment, channelId))
                .map(SelfPayment::fees)
                .reduce(Coins.NONE, Coins::add);
    }

    @Timed
    public Coins getRebalanceSourceCostsForPeer(Pubkey pubkey) {
        return channelService.getAllChannelsWith(pubkey).parallelStream()
                .map(Channel::getId)
                .map(this::getRebalanceSourceCostsForChannel)
                .reduce(Coins.NONE, Coins::add);
    }

    @Timed
    public Coins getRebalanceTargetCostsForChannel(ChannelId channelId) {
        return selfPaymentsService.getSelfPaymentsToChannel(channelId).stream()
                .filter(this::memoDoesNotMentionFirstHopChannel)
                .map(SelfPayment::fees)
                .reduce(Coins.NONE, Coins::add);
    }

    @Timed
    public Coins getRebalanceTargetCostsForPeer(Pubkey pubkey) {
        return channelService.getAllChannelsWith(pubkey).parallelStream()
                .map(Channel::getId)
                .map(this::getRebalanceTargetCostsForChannel)
                .reduce(Coins.NONE, Coins::add);
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
