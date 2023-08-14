package de.cotto.lndmanagej.ui.demo.data;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.ui.WarningService;
import de.cotto.lndmanagej.ui.dto.warning.ChannelWarningDto;
import de.cotto.lndmanagej.ui.dto.warning.DashboardWarningDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.ui.demo.data.DemoDataService.ACINQ;
import static de.cotto.lndmanagej.ui.demo.data.DemoDataService.B_CASH_IS_TRASH;
import static de.cotto.lndmanagej.ui.demo.data.DemoDataService.POCKET;
import static de.cotto.lndmanagej.ui.demo.data.DemoDataService.TRY_BITCOIN;
import static java.util.stream.Collectors.toSet;

@SuppressWarnings("PMD.ExcessiveImports")
@Component
public class DemoWarningService extends WarningService {

    public static final DashboardWarningDto ACINQ_WARNING = new DashboardWarningDto(
            ACINQ.remoteAlias(),
            ACINQ.remotePubkey(),
            Set.of("Node has been online 86% in the past 14 days."),
            Set.of()
    );

    public static final DashboardWarningDto POCKET_WARNING = new DashboardWarningDto(
            POCKET.remoteAlias(),
            POCKET.remotePubkey(),
            Set.of("Rating of 182 is below threshold of 1,000"),
            Set.of(new ChannelWarningDto(POCKET.channelId(), "Channel has accumulated 500,000 updates."))
    );

    public static final DashboardWarningDto BCASH_WARNING = new DashboardWarningDto(
            B_CASH_IS_TRASH.remoteAlias(),
            B_CASH_IS_TRASH.remotePubkey(),
            Set.of("Node has been online 66% in the past 14 days.", "No flow in the past 35 days."),
            Set.of()
    );

    public static final DashboardWarningDto TRY_BTC_WARNING = new DashboardWarningDto(
            TRY_BITCOIN.remoteAlias(),
            TRY_BITCOIN.remotePubkey(),
            Set.of(),
            Set.of(new ChannelWarningDto(
                    TRY_BITCOIN.channelId(), "Channel has accumulated 700,000 updates.")
            )
    );

    public DemoWarningService() {
        super();
    }

    @Override
    public List<DashboardWarningDto> getWarnings() {
        return List.of(BCASH_WARNING, POCKET_WARNING, ACINQ_WARNING, TRY_BTC_WARNING);
    }

    public Set<String> getChannelWarnings(ChannelId channelId) {
        return getWarnings().stream()
                .filter(warning -> warning.channelWarnings().stream().anyMatch(id -> id.channelId().equals(channelId)))
                .flatMap(warning -> warning.channelWarnings().stream().map(ChannelWarningDto::description))
                .collect(toSet());
    }

    public Set<String> getNodeWarnings(Pubkey pubkey) {
        Optional<DashboardWarningDto> first = getWarnings().stream()
                .filter(warning -> warning.pubkey().equals(pubkey))
                .findFirst();
        return first.isPresent() ? first.get().nodeWarnings() : Set.of();
    }
}
