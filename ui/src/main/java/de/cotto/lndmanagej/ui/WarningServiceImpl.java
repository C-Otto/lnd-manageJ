package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.warnings.ChannelWarnings;
import de.cotto.lndmanagej.service.ChannelWarningsService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.NodeWarningsService;
import de.cotto.lndmanagej.ui.dto.warning.ChannelWarningDto;
import de.cotto.lndmanagej.ui.dto.warning.DashboardWarningDto;
import de.cotto.lndmanagej.ui.dto.warning.DashboardWarnings;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        List<DashboardWarningDto> warnings = new ArrayList<>();
        warnings.addAll(getNodeDashboardWarnings());
        warnings.addAll(getChannelDashboardWarnings());

        return merge(warnings);
    }

    private List<DashboardWarningDto> getNodeDashboardWarnings() {
        return nodeWarningsService.getNodeWarnings().entrySet().stream()
                .map(entry -> DashboardWarningDto.forNodeWarnings(entry.getKey(), entry.getValue().descriptions()))
                .toList();
    }

    private List<DashboardWarningDto> getChannelDashboardWarnings() {
        return channelWarningsService.getChannelWarnings().entrySet().stream()
                .map(entry -> createDashboardWarning(entry.getKey(), entry.getValue()))
                .toList();
    }

    private DashboardWarningDto createDashboardWarning(LocalOpenChannel channel, ChannelWarnings channelWarning) {
        Pubkey pubkey = channel.getRemotePubkey();
        ChannelId channelId = channel.getId();
        List<ChannelWarningDto> warnings = channelWarning.warnings().stream()
                .map(warning -> new ChannelWarningDto(channelId, warning.description()))
                .collect(Collectors.toList());
        return DashboardWarningDto.forChannelWarnings(nodeService.getAlias(pubkey), pubkey, warnings);
    }

    private List<DashboardWarningDto> merge(List<DashboardWarningDto> warnings) {
        DashboardWarnings dashboardWarnings = new DashboardWarnings();
        warnings.forEach(dashboardWarnings::add);
        return dashboardWarnings.getAsList();
    }
}
