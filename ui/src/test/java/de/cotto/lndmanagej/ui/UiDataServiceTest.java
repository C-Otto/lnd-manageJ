package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.controller.dto.NodeDetailsDto;
import de.cotto.lndmanagej.controller.dto.NodesAndChannelsWithWarningsDto;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.ui.dto.ChannelDetailsDto;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.cotto.lndmanagej.controller.dto.ChannelDetailsDtoFixture.CHANNEL_DETAILS_DTO;
import static de.cotto.lndmanagej.controller.dto.NodeDetailsDtoFixture.NODE_DETAILS_DTO;
import static de.cotto.lndmanagej.controller.dto.NodesAndChannelsWithWarningsDto.NONE;
import static de.cotto.lndmanagej.controller.dto.OpenChannelDtoFixture.OPEN_CHANNEL_DTO;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;

class UiDataServiceTest {
    private static final NodeDto NODE_DTO = new NodeDto(PUBKEY.toString(), NODE.alias(), true);
    private final UiDataService uiDataService = new TestableUiDataService();

    @Test
    void createNodeList() {
        assertThat(uiDataService.createNodeList()).containsExactly(NODE_DTO);
    }

    private static class TestableUiDataService extends UiDataService {
        @Override
        public NodesAndChannelsWithWarningsDto getWarnings() {
            return NONE;
        }

        @Override
        public List<OpenChannelDto> getOpenChannels() {
            return List.of(OPEN_CHANNEL_DTO);
        }

        @Override
        public ChannelDetailsDto getChannelDetails(ChannelId channelId) {
            return CHANNEL_DETAILS_DTO;
        }

        @Override
        public NodeDto getNode(Pubkey pubkey) {
            return NODE_DTO;
        }

        @Override
        public NodeDetailsDto getNodeDetails(Pubkey pubkey) {
            return NODE_DETAILS_DTO;
        }
    }
}
