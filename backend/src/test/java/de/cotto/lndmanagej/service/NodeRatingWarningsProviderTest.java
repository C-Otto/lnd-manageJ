package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.model.warnings.NodeRatingWarning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.configuration.WarningsConfigurationSettings.NODE_RATING_THRESHOLD;
import static de.cotto.lndmanagej.configuration.WarningsConfigurationSettings.NODE_RATING_WARNING_IGNORE_NODE;
import static de.cotto.lndmanagej.model.PeerRatingFixtures.ratingWithValue;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NodeRatingWarningsProviderTest {
    @InjectMocks
    private NodeRatingWarningsProvider nodeRatingWarningsProvider;

    @Mock
    private RatingService ratingService;

    @Mock
    private ConfigurationService configurationService;

    @Test
    void no_rating() {
        when(ratingService.getRatingForPeer(PUBKEY)).thenReturn(Optional.empty());
        assertThat(nodeRatingWarningsProvider.getNodeWarnings(PUBKEY)).isEmpty();
    }

    @Test
    void high_rating() {
        when(ratingService.getRatingForPeer(PUBKEY)).thenReturn(Optional.of(ratingWithValue(10_000)));
        assertThat(nodeRatingWarningsProvider.getNodeWarnings(PUBKEY)).isEmpty();
    }

    @Test
    void low_rating() {
        when(ratingService.getRatingForPeer(PUBKEY)).thenReturn(Optional.of(ratingWithValue(1)));
        assertThat(nodeRatingWarningsProvider.getNodeWarnings(PUBKEY)).contains(new NodeRatingWarning(1, 1_000));
    }

    @Test
    void low_rating_with_configured_threshold() {
        when(configurationService.getIntegerValue(NODE_RATING_THRESHOLD)).thenReturn(Optional.of(1_002));
        when(ratingService.getRatingForPeer(PUBKEY)).thenReturn(Optional.of(ratingWithValue(1_001)));
        assertThat(nodeRatingWarningsProvider.getNodeWarnings(PUBKEY)).contains(new NodeRatingWarning(1_001, 1_002));
    }

    @Nested
    class IgnoredWarnings {
        @BeforeEach
        void setUp() {
            lenient().when(ratingService.getRatingForPeer(PUBKEY)).thenReturn(Optional.of(ratingWithValue(1)));
        }

        @Test
        void no_warning_for_ignored_node() {
            when(configurationService.getPubkeys(NODE_RATING_WARNING_IGNORE_NODE)).thenReturn(Set.of(PUBKEY_2, PUBKEY));
            assertThat(nodeRatingWarningsProvider.getNodeWarnings(PUBKEY)).isEmpty();
        }

        @Test
        void warning_if_other_node_is_ignored() {
            when(configurationService.getPubkeys(NODE_RATING_WARNING_IGNORE_NODE))
                    .thenReturn(Set.of(PUBKEY_2, PUBKEY_3));
            assertThat(nodeRatingWarningsProvider.getNodeWarnings(PUBKEY)).isNotEmpty();
        }
    }
}
