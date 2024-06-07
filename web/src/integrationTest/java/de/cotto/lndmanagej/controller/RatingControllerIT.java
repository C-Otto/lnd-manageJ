package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelIdParser;
import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.ChannelRating;
import de.cotto.lndmanagej.model.ChannelRatingFixtures;
import de.cotto.lndmanagej.model.PeerRating;
import de.cotto.lndmanagej.model.PeerRatingFixtures;
import de.cotto.lndmanagej.service.RatingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.mockito.Mockito.when;

@WebFluxTest(RatingController.class)
@Import(ChannelIdParser.class)
class RatingControllerIT {
    private static final String PREFIX = "/api/";
    private static final String RATING = "/rating";

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    @SuppressWarnings("unused")
    private ChannelIdResolver channelIdResolver;

    @MockBean
    private RatingService ratingService;

    @Test
    void getRatingForPeer() {
        PeerRating rating = PeerRatingFixtures.ratingWithValue(123)
                .withChannelRating(ChannelRating.forChannel(CHANNEL_ID_2).addValueWithDescription(5, "something else"));
        when(ratingService.getRatingForPeer(PUBKEY)).thenReturn(Optional.of(rating));
        webTestClient.get().uri(PREFIX + "/node/" + PUBKEY + RATING).exchange()
                .expectBody()
                .json("""
                        {\
                          "rating": 128,\
                          "message": "",\
                          "descriptions": {\
                            "%s rating": "128",\
                            "%s raw rating": "123",\
                            "%s something": "123",\
                            "%s raw rating": "5",\
                            "%s something else": "5"\
                          }\
                        }""".formatted(PUBKEY, CHANNEL_ID, CHANNEL_ID, CHANNEL_ID_2, CHANNEL_ID_2));
    }

    @Test
    void getRatingForPeer_no_rating() {
        when(ratingService.getRatingForPeer(PUBKEY)).thenReturn(Optional.empty());
        webTestClient.get().uri(PREFIX + "/node/" + PUBKEY + RATING).exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getRatingForChannel() {
        ChannelRating rating = ChannelRatingFixtures.ratingWithValue(123).addValueWithDescription(456, "a");
        when(ratingService.getRatingForChannel(CHANNEL_ID)).thenReturn(Optional.of(rating));
        webTestClient.get().uri(PREFIX + "/channel/" + CHANNEL_ID + RATING).exchange()
                .expectBody()
                .json("""
                        {\
                          "rating": 579,\
                          "message": "",\
                          "descriptions": {\
                            "%s a": "456",\
                            "%s something": "123",\
                            "%s raw rating": "579"\
                          }\
                        }""".formatted(CHANNEL_ID, CHANNEL_ID, CHANNEL_ID));
    }

    @Test
    void getRatingForChannel_not_found() {
        webTestClient.get().uri(PREFIX + "/channel/" + CHANNEL_ID + RATING).exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getRatingForChannel_empty() {
        when(ratingService.getRatingForChannel(CHANNEL_ID)).thenReturn(Optional.empty());
        webTestClient.get().uri(PREFIX + "/channel/" + CHANNEL_ID + RATING).exchange()
                .expectStatus().isNotFound();
    }
}
