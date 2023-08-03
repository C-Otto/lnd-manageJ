package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelIdParser;
import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.PubkeyAndFeeRate;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.GraphService;
import de.cotto.lndmanagej.service.OwnNodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_TO_NODE_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@WebFluxTest(StatusController.class)
@Import(ChannelIdParser.class)
class StatusControllerIT {
    private static final String PREFIX = "/api/status/";

    @Autowired
    private WebTestClient webTestClient;

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
    void isSyncedToChain() {
        when(ownNodeService.isSyncedToChain()).thenReturn(true);
        webTestClient.get().uri(PREFIX + "/synced-to-chain").exchange()
                .expectBody(String.class).isEqualTo("true");
    }

    @Test
    void getBlockHeight() {
        when(ownNodeService.getBlockHeight()).thenReturn(123_456);
        webTestClient.get().uri(PREFIX + "/block-height").exchange()
                .expectBody(String.class).isEqualTo("123456");
    }

    @Test
    void getOpenChannels() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL_TO_NODE_3, LOCAL_OPEN_CHANNEL));
        List<String> sortedChannelIds = List.of(
                LOCAL_OPEN_CHANNEL.getId().toString(),
                LOCAL_OPEN_CHANNEL_TO_NODE_3.getId().toString()
        );
        webTestClient.get().uri(PREFIX + "/open-channels").exchange().expectBody()
                .jsonPath("$.channels").value(is(sortedChannelIds));
    }

    @Test
    void getPubkeysForOpenChannels() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL_TO_NODE_3, LOCAL_OPEN_CHANNEL));
        List<String> sortedPubkeys = List.of(
                LOCAL_OPEN_CHANNEL.getRemotePubkey().toString(),
                LOCAL_OPEN_CHANNEL_TO_NODE_3.getRemotePubkey().toString()
        );
        webTestClient.get().uri(PREFIX + "/open-channels/pubkeys").exchange().expectBody()
                .jsonPath("$.pubkeys").value(is(sortedPubkeys));
    }

    @Test
    void getAllChannels() {
        when(channelService.getAllLocalChannels()).thenReturn(Stream.of(LOCAL_OPEN_CHANNEL_TO_NODE_3, CLOSED_CHANNEL));
        List<String> sortedChannelIds = List.of(
                CLOSED_CHANNEL.getId().toString(),
                LOCAL_OPEN_CHANNEL_TO_NODE_3.getId().toString()
        );
        webTestClient.get().uri(PREFIX + "/all-channels").exchange().expectBody()
                .jsonPath("$.channels").value(is(sortedChannelIds));
    }

    @Test
    void getPubkeysForAllChannels() {
        when(channelService.getAllLocalChannels()).thenReturn(Stream.of(LOCAL_OPEN_CHANNEL_TO_NODE_3, CLOSED_CHANNEL));
        List<String> sortedPubkeys = List.of(
                CLOSED_CHANNEL.getRemotePubkey().toString(),
                LOCAL_OPEN_CHANNEL_TO_NODE_3.getRemotePubkey().toString()
        );
        webTestClient.get().uri(PREFIX + "/all-channels/pubkeys").exchange().expectBody()
                .jsonPath("$.pubkeys").value(is(sortedPubkeys));
    }

    @Test
    void getKnownChannels() {
        when(graphService.getNumberOfChannels()).thenReturn(123);
        webTestClient.get().uri(PREFIX + "/known-channels").exchange().expectBody()
                .jsonPath("$").value(is(123));
    }

    @Test
    void getNodesWithHighIncomingFeeRate() {
        when(graphService.getNodesWithHighFeeRate()).thenReturn(List.of(
                new PubkeyAndFeeRate(PUBKEY, 123),
                new PubkeyAndFeeRate(PUBKEY_2, 456)
        ));
        webTestClient.get().uri(PREFIX + "/nodes-with-high-incoming-fee-rate").exchange().expectBody()
                .jsonPath("$.entries[0].pubkey").value(is(PUBKEY.toString()))
                .jsonPath("$.entries[0].feeRate").value(is(123))
                .jsonPath("$.entries[1].pubkey").value(is(PUBKEY_2.toString()))
                .jsonPath("$.entries[1].feeRate").value(is(456));
    }
}
