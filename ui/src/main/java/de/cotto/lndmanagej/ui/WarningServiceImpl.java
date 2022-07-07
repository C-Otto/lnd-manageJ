package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.warnings.ChannelWarnings;
import de.cotto.lndmanagej.model.warnings.NodeWarnings;
import de.cotto.lndmanagej.service.ChannelWarningsService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.NodeWarningsService;
import de.cotto.lndmanagej.ui.dto.warning.ChannelWarningDto;
import de.cotto.lndmanagej.ui.dto.warning.DashboardWarningDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByKey;
import static java.util.stream.Collectors.toCollection;

@Component
public class WarningServiceImpl extends WarningService {

    private final NodeService nodeService;
    private final NodeWarningsService nodeWarningsService;
    private final ChannelWarningsService channelWarningsService;

    public WarningServiceImpl(
            NodeService nodeService,
            NodeWarningsService nodeWarningsService,
            ChannelWarningsService channelWarningsService
    ) {
        super();
        this.nodeService = nodeService;
        this.nodeWarningsService = nodeWarningsService;
        this.channelWarningsService = channelWarningsService;
    }

    @Override
    public List<DashboardWarningDto> getWarnings() {
        Map<Node, NodeWarnings> nodeWarnings = nodeWarningsService.getNodeWarnings();
        List<DashboardWarningDto> dashboardWarnings = nodeWarnings.entrySet().stream()
                .sorted(comparingByKey())
                .map(entry -> createDashboardWarning(entry.getKey(), entry.getValue()))
                .collect(toCollection(ArrayList::new));
        Map<LocalOpenChannel, ChannelWarnings> channelWarnings = channelWarningsService.getChannelWarnings();
        channelWarnings.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().getId()))
                .forEach(entry -> {
                    addChannelWarnings(dashboardWarnings, entry.getKey(), entry.getValue());
                });

        return dashboardWarnings;
    }

    private void addChannelWarnings(
            List<DashboardWarningDto> dashboardWarnings,
            LocalOpenChannel channel,
            ChannelWarnings channelWarning) {
        Pubkey pubkey = channel.getRemotePubkey();
        ChannelId channelId = channel.getId();
        Optional<DashboardWarningDto> dashboardWarning = dashboardWarnings.stream()
                .filter(warning -> warning.pubkey().equals(pubkey))
                .findFirst();
        List<ChannelWarningDto> warnings = channelWarning.warnings().stream()
                .map(description -> new ChannelWarningDto(channelId, description.description()))
                .collect(Collectors.toList());
        if (dashboardWarning.isPresent()) {
            dashboardWarning.get().channelWarnings().addAll(warnings);
        } else {
            dashboardWarnings.add(new DashboardWarningDto(
                    nodeService.getAlias(pubkey), pubkey, new ArrayList<>(), warnings));
        }
    }

    private DashboardWarningDto createDashboardWarning(Node node, NodeWarnings warnings) {
        return new DashboardWarningDto(
                node.alias(),
                node.pubkey(),
                new ArrayList<>(warnings.descriptions()),
                new ArrayList<>()
        );
    }
}
