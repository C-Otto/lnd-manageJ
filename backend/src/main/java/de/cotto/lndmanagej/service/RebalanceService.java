package de.cotto.lndmanagej.service;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.RebalanceReport;
import de.cotto.lndmanagej.model.SelfPayment;
import de.cotto.lndmanagej.model.SelfPaymentRoute;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Component
public class RebalanceService {
    private static final Duration DEFAULT_MAX_AGE = Duration.ofDays(365 * 1_000);
    private final SelfPaymentsService selfPaymentsService;
    private final ChannelService channelService;

    public RebalanceService(SelfPaymentsService selfPaymentsService, ChannelService channelService) {
        this.selfPaymentsService = selfPaymentsService;
        this.channelService = channelService;
    }

    public RebalanceReport getReportForChannel(ChannelId channelId) {
        return getReportForChannel(channelId, DEFAULT_MAX_AGE);
    }

    @Timed
    public RebalanceReport getReportForChannel(ChannelId channelId, Duration maxAge) {
        return new RebalanceReport(
                getSourceCostsForChannel(channelId, maxAge),
                getAmountFromChannel(channelId, maxAge),
                getTargetCostsForChannel(channelId, maxAge),
                getAmountToChannel(channelId, maxAge),
                getSupportAsSourceAmountFromChannel(channelId, maxAge),
                getSupportAsTargetAmountToChannel(channelId, maxAge)
        );
    }

    public RebalanceReport getReportForPeer(Pubkey pubkey) {
        return getReportForPeer(pubkey, DEFAULT_MAX_AGE);
    }

    @Timed
    public RebalanceReport getReportForPeer(Pubkey pubkey, Duration maxAge) {
        return new RebalanceReport(
                getSourceCostsForPeer(pubkey, maxAge),
                getAmountFromPeer(pubkey, maxAge),
                getTargetCostsForPeer(pubkey, maxAge),
                getAmountToPeer(pubkey, maxAge),
                getSupportAsSourceAmountFromPeer(pubkey, maxAge),
                getSupportAsTargetAmountToPeer(pubkey, maxAge)
        );
    }

    public Coins getSourceCostsForChannel(ChannelId channelId) {
        return getSourceCostsForChannel(channelId, DEFAULT_MAX_AGE);
    }

    @Timed
    public Coins getSourceCostsForChannel(ChannelId channelId, Duration maxAge) {
        return getSumOfFees(getRebalancesFromChannel(channelId, maxAge));
    }

    public Coins getSourceCostsForPeer(Pubkey pubkey) {
        return getSourceCostsForPeer(pubkey, DEFAULT_MAX_AGE);
    }

    @Timed
    public Coins getSourceCostsForPeer(Pubkey pubkey, Duration maxAge) {
        return getSumOfFees(getRebalancesFromPeer(pubkey, maxAge));
    }

    public Coins getTargetCostsForChannel(ChannelId channelId) {
        return getTargetCostsForChannel(channelId, DEFAULT_MAX_AGE);
    }

    @Timed
    public Coins getTargetCostsForChannel(ChannelId channelId, Duration maxAge) {
        return getSumOfFees(getRebalancesToChannel(channelId, maxAge));
    }

    public Coins getTargetCostsForPeer(Pubkey pubkey) {
        return getTargetCostsForPeer(pubkey, DEFAULT_MAX_AGE);
    }

    @Timed
    public Coins getTargetCostsForPeer(Pubkey pubkey, Duration maxAge) {
        return getSumOfFees(getRebalancesToPeer(pubkey, maxAge));
    }

    public Coins getAmountFromChannel(ChannelId channelId) {
        return getAmountFromChannel(channelId, DEFAULT_MAX_AGE);
    }

    @Timed
    public Coins getAmountFromChannel(ChannelId channelId, Duration maxAge) {
        return getSumOfAmountPaid(getRebalancesFromChannel(channelId, maxAge));
    }

    public Coins getAmountFromPeer(Pubkey pubkey) {
        return getAmountFromPeer(pubkey, DEFAULT_MAX_AGE);
    }

    @Timed
    public Coins getAmountFromPeer(Pubkey pubkey, Duration maxAge) {
        return getSumOfAmountPaid(getRebalancesFromPeer(pubkey, maxAge));
    }

    public Coins getAmountToChannel(ChannelId channelId) {
        return getAmountToChannel(channelId, DEFAULT_MAX_AGE);
    }

    @Timed
    public Coins getAmountToChannel(ChannelId channelId, Duration maxAge) {
        return getSumOfAmountPaid(getRebalancesToChannel(channelId, maxAge));
    }

    public Coins getAmountToPeer(Pubkey pubkey) {
        return getAmountToPeer(pubkey, DEFAULT_MAX_AGE);
    }

