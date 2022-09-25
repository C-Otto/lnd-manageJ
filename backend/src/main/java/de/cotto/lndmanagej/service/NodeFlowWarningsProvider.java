package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FlowReport;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.warnings.NodeNoFlowWarning;
import de.cotto.lndmanagej.model.warnings.NodeWarning;
import de.cotto.lndmanagej.service.warnings.NodeWarningsProvider;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

import static de.cotto.lndmanagej.configuration.WarningsConfigurationSettings.NODE_FLOW_MAXIMUM_DAYS_TO_CONSIDER;
import static de.cotto.lndmanagej.configuration.WarningsConfigurationSettings.NODE_FLOW_MINIMUM_DAYS_FOR_WARNING;
import static de.cotto.lndmanagej.configuration.WarningsConfigurationSettings.NODE_FLOW_WARNING_IGNORE_NODE;

@Component
public class NodeFlowWarningsProvider implements NodeWarningsProvider {
    private static final int EXPECTED_MINUTES_PER_BLOCK = 10;
    private static final int MINUTES_PER_DAY = 1_440;
    private static final int DEFAULT_MINIMUM_DAYS_FOR_WARNING = 30;
    private static final int DEFAULT_MAXIMUM_DAYS_TO_CONSIDER = 90;
    private final FlowService flowService;
    private final ChannelService channelService;
    private final OwnNodeService ownNodeService;
    private final ConfigurationService configurationService;

    public NodeFlowWarningsProvider(
            FlowService flowService,
            ChannelService channelService,
            OwnNodeService ownNodeService,
            ConfigurationService configurationService
    ) {
        this.flowService = flowService;
        this.channelService = channelService;
        this.ownNodeService = ownNodeService;
        this.configurationService = configurationService;
    }

    @Override
    public Stream<NodeWarning> getNodeWarnings(Pubkey pubkey) {
        if (ignoreWarnings(pubkey)) {
            return Stream.empty();
        }
        return Stream.of(getNoFlowWarning(pubkey)).flatMap(Optional::stream);
    }

    private boolean ignoreWarnings(Pubkey pubkey) {
        return configurationService.getPubkeys(NODE_FLOW_WARNING_IGNORE_NODE).contains(pubkey);
    }

    private Optional<NodeWarning> getNoFlowWarning(Pubkey pubkey) {
        int daysWithoutFlow = getDaysWithoutFlow(pubkey);
        if (daysWithoutFlow < getMinimumDaysForWarning()) {
            return Optional.empty();
        }
        return Optional.of(new NodeNoFlowWarning(daysWithoutFlow));
    }

    private int getDaysWithoutFlow(Pubkey pubkey) {
        int daysToConsider = getDaysToConsider(pubkey);
        int daysToCheck = getMinimumDaysForWarning();
        while (noFlow(pubkey, daysToCheck) && daysToCheck <= daysToConsider) {
            daysToCheck++;
        }
        return daysToCheck - 1;
    }

    private int getDaysToConsider(Pubkey pubkey) {
        OptionalInt openHeightOldestOpenChannel = channelService.getOpenChannelsWith(pubkey).stream()
                .map(channelService::getOpenHeight)
                .mapToInt(h -> h)
                .max();
        if (openHeightOldestOpenChannel.isEmpty()) {
            return 0;
        }
        int channelAgeInBlocks = ownNodeService.getBlockHeight() - openHeightOldestOpenChannel.getAsInt();
        int channelAgeInDays = (int) Math.ceil(channelAgeInBlocks * 1.0 * EXPECTED_MINUTES_PER_BLOCK / MINUTES_PER_DAY);
        return Math.min(getMaxDaysToConsider(), channelAgeInDays);
    }

    private boolean noFlow(Pubkey pubkey, int days) {
        Duration maxAge = Duration.ofDays(days);
        FlowReport flowReport = flowService.getFlowReportForPeer(pubkey, maxAge);
        Coins absoluteFlow = flowReport.totalSent().add(flowReport.totalReceived());
        return absoluteFlow.isNonPositive();
    }

    private int getMinimumDaysForWarning() {
        return configurationService.getIntegerValue(NODE_FLOW_MINIMUM_DAYS_FOR_WARNING)
                .orElse(DEFAULT_MINIMUM_DAYS_FOR_WARNING);
    }

    private int getMaxDaysToConsider() {
        return configurationService.getIntegerValue(NODE_FLOW_MAXIMUM_DAYS_TO_CONSIDER)
                .orElse(DEFAULT_MAXIMUM_DAYS_TO_CONSIDER);
    }
}
