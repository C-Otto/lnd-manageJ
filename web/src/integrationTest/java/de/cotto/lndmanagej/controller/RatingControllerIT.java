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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RatingController.class)
@Import(ChannelIdParser.class)
class RatingControllerIT {
    private static final String PREFIX = "/api/";
    private static final String RATING = "/rating";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @SuppressWarnings("unused")
    private ChannelIdResolver channelIdResolver;

    @MockBean
    private RatingService ratingService;

    @Test
    void getRatingForPeer() throws Exception {
        PeerRating rating = PeerRatingFixtures.ratingWithValue(123)
                .withChannelRating(ChannelRating.forChannel(CHANNEL_ID_2).addValueWithDescription(5, "something else"));
        when(ratingService.getRatingForPeer(PUBKEY)).thenReturn(Optional.of(rating));
        mockMvc.perform(get(PREFIX + "/node/" + PUBKEY + RATING))
                .andExpect(content().json("""
                        {
                          "rating": 128,
                          "message": "",
                          "descriptions": {
                            "%s rating": "128",
                            "%s rating": "123",
                            "%s something": "123",
                            "%s rating": "5",
                            "%s something else": "5"
                          }
                        }""".formatted(PUBKEY, CHANNEL_ID, CHANNEL_ID, CHANNEL_ID_2, CHANNEL_ID_2)));
    }

    @Test
    void getRatingForPeer_no_rating() throws Exception {
        when(ratingService.getRatingForPeer(PUBKEY)).thenReturn(Optional.empty());
        mockMvc.perform(get(PREFIX + "/node/" + PUBKEY + RATING))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRatingForChannel() throws Exception {
        ChannelRating rating = ChannelRatingFixtures.ratingWithValue(123).addValueWithDescription(456, "a");
        when(ratingService.getRatingForChannel(CHANNEL_ID)).thenReturn(Optional.of(rating));
        mockMvc.perform(get(PREFIX + "/channel/" + CHANNEL_ID + RATING))
                .andExpect(content().json("""
                        {
                          "rating": 579,
                          "message": "",
                          "descriptions": {
                            "%s a": "456",
                            "%s something": "123",
                            "%s rating": "579"
                          }
                        }""".formatted(CHANNEL_ID, CHANNEL_ID, CHANNEL_ID)));
    }

    @Test
    void getRatingForChannel_not_found() throws Exception {
        mockMvc.perform(get(PREFIX + "/channel/" + CHANNEL_ID + RATING))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRatingForChannel_empty() throws Exception {
        when(ratingService.getRatingForChannel(CHANNEL_ID)).thenReturn(Optional.empty());
        mockMvc.perform(get(PREFIX + "/channel/" + CHANNEL_ID + RATING))
                .andExpect(status().isNotFound());
    }
}
