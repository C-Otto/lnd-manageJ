package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.controller.dto.NodesAndChannelsWithWarningsDto;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.ui.dto.ChannelDetailsDto;
import de.cotto.lndmanagej.ui.dto.NodeDetailsDto;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static de.cotto.lndmanagej.controller.dto.NodesAndChannelsWithWarningsDto.NONE;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.ui.dto.ChannelDetailsDtoFixture.CHANNEL_DETAILS_DTO;
import static de.cotto.lndmanagej.ui.dto.NodeDetailsDtoFixture.NODE_DETAILS_MODEL;
import static de.cotto.lndmanagej.ui.dto.OpenChannelDtoFixture.OPEN_CHANNEL_DTO;
import static org.assertj.core.api.Assertions.assertThat;

class UiDataServiceTest {
    private static final NodeDto NODE_DTO = new NodeDto(PUBKEY.toString(), NODE.alias(), true);
    private final UiDataService uiDataService = new TestableUiDataService();

    @Test
    void createNodeList() {
        assertThat(uiDataService.createNodeList()).containsExactly(NODE_DTO);
    }

    @Test
    void calculateDaysOfBlocks_oneHour() {
        assertThat(uiDataService.calculateDaysOfBlocks(123_456, 123_450)).isEqualTo(1);
    }

    @Test
    void calculateDaysOfBlocks_oneDay() {
        assertThat(uiDataService.calculateDaysOfBlocks(123_456, 123_312)).isEqualTo(1);
    }

    @Test
    void calculateDaysOfBlocks_twoDays() {
        assertThat(uiDataService.calculateDaysOfBlocks(123_456, 123_311)).isEqualTo(2);
    }

    private static class TestableUiDataService extends UiDataService {
        @Override
        public NodesAndChannelsWithWarningsDto getWarnings() {
            return NONE;
        }

        @Override
        public Set<Pubkey> getPubkeys() {
            return Set.of(PUBKEY);
        }

        @Override
        public List<OpenChannelDto> getOpenChannels(@Nullable String sort) {
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
            return NODE_DETAILS_MODEL;
        }
    }
}
