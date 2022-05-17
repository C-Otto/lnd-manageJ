package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.GraphService;
import de.cotto.lndmanagej.service.OwnNodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_TO_NODE_3;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = StatusController.class)
class StatusControllerIT {
    private static final String PREFIX = "/api/status/";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @SuppressWarnings("unused")
    private ChannelIdResolver channelIdResolver;

    @MockBean
    private ChannelService channelService;

    @MockBean
    private OwnNodeService ownNodeService;

    @MockBean
    private GraphService graphService;

    @Test
    void isSyncedToChain() throws Exception {
        when(ownNodeService.isSyncedToChain()).thenReturn(true);
        mockMvc.perform(get(PREFIX + "/synced-to-chain"))
                .andExpect(content().string("true"));
    }

    @Test
    void getBlockHeight() throws Exception {
        when(ownNodeService.getBlockHeight()).thenReturn(123_456);
        mockMvc.perform(get(PREFIX + "/block-height"))
                .andExpect(content().string("123456"));
    }

    @Test
    void getOpenChannels() throws Exception {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL_TO_NODE_3, LOCAL_OPEN_CHANNEL));
        List<String> sortedChannelIds = List.of(
                LOCAL_OPEN_CHANNEL.getId().toString(),
                LOCAL_OPEN_CHANNEL_TO_NODE_3.getId().toString()
        );
        mockMvc.perform(get(PREFIX + "/open-channels/"))
                .andExpect(jsonPath("$.channels", is(sortedChannelIds)));
    }

    @Test
    void getPubkeysForOpenChannels() throws Exception {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL_TO_NODE_3, LOCAL_OPEN_CHANNEL));
        List<String> sortedPubkeys = List.of(
                LOCAL_OPEN_CHANNEL.getRemotePubkey().toString(),
                LOCAL_OPEN_CHANNEL_TO_NODE_3.getRemotePubkey().toString()
        );
        mockMvc.perform(get(PREFIX + "/open-channels/pubkeys"))
                .andExpect(jsonPath("$.pubkeys", is(sortedPubkeys)));
    }

    @Test
    void getAllChannels() throws Exception {
        when(channelService.getAllLocalChannels()).thenReturn(Stream.of(LOCAL_OPEN_CHANNEL_TO_NODE_3, CLOSED_CHANNEL));
        List<String> sortedChannelIds = List.of(
                CLOSED_CHANNEL.getId().toString(),
                LOCAL_OPEN_CHANNEL_TO_NODE_3.getId().toString()
        );
        mockMvc.perform(get(PREFIX + "/all-channels/"))
                .andExpect(jsonPath("$.channels", is(sortedChannelIds)));
    }

    @Test
    void getPubkeysForAllChannels() throws Exception {
        when(channelService.getAllLocalChannels()).thenReturn(Stream.of(LOCAL_OPEN_CHANNEL_TO_NODE_3, CLOSED_CHANNEL));
        List<String> sortedPubkeys = List.of(
                CLOSED_CHANNEL.getRemotePubkey().toString(),
                LOCAL_OPEN_CHANNEL_TO_NODE_3.getRemotePubkey().toString()
        );
        mockMvc.perform(get(PREFIX + "/all-channels/pubkeys"))
                .andExpect(jsonPath("$.pubkeys", is(sortedPubkeys)));
    }

    @Test
    void getKnownChannels() throws Exception {
        when(graphService.getNumberOfChannels()).thenReturn(123);
        mockMvc.perform(get(PREFIX + "/known-channels"))
                .andExpect(jsonPath("$", is(123)));
    }
}
