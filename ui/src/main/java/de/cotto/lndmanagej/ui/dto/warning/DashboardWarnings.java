package de.cotto.lndmanagej.ui.dto.warning;

import de.cotto.lndmanagej.model.Pubkey;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DashboardWarnings {
    private final List<DashboardWarningDto> warnings;

    public DashboardWarnings() {
        warnings = new ArrayList<>();
    }

    public List<DashboardWarningDto> getAsList() {
        return warnings;
    }

    public void add(DashboardWarningDto warning) {
        Pubkey pubkey = warning.pubkey();
        DashboardWarningDto existingWarningForPubkey = warnings.stream()
                .filter(w -> pubkey.equals(w.pubkey()))
                .findFirst()
                .orElse(null);

        if (existingWarningForPubkey == null) {
            warnings.add(warning);
        } else {
            warnings.remove(existingWarningForPubkey);
            warnings.add(combine(warning, existingWarningForPubkey));
        }
    }

    private DashboardWarningDto combine(DashboardWarningDto warning1, DashboardWarningDto warning2) {
        Pubkey pubkey = warning1.pubkey();
        Set<String> combinedNodeWarnings = new LinkedHashSet<>(warning2.nodeWarnings());
        combinedNodeWarnings.addAll(warning1.nodeWarnings());
        Set<ChannelWarningDto> combinedChannelWarnings = new LinkedHashSet<>(warning2.channelWarnings());
        combinedChannelWarnings.addAll(warning1.channelWarnings());
        return new DashboardWarningDto(warning2.alias(), pubkey, combinedNodeWarnings, combinedChannelWarnings);
    }
}
