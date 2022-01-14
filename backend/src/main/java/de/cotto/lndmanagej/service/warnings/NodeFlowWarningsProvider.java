package de.cotto.lndmanagej.service.warnings;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FlowReport;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.warnings.NodeNoFlowWarning;
import de.cotto.lndmanagej.model.warnings.NodeWarning;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.FlowService;
import de.cotto.lndmanagej.service.NodeWarningsProvider;
import de.cotto.lndmanagej.service.OwnNodeService;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

@Component
public class NodeFlowWarningsProvider implements NodeWarningsProvider {
    private static final int EXPECTED_MINUTES_PER_BLOCK = 10;
    private static final int MINUTES_PER_DAY = 1_440;
    private static final int MINIMUM_DAYS_FOR_WARNING = 14;
    private static final int MAX_DAYS_TO_CONSIDER = 60;
    private final FlowService flowService;
    private final ChannelService channelService;
    private final OwnNodeService ownNodeService;

    public NodeFlowWarningsProvider(
            FlowService flowService,
            ChannelService channelService,
            OwnNodeService ownNodeService
    ) {
        this.flowService = flowService;
        this.channelService = channelService;
        this.ownNodeService = ownNodeService;
    }

    @Override
    public Stream<NodeWarning> getNodeWarnings(Pubkey pubkey) {
        return Stream.of(getNoFlowWarning(pubkey)).flatMap(Optional::stream);
    }

    private Optional<NodeWarning> getNoFlowWarning(Pubkey pubkey) {
        int daysWithoutFlow = getDaysWithoutFlow(pubkey);
        if (daysWithoutFlow < MINIMUM_DAYS_FOR_WARNING) {
            return Optional.empty();
        }
        return Optional.of(new NodeNoFlowWarning(daysWithoutFlow));
    }

    private int getDaysWithoutFlow(Pubkey pubkey) {
        int daysToConsider = getDaysToConsider(pubkey);
        int daysToCheck = MINIMUM_DAYS_FOR_WARNING;
        while (noFlow(pubkey, daysToCheck) && daysToCheck <= daysToConsider) {
            daysToCheck++;
        }
        return daysToCheck - 1;
    }

    private int getDaysToConsider(Pubkey pubkey) {
        OptionalInt openHeightOldestOpenChannel = channelService.getOpenChannelsWith(pubkey).stream()
                .map(channelService::getOpenHeight)
                .flatMap(Optional::stream)
                .mapToInt(h -> h)
                .max();
        if (openHeightOldestOpenChannel.isEmpty()) {
            return 0;
        }
        int channelAgeInBlocks = ownNodeService.getBlockHeight() - openHeightOldestOpenChannel.getAsInt();
        int channelAgeInDays = (int) Math.ceil(channelAgeInBlocks * 1.0 * EXPECTED_MINUTES_PER_BLOCK / MINUTES_PER_DAY);
        return Math.min(MAX_DAYS_TO_CONSIDER, channelAgeInDays);
    }

    private boolean noFlow(Pubkey pubkey, int days) {
        Duration maxAge = Duration.ofDays(days);
        FlowReport flowReport = flowService.getFlowReportForPeer(pubkey, maxAge);
        Coins absoluteFlow = flowReport.totalSent().add(flowReport.totalReceived());
        return absoluteFlow.isNonPositive();
    }

}
