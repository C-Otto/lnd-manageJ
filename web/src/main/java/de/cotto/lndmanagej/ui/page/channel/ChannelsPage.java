package de.cotto.lndmanagej.ui.page.channel;

import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
import de.cotto.lndmanagej.ui.page.general.ThymeleafPage;

import java.util.Comparator;
import java.util.List;

public class ChannelsPage extends ThymeleafPage {

    public ChannelsPage(List<OpenChannelDto> channels) {
        super();
        List<OpenChannelDto> sortedChannels = channels.stream()
                .sorted(Comparator.comparing(OpenChannelDto::getOutboundPercentage))
                .toList();
        add("channels", sortedChannels);
    }

    @Override
    public String getView() {
        return "channels";
    }
}
