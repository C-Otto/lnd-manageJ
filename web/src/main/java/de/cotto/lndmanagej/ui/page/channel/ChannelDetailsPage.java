package de.cotto.lndmanagej.ui.page.channel;

import de.cotto.lndmanagej.ui.dto.ChanDetailsDto;
import de.cotto.lndmanagej.ui.page.general.ThymeleafPage;

public class ChannelDetailsPage extends ThymeleafPage {

    public ChannelDetailsPage(ChanDetailsDto channel) {
        super();
        add("id", channel.channelId());
        add("channel", channel);
    }

    @Override
    public String getView() {
        return "channel-details";
    }
}
