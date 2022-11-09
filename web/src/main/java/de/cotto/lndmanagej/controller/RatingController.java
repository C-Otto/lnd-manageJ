package de.cotto.lndmanagej.controller;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.controller.dto.ObjectMapperConfiguration;
import de.cotto.lndmanagej.controller.dto.RatingDto;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.PeerRating;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.service.RatingService;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@Import(ObjectMapperConfiguration.class)
@RequestMapping("/api/")
public class RatingController {
    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @Timed
    @GetMapping("/node/{peer}/rating")
    public RatingDto getRatingForPeer(@PathVariable Pubkey peer) throws NotFoundException {
        Optional<PeerRating> rating = ratingService.getRatingForPeer(peer);
        if (rating.isEmpty()) {
            throw new NotFoundException();
        }
        return RatingDto.fromModel(rating.get());
    }

    @Timed
    @GetMapping("/channel/{channelId}/rating")
    public RatingDto getRatingForChannel(@PathVariable ChannelId channelId) throws NotFoundException {
        return ratingService.getRatingForChannel(channelId)
                .map(RatingDto::fromModel)
                .orElseThrow(NotFoundException::new);
    }
}