    @Timed
    public Coins getAmountToPeer(Pubkey pubkey, Duration maxAge) {
        return getSumOfAmountPaid(getRebalancesToPeer(pubkey, maxAge));
    }

    public Coins getSupportAsSourceAmountFromChannel(ChannelId channelId) {
        return getSupportAsSourceAmountFromChannel(channelId, DEFAULT_MAX_AGE);
    }

    @Timed
    public Coins getSupportAsSourceAmountFromChannel(ChannelId channelId, Duration maxAge) {
        Coins amountSourceTotal = getSumOfAmountPaid(selfPaymentsService.getSelfPaymentsFromChannel(channelId, maxAge));
        Coins amountRebalanceSource = getAmountFromChannel(channelId, maxAge);
        return amountSourceTotal.subtract(amountRebalanceSource);
    }

    @Timed
    public Coins getSupportAsSourceCostsFromChannel(ChannelId channelId, Duration maxAge) {
        Coins costsSourceTotal = getSumOfFees(selfPaymentsService.getSelfPaymentsFromChannel(channelId, maxAge));
        Coins costsRebalanceSource = getSourceCostsForChannel(channelId, maxAge);
        return costsSourceTotal.subtract(costsRebalanceSource);
    }

    public Coins getSupportAsTargetAmountToChannel(ChannelId channelId) {
        return getSupportAsTargetAmountToChannel(channelId, DEFAULT_MAX_AGE);
    }

    @Timed
    public Coins getSupportAsTargetAmountToChannel(ChannelId channelId, Duration maxAge) {
        Coins amountTargetTotal = getSumOfAmountPaid(selfPaymentsService.getSelfPaymentsToChannel(channelId, maxAge));
        Coins amountRebalanceTarget = getAmountToChannel(channelId, maxAge);
        return amountTargetTotal.subtract(amountRebalanceTarget);
    }

    public Coins getSupportAsSourceAmountFromPeer(Pubkey pubkey) {
        return getSupportAsSourceAmountFromPeer(pubkey, DEFAULT_MAX_AGE);
    }

    @Timed
    public Coins getSupportAsSourceAmountFromPeer(Pubkey pubkey, Duration maxAge) {
        return channelService.getAllChannelsWith(pubkey).parallelStream()
                .map(Channel::getId)
                .map(channelId -> getSupportAsSourceAmountFromChannel(channelId, maxAge))
                .reduce(Coins.NONE, Coins::add);
    }

    public Coins getSupportAsTargetAmountToPeer(Pubkey pubkey) {
        return getSupportAsTargetAmountToPeer(pubkey, DEFAULT_MAX_AGE);
    }

    @Timed
    public Coins getSupportAsTargetAmountToPeer(Pubkey pubkey, Duration maxAge) {
        return channelService.getAllChannelsWith(pubkey).parallelStream()
                .map(Channel::getId)
                .map(channelId -> getSupportAsTargetAmountToChannel(channelId, maxAge))
                .reduce(Coins.NONE, Coins::add);
    }

    private Set<SelfPayment> getRebalancesFromChannel(ChannelId channelId, Duration maxAge) {
        return selfPaymentsService.getSelfPaymentsFromChannel(channelId, maxAge).stream()
                .filter(selfPayment -> memoMentionsChannel(selfPayment, channelId))
                .collect(toSet());
    }

    private Set<SelfPayment> getRebalancesToChannel(ChannelId channelId, Duration maxAge) {
        return selfPaymentsService.getSelfPaymentsToChannel(channelId, maxAge).stream()
                .filter(this::memoDoesNotMentionFirstHopChannel)
                .collect(toSet());
    }

    private Set<SelfPayment> getRebalancesFromPeer(Pubkey pubkey, Duration maxAge) {
        return channelService.getAllChannelsWith(pubkey).parallelStream()
                .map(Channel::getId)
                .map(channelId -> getRebalancesFromChannel(channelId, maxAge))
                .flatMap(Set::stream)
                .collect(toSet());
    }

    private Set<SelfPayment> getRebalancesToPeer(Pubkey pubkey, Duration maxAge) {
        return channelService.getAllChannelsWith(pubkey).parallelStream()
                .map(Channel::getId)
                .map(channelId -> getRebalancesToChannel(channelId, maxAge))
                .flatMap(Set::stream)
                .collect(toSet());
    }

    private Coins getSumOfAmountPaid(Collection<SelfPayment> selfPayments) {
        return selfPayments.stream()
                .map(SelfPayment::amountPaid)
                .reduce(Coins.NONE, Coins::add);
    }

    private Coins getSumOfFees(Collection<SelfPayment> selfPayments) {
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
        return selfPayment.routes().stream()
                .map(SelfPaymentRoute::channelIdOut)
                .noneMatch(firstChannel -> memoMentionsChannel(selfPayment, firstChannel));
    }
}
