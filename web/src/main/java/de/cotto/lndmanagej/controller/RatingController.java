package de.cotto.lndmanagej.controller;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.controller.dto.ObjectMapperConfiguration;
import de.cotto.lndmanagej.controller.dto.RatingDto;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.Rating;
import de.cotto.lndmanagej.service.RatingService;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Import(ObjectMapperConfiguration.class)
@RequestMapping("/api/")
public class RatingController {
    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @Timed
    @GetMapping("/peer/{peer}/rating")
    public RatingDto getRatingForPeer(@PathVariable Pubkey peer) {
        Rating rating = ratingService.getRatingForPeer(peer);
        return RatingDto.fromModel(rating);
    }

    @Timed
    @GetMapping("/channel/{channelId}/rating")
    public RatingDto getRatingForChannel(ChannelId channelId) throws NotFoundException {
        return ratingService.getRatingForChannel(channelId)
                .map(RatingDto::fromModel)
                .orElseThrow(NotFoundException::new);
    }
}
