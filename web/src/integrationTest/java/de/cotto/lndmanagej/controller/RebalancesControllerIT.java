package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelIdParser;
import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.service.RebalanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.mockito.Mockito.when;

@WebFluxTest(RebalancesController.class)
@Import(ChannelIdParser.class)
class RebalancesControllerIT {
    private static final String CHANNEL_PREFIX = "/api/channel/" + CHANNEL_ID.getShortChannelId() + "/";
    private static final String NODE_PREFIX = "/api/node/" + PUBKEY + "/";

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    @SuppressWarnings("unused")
    private ChannelIdResolver channelIdResolver;

    @MockBean
    private RebalanceService rebalanceService;

    @Test
    void getRebalanceSourceCostsForChannel() {
        when(rebalanceService.getSourceCostsForChannel(CHANNEL_ID)).thenReturn(Coins.ofMilliSatoshis(123));
        webTestClient.get().uri(CHANNEL_PREFIX + "/rebalance-source-costs").exchange()
                .expectBody(String.class).isEqualTo("123");
    }

    @Test
    void getRebalanceSourceAmountForChannel() {
        when(rebalanceService.getAmountFromChannel(CHANNEL_ID)).thenReturn(Coins.ofMilliSatoshis(456));
        webTestClient.get().uri(CHANNEL_PREFIX + "/rebalance-source-amount").exchange()
                .expectBody(String.class).isEqualTo("456");
    }

    @Test
    void getRebalanceSourceCostsForPeer() {
        when(rebalanceService.getSourceCostsForPeer(PUBKEY)).thenReturn(Coins.ofMilliSatoshis(124));
        webTestClient.get().uri(NODE_PREFIX + "/rebalance-source-costs").exchange()
                .expectBody(String.class).isEqualTo("124");
    }

    @Test
    void getRebalanceSourceAmountForPeer() {
        when(rebalanceService.getAmountFromPeer(PUBKEY)).thenReturn(Coins.ofMilliSatoshis(666));
        webTestClient.get().uri(NODE_PREFIX + "/rebalance-source-amount").exchange()
                .expectBody(String.class).isEqualTo("666");
    }

    @Test
    void getRebalanceTargetCostsForChannel() {
        when(rebalanceService.getTargetCostsForChannel(CHANNEL_ID)).thenReturn(Coins.ofMilliSatoshis(125));
        webTestClient.get().uri(CHANNEL_PREFIX + "/rebalance-target-costs").exchange()
                .expectBody(String.class).isEqualTo("125");
    }

    @Test
    void getRebalanceTargetAmountForChannel() {
        when(rebalanceService.getAmountToChannel(CHANNEL_ID)).thenReturn(Coins.ofMilliSatoshis(7777));
        webTestClient.get().uri(CHANNEL_PREFIX + "/rebalance-target-amount").exchange()
                .expectBody(String.class).isEqualTo("7777");
    }

    @Test
    void getRebalanceTargetCostsForPeer() {
        when(rebalanceService.getTargetCostsForPeer(PUBKEY)).thenReturn(Coins.ofMilliSatoshis(126));
        webTestClient.get().uri(NODE_PREFIX + "/rebalance-target-costs").exchange()
                .expectBody(String.class).isEqualTo("126");
    }

    @Test
    void getRebalanceTargetAmountForPeer() {
        when(rebalanceService.getAmountToPeer(PUBKEY)).thenReturn(Coins.ofMilliSatoshis(999));
        webTestClient.get().uri(NODE_PREFIX + "/rebalance-target-amount").exchange()
                .expectBody(String.class).isEqualTo("999");
    }

    @Test
    void getRebalanceSupportAsSourceAmountForChannel() {
        when(rebalanceService.getSupportAsSourceAmountFromChannel(CHANNEL_ID)).thenReturn(Coins.ofMilliSatoshis(1));
        webTestClient.get().uri(CHANNEL_PREFIX + "/rebalance-support-as-source-amount").exchange()
                .expectBody(String.class).isEqualTo("1");
    }

    @Test
    void getRebalanceSupportAsSourceAmountForPeer() {
        when(rebalanceService.getSupportAsSourceAmountFromPeer(PUBKEY)).thenReturn(Coins.ofMilliSatoshis(2));
        webTestClient.get().uri(NODE_PREFIX + "/rebalance-support-as-source-amount").exchange()
                .expectBody(String.class).isEqualTo("2");
    }

    @Test
    void getRebalanceSupportAsTargetAmountForChannel() {
        when(rebalanceService.getSupportAsTargetAmountToChannel(CHANNEL_ID)).thenReturn(Coins.ofMilliSatoshis(3));
        webTestClient.get().uri(CHANNEL_PREFIX + "/rebalance-support-as-target-amount").exchange()
                .expectBody(String.class).isEqualTo("3");
    }

    @Test
    void getRebalanceSupportAsTargetAmountForPeer() {
        when(rebalanceService.getSupportAsTargetAmountToPeer(PUBKEY)).thenReturn(Coins.ofMilliSatoshis(4));
        webTestClient.get().uri(NODE_PREFIX + "/rebalance-support-as-target-amount").exchange()
                .expectBody(String.class).isEqualTo("4");
    }
}
