package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.controller.dto.RatingDto;
import de.cotto.lndmanagej.model.ChannelRating;
import de.cotto.lndmanagej.model.ChannelRatingFixtures;
import de.cotto.lndmanagej.model.PeerRating;
import de.cotto.lndmanagej.model.PeerRatingFixtures;
import de.cotto.lndmanagej.service.RatingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingControllerTest {
    @InjectMocks
    private RatingController ratingController;

    @Mock
    private RatingService ratingService;

    @Test
    void getRatingForPeer_empty() {
        when(ratingService.getRatingForPeer(PUBKEY)).thenReturn(Optional.empty());
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> ratingController.getRatingForPeer(PUBKEY));
    }

    @Test
    void getRatingForPeer() throws NotFoundException {
        PeerRating rating = PeerRatingFixtures.ratingWithValue(123);
        when(ratingService.getRatingForPeer(PUBKEY)).thenReturn(Optional.of(rating));
        assertThat(ratingController.getRatingForPeer(PUBKEY)).isEqualTo(RatingDto.fromModel(rating));
    }

    @Test
    void getRatingForChannel_channel_not_found() {
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> ratingController.getRatingForChannel(CHANNEL_ID)
        );
    }

    @Test
    void getRatingForChannel() throws Exception {
        ChannelRating rating = ChannelRatingFixtures.ratingWithValue(1)
                .addValueWithDescription(123L, "some description");
        when(ratingService.getRatingForChannel(CHANNEL_ID)).thenReturn(Optional.of(rating));
        assertThat(ratingController.getRatingForChannel(CHANNEL_ID)).isEqualTo(RatingDto.fromModel(rating));
    }
}
