package de.cotto.lndmanagej.ui.dto;

import static de.cotto.lndmanagej.model.NodeFixtures.NODE;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.RatingFixtures.RATING;

public class NodeDtoFixture {

    public static final NodeDto NODE_DTO = new NodeDto(PUBKEY.toString(), NODE.alias(), true, RATING.getRating());

}