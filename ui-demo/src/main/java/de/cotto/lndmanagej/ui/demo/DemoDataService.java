package de.cotto.lndmanagej.ui.demo;

import de.cotto.lndmanagej.controller.dto.NodeDetailsDto;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.ui.UiDataService;
import de.cotto.lndmanagej.ui.dto.ChannelDetailsDto;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
import de.cotto.lndmanagej.ui.dto.StatusModel;
import org.springframework.stereotype.Component;

import java.util.List;

import static de.cotto.lndmanagej.controller.dto.OpenChannelDtoFixture.ACINQ;
import static de.cotto.lndmanagej.controller.dto.OpenChannelDtoFixture.ACINQ2;
import static de.cotto.lndmanagej.controller.dto.OpenChannelDtoFixture.BCASH;
import static de.cotto.lndmanagej.controller.dto.OpenChannelDtoFixture.C_OTTO;
import static de.cotto.lndmanagej.controller.dto.OpenChannelDtoFixture.OPEN_CHANNEL_DTO;
import static de.cotto.lndmanagej.controller.dto.OpenChannelDtoFixture.WOS;
import static de.cotto.lndmanagej.controller.dto.OpenChannelDtoFixture.WOS2;
import static de.cotto.lndmanagej.ui.demo.utils.ChannelDetailsUtil.createChannelDetails;
import static de.cotto.lndmanagej.ui.demo.utils.NodeDetailsUtil.createNodeDetails;
import static de.cotto.lndmanagej.ui.demo.utils.NodeWarningsUtil.getStatusModel;

@Component
public class DemoDataService extends UiDataService {

    public DemoDataService() {
        super();
    }

    @Override
    public StatusModel getStatus() {
        return getStatusModel();
    }

    @Override
    public List<OpenChannelDto> getOpenChannels() {
        return List.of(OPEN_CHANNEL_DTO, ACINQ, ACINQ2, WOS, WOS2, BCASH, C_OTTO);
    }

    @Override
    public ChannelDetailsDto getChannelDetails(ChannelId channelId) {
        OpenChannelDto localOpenChannel = getOpenChannels().stream()
                .filter(c -> channelId.equals(c.channelId()))
                .findFirst()
                .orElseThrow();
        return createChannelDetails(localOpenChannel);
    }

    @Override
    public NodeDto getNode(Pubkey pubkey) {
        return getOpenChannels().stream()
                .filter(channel -> channel.remotePubkey().equals(pubkey))
                .map(channel -> new NodeDto(pubkey.toString(), channel.remoteAlias(), isOnline(channel)))
                .findFirst().orElseThrow();
    }

    @Override
    public NodeDetailsDto getNodeDetails(Pubkey pubkey) {
        return createNodeDetails(getNode(pubkey));
    }

    private static boolean isOnline(OpenChannelDto channel) {
        return channel.channelId().getShortChannelId() % 2 != 0;
    }

}


