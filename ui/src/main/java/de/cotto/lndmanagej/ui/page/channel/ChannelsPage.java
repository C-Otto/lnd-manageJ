package de.cotto.lndmanagej.ui.page.channel;

import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
import de.cotto.lndmanagej.ui.page.general.ThymeleafPage;

import java.util.List;

public class ChannelsPage extends ThymeleafPage {

    public ChannelsPage(List<OpenChannelDto> channels) {
        super();
        add("channels", channels);
    }

    @Override
    public String getView() {
        return "channels";
    }
}
