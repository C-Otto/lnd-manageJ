package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelIdParser;
import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.service.OnChainCostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.OnChainCostsFixtures.ON_CHAIN_COSTS;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@WebFluxTest(OnChainCostsController.class)
@Import(ChannelIdParser.class)
class OnChainCostsControllerIT {
    private static final String CHANNEL_PREFIX = "/api/channel/" + CHANNEL_ID.getShortChannelId();
    private static final String PEER_PREFIX = "/api/node/" + PUBKEY;

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    @SuppressWarnings("unused")
    private ChannelIdResolver channelIdResolver;

    @MockBean
    private OnChainCostService onChainCostService;

    @Test
    void on_chain_costs_for_peer() {
        when(onChainCostService.getOnChainCostsForPeer(PUBKEY)).thenReturn(ON_CHAIN_COSTS);
        webTestClient.get().uri(PEER_PREFIX + "/on-chain-costs").exchange().expectBody()
                .jsonPath("$.openCostsSat").value(is("1000"))
                .jsonPath("$.closeCostsSat").value(is("2000"))
                .jsonPath("$.sweepCostsSat").value(is("3000"));
    }

    @Test
    void open_costs_for_channel() {
        when(onChainCostService.getOpenCostsForChannelId(CHANNEL_ID)).thenReturn(Optional.of(Coins.ofSatoshis(123)));
        webTestClient.get().uri(CHANNEL_PREFIX + "/open-costs").exchange()
                .expectBody(String.class).isEqualTo("123");
    }

    @Test
    void open_costs_for_channel_unknown() {
        webTestClient.get().uri(CHANNEL_PREFIX + "/open-costs").exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Unable to get open costs for channel with ID " + CHANNEL_ID);
    }

    @Test
    void close_costs_for_channel() {
        when(onChainCostService.getCloseCostsForChannelId(CHANNEL_ID)).thenReturn(Optional.of(Coins.ofSatoshis(123)));
        webTestClient.get().uri(CHANNEL_PREFIX + "/close-costs").exchange()
                .expectBody(String.class).isEqualTo("123");
    }

    @Test
    void sweep_costs_for_channel() {
        when(onChainCostService.getSweepCostsForChannelId(CHANNEL_ID)).thenReturn(Optional.of(Coins.ofSatoshis(123)));
        webTestClient.get().uri(CHANNEL_PREFIX + "/sweep-costs").exchange()
                .expectBody(String.class).isEqualTo("123");
    }

    @Test
    void close_costs_for_channel_unknown() {
        webTestClient.get().uri(CHANNEL_PREFIX + "/close-costs").exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Unable to get close costs for channel with ID " + CHANNEL_ID);
    }

    @Test
    void sweep_costs_channel_unknown() {
        webTestClient.get().uri(CHANNEL_PREFIX + "/sweep-costs").exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Unable to get sweep costs for channel with ID " + CHANNEL_ID);
    }
}
