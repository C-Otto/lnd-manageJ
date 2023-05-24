package de.cotto.lndmanagej.ui.page.channel;

import de.cotto.lndmanagej.ui.dto.PendingOpenChannelDto;
import de.cotto.lndmanagej.ui.page.general.ThymeleafPage;

import java.util.List;

public class PendingChannelsPage extends ThymeleafPage {

    public PendingChannelsPage(List<PendingOpenChannelDto> pendingOpenChannels) {
        super();
        add("pendingOpenChannels", pendingOpenChannels);
    }

    @Override
    public String getView() {
        return "pending-channels-page";
    }
}
