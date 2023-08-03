package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelIdParser;
import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.warnings.ChannelWarnings;
import de.cotto.lndmanagej.model.warnings.NodeWarnings;
import de.cotto.lndmanagej.service.ChannelWarningsService;
import de.cotto.lndmanagej.service.NodeWarningsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_2;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_3;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_2;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.model.warnings.ChannelWarningsFixtures.CHANNEL_WARNINGS;
import static de.cotto.lndmanagej.model.warnings.NodeWarningsFixtures.NODE_WARNINGS;
import static de.cotto.lndmanagej.model.warnings.NodeWarningsFixtures.NODE_WARNINGS_2;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@WebFluxTest(WarningsController.class)
@Import(ChannelIdParser.class)
class WarningsControllerIT {
    private static final String NODE_PREFIX = "/api/node/" + PUBKEY;
    private static final String CHANNEL_PREFIX = "/api/channel/" + CHANNEL_ID;
    private static final String WARNINGS = "/warnings";
    private static final String WARNINGS_PATH = "$.warnings";

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    @SuppressWarnings("unused")
    private ChannelIdResolver channelIdResolver;

    @MockBean
    private NodeWarningsService nodeWarningsService;

    @MockBean
    private ChannelWarningsService channelWarningsService;

    @Test
    void getWarningsForNode() {
        when(nodeWarningsService.getNodeWarnings(PUBKEY)).thenReturn(NODE_WARNINGS);
        webTestClient.get().uri(NODE_PREFIX + WARNINGS).exchange().expectBody()
                .jsonPath(WARNINGS_PATH).value(containsInAnyOrder(
                        "No flow in the past 16 days",
                        "Node has been online 51% in the past 14 days",
                        "Node changed between online and offline 123 times in the past 7 days"
                ));
    }

    @Test
    void getWarningsForNode_empty() {
        when(nodeWarningsService.getNodeWarnings(PUBKEY)).thenReturn(NodeWarnings.NONE);
        webTestClient.get().uri(NODE_PREFIX + WARNINGS).exchange().expectBody()
                .jsonPath(WARNINGS_PATH).value(hasSize(0));
    }

    @Test
    void getWarningsForChannel() {
        when(channelWarningsService.getChannelWarnings(CHANNEL_ID)).thenReturn(CHANNEL_WARNINGS);
        webTestClient.get().uri(CHANNEL_PREFIX + WARNINGS).exchange().expectBody()
                .jsonPath(WARNINGS_PATH).value(containsInAnyOrder(
                        "Channel has accumulated 101,000 updates"
                ));
    }

    @Test
    void getWarningsForChannel_empty() {
        when(channelWarningsService.getChannelWarnings(CHANNEL_ID)).thenReturn(ChannelWarnings.NONE);
        webTestClient.get().uri(CHANNEL_PREFIX + WARNINGS).exchange().expectBody()
                .jsonPath(WARNINGS_PATH).value(hasSize(0));
    }

    @Test
    void getWarnings_empty() {
        when(nodeWarningsService.getNodeWarnings()).thenReturn(Map.of());
        webTestClient.get().uri("/api" + WARNINGS).exchange().expectBody()
                .jsonPath("$.nodesWithWarnings").value(hasSize(0))
                .jsonPath("$.channelsWithWarnings").value(hasSize(0));
    }

    @Test
    void getWarnings() {
        when(nodeWarningsService.getNodeWarnings())
                .thenReturn(Map.of(NODE_2, NODE_WARNINGS, NODE_3, NODE_WARNINGS_2));
        when(channelWarningsService.getChannelWarnings())
                .thenReturn(Map.of(LOCAL_OPEN_CHANNEL, CHANNEL_WARNINGS));
        webTestClient.get().uri("/api" + WARNINGS).exchange().expectBody()
                .jsonPath("$.nodesWithWarnings[0].alias").value(is(ALIAS_2))
                .jsonPath("$.nodesWithWarnings[0].pubkey").value(is(PUBKEY_2.toString()))
                .jsonPath("$.nodesWithWarnings[0].warnings").value(containsInAnyOrder(
                        "No flow in the past 16 days",
                        "Node has been online 51% in the past 14 days",
                        "Node changed between online and offline 123 times in the past 7 days"
                ))
                .jsonPath("$.nodesWithWarnings[1].alias").value(is(ALIAS_3))
                .jsonPath("$.nodesWithWarnings[1].pubkey").value(is(PUBKEY_3.toString()))
                .jsonPath("$.nodesWithWarnings[1].warnings").value(containsInAnyOrder(
                        "Node has been online 1% in the past 21 days",
                        "Node changed between online and offline 99 times in the past 14 days"
                ))
                .jsonPath("$.channelsWithWarnings[0].channelId").value(is(CHANNEL_ID.toString()))
                .jsonPath("$.channelsWithWarnings[0].warnings").value(containsInAnyOrder(
                        "Channel has accumulated 101,000 updates"
                ));
    }
}
