package de.cotto.lndmanagej.ui.dto;

import static de.cotto.lndmanagej.model.NodeFixtures.NODE;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_2;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.model.RatingFixtures.RATING;

public class NodeDtoFixture {
    public static final NodeDto NODE_DTO = new NodeDto(
            PUBKEY.toString(),
            NODE.alias(),
            true,
            RATING.value()
    );

    public static final NodeDto NODE_DTO_2 = new NodeDto(
            PUBKEY_2.toString(),
            NODE_2.alias(),
            true,
            RATING.value()
    );

    public static final NodeDto NODE_DTO_3 = new NodeDto(
            PUBKEY_3.toString(),
            NODE_3.alias(),
            true,
            RATING.value()
    );
}
