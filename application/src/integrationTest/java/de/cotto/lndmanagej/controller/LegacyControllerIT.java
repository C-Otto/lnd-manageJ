package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.FeeService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.OwnNodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_COMPACT;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_COMPACT_3;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL_3;
import static de.cotto.lndmanagej.model.ForceClosingChannelFixtures.FORCE_CLOSING_CHANNEL;
import static de.cotto.lndmanagej.model.ForceClosingChannelFixtures.FORCE_CLOSING_CHANNEL_3;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_3;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_TO_NODE_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LegacyController.class)
class LegacyControllerIT {
    private static final String PUBKEY_BASE = "/legacy/node/" + PUBKEY;
    private static final String CHANNEL_BASE = "/legacy/channel/" + CHANNEL_ID;
    private static final long FEE_RATE = 123L;
    private static final Coins BASE_FEE = Coins.ofMilliSatoshis(10L);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @SuppressWarnings("unused")
    private NodeService nodeService;

    @MockBean
    private ChannelService channelService;

    @MockBean
    private OwnNodeService ownNodeService;

    @MockBean
    private FeeService feeService;

    @MockBean
    private BalanceService balanceService;

    @MockBean
    @SuppressWarnings("unused")
    private Metrics metrics;

    @Test
    void getOpenChannelIds_for_peer() throws Exception {
        when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_3));
        mockMvc.perform(get(PUBKEY_BASE + "/open-channels"))
                .andExpect(content().string(CHANNEL_ID + "\n" + CHANNEL_ID_3));
    }

    @Test
    void getAllChannelIds_for_peer() throws Exception {
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, CLOSED_CHANNEL_3));
        mockMvc.perform(get(PUBKEY_BASE + "/all-channels"))
                .andExpect(content().string(CHANNEL_ID + "\n" + CHANNEL_ID_3));
    }

    @Test
    void getOpenChannelIds() throws Exception {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_3));
        mockMvc.perform(get("/legacy/open-channels"))
                .andExpect(content().string(CHANNEL_ID + "\n" + CHANNEL_ID_3));
    }

    @Test
    void getOpenChannelIdsCompact() throws Exception {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_3));
        mockMvc.perform(get("/legacy/open-channels/compact"))
                .andExpect(content().string(CHANNEL_ID_COMPACT + "\n" + CHANNEL_ID_COMPACT_3));
    }

    @Test
    void getOpenChannelIdsPretty() throws Exception {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_3));
        mockMvc.perform(get("/legacy/open-channels/pretty"))
                .andExpect(status().isOk());
    }

    @Test
    void getClosedChannelIds() throws Exception {
        when(channelService.getClosedChannels()).thenReturn(Set.of(CLOSED_CHANNEL, CLOSED_CHANNEL_3));
        mockMvc.perform(get("/legacy/closed-channels"))
                .andExpect(content().string(CHANNEL_ID + "\n" + CHANNEL_ID_3));
    }

    @Test
    void getForceClosingChannels() throws Exception {
        when(channelService.getForceClosingChannels())
                .thenReturn(Set.of(FORCE_CLOSING_CHANNEL, FORCE_CLOSING_CHANNEL_3));
        mockMvc.perform(get("/legacy/force-closing-channels"))
                .andExpect(content().string(CHANNEL_ID + "\n" + CHANNEL_ID_3));
    }

    @Test
    void isSyncedToChain_true() throws Exception {
        when(ownNodeService.isSyncedToChain()).thenReturn(true);
        mockMvc.perform(get("/legacy/synced-to-chain")).andExpect(content().string("true"));
    }

    @Test
    void isSyncedToChain_false() throws Exception {
        when(ownNodeService.isSyncedToChain()).thenReturn(false);
        mockMvc.perform(get("/legacy/synced-to-chain")).andExpect(content().string("false"));
    }

    @Test
    void getPeerPubkeys() throws Exception {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_TO_NODE_3));
        mockMvc.perform(get("/legacy/peer-pubkeys"))
                .andExpect(content().string(PUBKEY_2 + "\n" + PUBKEY_3));
    }

    @Test
    void getIncomingFeeRate() throws Exception {
        when(feeService.getIncomingFeeRate(CHANNEL_ID)).thenReturn(FEE_RATE);
        mockMvc.perform(get(CHANNEL_BASE + "/incoming-fee-rate"))
                .andExpect(content().string(Long.toString(FEE_RATE)));
    }

    @Test
    void getOutgoingFeeRate() throws Exception {
        when(feeService.getOutgoingFeeRate(CHANNEL_ID)).thenReturn(FEE_RATE);
        mockMvc.perform(get(CHANNEL_BASE + "/outgoing-fee-rate"))
                .andExpect(content().string(Long.toString(FEE_RATE)));
    }

    @Test
    void getIncomingBaseFee() throws Exception {
        when(feeService.getIncomingBaseFee(CHANNEL_ID)).thenReturn(BASE_FEE);
        mockMvc.perform(get(CHANNEL_BASE + "/incoming-base-fee"))
                .andExpect(content().string(String.valueOf(BASE_FEE.milliSatoshis())));
    }

    @Test
    void getOutgoingBaseFee() throws Exception {
        when(feeService.getOutgoingBaseFee(CHANNEL_ID)).thenReturn(BASE_FEE);
        mockMvc.perform(get(CHANNEL_BASE + "/outgoing-base-fee"))
                .andExpect(content().string(String.valueOf(BASE_FEE.milliSatoshis())));
    }

    @Test
    void getAvailableLocalBalance_channel() throws Exception {
        Coins availableBalance = Coins.ofSatoshis(999);
        when(balanceService.getAvailableLocalBalance(CHANNEL_ID)).thenReturn(availableBalance);
        mockMvc.perform(get(CHANNEL_BASE + "/available-local-balance"))
                .andExpect(content().string(String.valueOf(availableBalance.satoshis())));
    }

    @Test
    void getAvailableRemoteBalance_channel() throws Exception {
        Coins availableBalance = Coins.ofSatoshis(999);
        when(balanceService.getAvailableRemoteBalance(CHANNEL_ID)).thenReturn(availableBalance);
        mockMvc.perform(get(CHANNEL_BASE + "/available-remote-balance"))
                .andExpect(content().string(String.valueOf(availableBalance.satoshis())));
    }

    @Test
    void getAvailableLocalBalance_peer() throws Exception {
        Coins availableBalance = Coins.ofSatoshis(999);
        when(balanceService.getAvailableLocalBalance(PUBKEY)).thenReturn(availableBalance);
        mockMvc.perform(get(PUBKEY_BASE + "/available-local-balance"))
                .andExpect(content().string(String.valueOf(availableBalance.satoshis())));
    }

    @Test
    void getAvailableRemoteBalance_peer() throws Exception {
        Coins availableBalance = Coins.ofSatoshis(999);
        when(balanceService.getAvailableRemoteBalance(PUBKEY)).thenReturn(availableBalance);
        mockMvc.perform(get(PUBKEY_BASE + "/available-remote-balance"))
                .andExpect(content().string(String.valueOf(availableBalance.satoshis())));
    }
}