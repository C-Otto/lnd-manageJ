package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.controller.dto.ObjectMapperConfiguration;
import de.cotto.lndmanagej.model.ChannelIdParser;
import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.GraphService;
import de.cotto.lndmanagej.service.OwnNodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Set;

import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static org.mockito.Mockito.when;

@WebFluxTest(StatusController.class)
@Import({ObjectMapperConfiguration.class, ChannelIdParser.class})
class ObjectMapperConfigurationIT {
    private static final String PREFIX = "/api/status/";

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    @SuppressWarnings("unused")
    private ChannelIdResolver channelIdResolver;

    @MockBean
    private ChannelService channelService;

    @MockBean
    @SuppressWarnings("unused")
    private OwnNodeService ownNodeService;

    @MockBean
    @SuppressWarnings("unused")
    private GraphService graphService;

    @Test
    void output_is_pretty_printed() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL));
        String expectedString = """
                {
                  "channels" : [ "712345x123x1" ]
                }""";
        webTestClient.get().uri(PREFIX + "/open-channels").exchange()
                .expectBody(String.class).isEqualTo(expectedString);
    }
}
