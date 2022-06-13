package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.Rating;
import de.cotto.lndmanagej.service.RatingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RatingController.class)
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
        when(ratingService.getRatingForPeer(PUBKEY)).thenReturn(new Rating(123));
        mockMvc.perform(get(PREFIX + "/peer/" + PUBKEY + RATING))
                .andExpect(content().json("{\"rating\":  123, \"message\":  \"\"}"));
    }

    @Test
    void getRatingForPeer_no_rating() throws Exception {
        when(ratingService.getRatingForPeer(PUBKEY)).thenReturn(Rating.EMPTY);
        mockMvc.perform(get(PREFIX + "/peer/" + PUBKEY + RATING))
                .andExpect(content().json("{\"rating\":  -1, \"message\":  \"Unable to compute rating\"}"));
    }

    @Test
    void getRatingForChannel() throws Exception {
        when(ratingService.getRatingForChannel(CHANNEL_ID)).thenReturn(Optional.of(new Rating(123)));
        mockMvc.perform(get(PREFIX + "/channel/" + CHANNEL_ID + RATING))
                .andExpect(content().json("{\"rating\":  123, \"message\":  \"\"}"));
    }

    @Test
    void getRatingForChannel_not_found() throws Exception {
        mockMvc.perform(get(PREFIX + "/channel/" + CHANNEL_ID + RATING))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRatingForChannel_empty() throws Exception {
        when(ratingService.getRatingForChannel(CHANNEL_ID)).thenReturn(Optional.of(Rating.EMPTY));
        mockMvc.perform(get(PREFIX + "/channel/" + CHANNEL_ID + RATING))
                .andExpect(content().json("{\"rating\":  -1, \"message\":  \"Unable to compute rating\"}"));
    }
}
