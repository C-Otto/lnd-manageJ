package de.cotto.lndmanagej.demo;

import de.cotto.lndmanagej.controller.dto.NodeDetailsDto;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.ui.UiDataService;
import de.cotto.lndmanagej.ui.dto.ChanDetailsDto;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
import de.cotto.lndmanagej.ui.dto.StatusModel;

import java.util.List;

import static de.cotto.lndmanagej.demo.utils.ChannelDetailsUtil.createChannelDetails;
import static de.cotto.lndmanagej.demo.utils.NodeDetailsUtil.createNodeDetails;
import static de.cotto.lndmanagej.demo.utils.NodeWarningsUtil.getStatusModel;
import static de.cotto.lndmanagej.ui.model.OpenChannelDtoFixture.ACINQ;
import static de.cotto.lndmanagej.ui.model.OpenChannelDtoFixture.ACINQ2;
import static de.cotto.lndmanagej.ui.model.OpenChannelDtoFixture.BCASH;
import static de.cotto.lndmanagej.ui.model.OpenChannelDtoFixture.COTTO;
import static de.cotto.lndmanagej.ui.model.OpenChannelDtoFixture.OPEN_CHANNEL_DTO;
import static de.cotto.lndmanagej.ui.model.OpenChannelDtoFixture.WOS;
import static de.cotto.lndmanagej.ui.model.OpenChannelDtoFixture.WOS2;

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
        return List.of(OPEN_CHANNEL_DTO, ACINQ, ACINQ2, WOS, WOS2, BCASH, COTTO);
    }

    @Override
    public ChanDetailsDto getChannelDetails(ChannelId channelId) {
        OpenChannelDto localOpenChannel = getOpenChannels().stream()
                .filter(c -> c.channelId().equals(channelId))
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


