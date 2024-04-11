package de.cotto.lndmanagej.ui.page.channel;

import de.cotto.lndmanagej.ui.dto.ChannelDetailsDto;
import de.cotto.lndmanagej.ui.page.general.ThymeleafPage;

public final class ChannelDetailsPage extends ThymeleafPage {

    public ChannelDetailsPage(ChannelDetailsDto channel) {
        super();
        add("id", channel.channelId());
        add("channel", channel);
    }

    @Override
    public String getView() {
        return "channel-details-page";
    }
}
