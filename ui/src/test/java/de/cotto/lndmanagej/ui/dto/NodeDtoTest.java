package de.cotto.lndmanagej.ui.dto;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.RatingFixtures.RATING;
import static org.assertj.core.api.Assertions.assertThat;

class NodeDtoTest {

    public static final String ALICE = "alice";
    public static final String BOB = "bob";

    private static final NodeDto.OnlineStatusAndAliasComparator COMPARATOR
            = new NodeDto.OnlineStatusAndAliasComparator();

    @Test
    void compare_differentStatus_byStatus() {
        NodeDto alice = node(ALICE, true);
        NodeDto bob = node(BOB, false);
        assertThat(COMPARATOR.compare(alice, bob)).isPositive();
    }

    @Test
    void compare_bothOnline_byAlias() {
        NodeDto alice = node(ALICE, true);
        NodeDto bob = node(BOB, true);
        assertThat(COMPARATOR.compare(alice, bob)).isNegative();
    }

    @Test
    void compare_bothOffline_byAlias() {
        NodeDto alice = node(ALICE, false);
        NodeDto bob = node(BOB, false);
        assertThat(COMPARATOR.compare(alice, bob)).isNegative();
    }

    private NodeDto node(String alias, boolean online) {
        return new NodeDto(PUBKEY.toString(), alias, online, RATING.getRating());
    }
}