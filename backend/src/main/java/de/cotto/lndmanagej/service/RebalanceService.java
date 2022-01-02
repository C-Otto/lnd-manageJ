package de.cotto.lndmanagej.service;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.RebalanceReport;
import de.cotto.lndmanagej.model.SelfPayment;
import org.springframework.stereotype.Component;

import java.util.Collection;
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
    public RebalanceReport getReportForChannel(ChannelId channelId) {
        return new RebalanceReport(
                getSourceCostsForChannel(channelId),
                getAmountFromChannel(channelId),
                getTargetCostsForChannel(channelId),
                getAmountToChannel(channelId),
                getSupportAsSourceAmountFromChannel(channelId),
                getSupportAsTargetAmountToChannel(channelId)
        );
    }

    @Timed
    public RebalanceReport getReportForPeer(Pubkey pubkey) {
        return new RebalanceReport(
                getSourceCostsForPeer(pubkey),
                getAmountFromPeer(pubkey),
                getTargetCostsForPeer(pubkey),
                getAmountToPeer(pubkey),
                getSupportAsSourceAmountFromPeer(pubkey),
                getSupportAsTargetAmountToPeer(pubkey)
        );
    }

    @Timed
    public Coins getSourceCostsForChannel(ChannelId channelId) {
        return getSumOfFees(getRebalancesFromChannel(channelId));
    }

    @Timed
    public Coins getSourceCostsForPeer(Pubkey pubkey) {
        return getSumOfFees(getRebalancesFromPeer(pubkey));
    }

    @Timed
    public Coins getTargetCostsForChannel(ChannelId channelId) {
        return getSumOfFees(getRebalancesToChannel(channelId));
    }

    @Timed
    public Coins getTargetCostsForPeer(Pubkey pubkey) {
        return getSumOfFees(getRebalancesToPeer(pubkey));
    }

    @Timed
    public Coins getAmountFromChannel(ChannelId channelId) {
        return getSumOfAmountPaid(getRebalancesFromChannel(channelId));
    }

    @Timed
    public Coins getAmountFromPeer(Pubkey pubkey) {
        return getSumOfAmountPaid(getRebalancesFromPeer(pubkey));
    }

    @Timed
    public Coins getAmountToChannel(ChannelId channelId) {
        return getSumOfAmountPaid(getRebalancesToChannel(channelId));
    }

    @Timed
    public Coins getAmountToPeer(Pubkey pubkey) {
        return getSumOfAmountPaid(getRebalancesToPeer(pubkey));
    }

    @Timed
    public Coins getSupportAsSourceAmountFromChannel(ChannelId channelId) {
        Coins amountSourceTotal = getSumOfAmountPaid(selfPaymentsService.getSelfPaymentsFromChannel(channelId));
        Coins amountRebalanceSource = getAmountFromChannel(channelId);
        return amountSourceTotal.subtract(amountRebalanceSource);
    }

    @Timed
    public Coins getSupportAsTargetAmountToChannel(ChannelId channelId) {
        Coins amountTargetTotal = getSumOfAmountPaid(selfPaymentsService.getSelfPaymentsToChannel(channelId));
        Coins amountRebalanceTarget = getAmountToChannel(channelId);
        return amountTargetTotal.subtract(amountRebalanceTarget);
    }

    @Timed
    public Coins getSupportAsSourceAmountFromPeer(Pubkey pubkey) {
        return channelService.getAllChannelsWith(pubkey).parallelStream()
                .map(Channel::getId)
                .map(this::getSupportAsSourceAmountFromChannel)
                .reduce(Coins.NONE, Coins::add);
    }

    @Timed
    public Coins getSupportAsTargetAmountToPeer(Pubkey pubkey) {
        return channelService.getAllChannelsWith(pubkey).parallelStream()
                .map(Channel::getId)
                .map(this::getSupportAsTargetAmountToChannel)
                .reduce(Coins.NONE, Coins::add);
    }

    private Set<SelfPayment> getRebalancesFromChannel(ChannelId channelId) {
        return selfPaymentsService.getSelfPaymentsFromChannel(channelId).stream()
                .filter(selfPayment -> memoMentionsChannel(selfPayment, channelId))
                .collect(toSet());
    }

    private Set<SelfPayment> getRebalancesToChannel(ChannelId channelId) {
        return selfPaymentsService.getSelfPaymentsToChannel(channelId).stream()
                .filter(this::memoDoesNotMentionFirstHopChannel)
                .collect(toSet());
    }

    private Set<SelfPayment> getRebalancesFromPeer(Pubkey pubkey) {
        return channelService.getAllChannelsWith(pubkey).parallelStream()
                .map(Channel::getId)
                .map(this::getRebalancesFromChannel)
                .flatMap(Set::stream)
                .collect(toSet());
    }

    private Set<SelfPayment> getRebalancesToPeer(Pubkey pubkey) {
        return channelService.getAllChannelsWith(pubkey).parallelStream()
                .map(Channel::getId)
                .map(this::getRebalancesToChannel)
                .flatMap(Set::stream)
                .collect(toSet());
    }

    private Coins getSumOfAmountPaid(Collection<SelfPayment> selfPayments) {
        return selfPayments.stream()
                .map(SelfPayment::amountPaid)
                .reduce(Coins.NONE, Coins::add);
    }

    private Coins getSumOfFees(Set<SelfPayment> selfPayments) {
        return selfPayments.stream()
                .map(SelfPayment::fees)
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
