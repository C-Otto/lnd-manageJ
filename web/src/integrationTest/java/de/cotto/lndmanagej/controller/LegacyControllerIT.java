package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelIdParser;
import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.PolicyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Set;

import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_3;
import static org.mockito.Mockito.when;

@WebFluxTest(LegacyController.class)
@Import(ChannelIdParser.class)
class LegacyControllerIT {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    @SuppressWarnings("unused")
    private ChannelIdResolver channelIdResolver;

    @MockBean
    @SuppressWarnings("unused")
    private NodeService nodeService;

    @MockBean
    @SuppressWarnings("unused")
    private PolicyService policyService;

    @MockBean
    private ChannelService channelService;

    @Test
    void getOpenChannelIdsPretty() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_3));
        webTestClient.get().uri("/legacy/open-channels/pretty").exchange()
                .expectStatus().isOk();
    }
}
