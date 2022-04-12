package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcMissionControl;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.MissionControlEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static java.util.stream.Collectors.toCollection;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MissionControlServiceTest {
    private static final Instant NOW = Instant.now();
    private static final Instant ONE_HOUR_BEFORE = NOW.minus(Duration.ofHours(1));
    private static final Instant ONE_SECOND_BEFORE = NOW.minus(Duration.ofSeconds(1));
    private static final Instant THREE_SECONDS_BEFORE = NOW.minus(Duration.ofSeconds(3));
    private static final Instant TWO_SECONDS_BEFORE = NOW.minus(Duration.ofSeconds(2));
    private static final Coins SMALLER_AMOUNT = Coins.ofSatoshis(100);
    private static final Coins AMOUNT = Coins.ofSatoshis(123);
    private static final Coins LARGER_AMOUNT = Coins.ofSatoshis(200);

    @InjectMocks
    private MissionControlService missionControlService;

    @Mock
    private GrpcMissionControl grpcMissionControl;

    @Test
    void unknown_empty_optional() {
        assertThat(missionControlService.getMinimumOfRecentFailures(PUBKEY, PUBKEY_2)).isEmpty();
    }

    @Test
    void unknown_no_entry() {
        mockEntries();
        assertThat(missionControlService.getMinimumOfRecentFailures(PUBKEY, PUBKEY_2)).isEmpty();
    }

    @Test
    void known_too_old() {
        mockEntries(new MissionControlEntry(PUBKEY, PUBKEY_2, AMOUNT, Instant.ofEpochSecond(0), true));
        assertThat(missionControlService.getMinimumOfRecentFailures(PUBKEY, PUBKEY_2)).isEmpty();
    }

    @Test
    void known_but_success() {
        mockEntries(new MissionControlEntry(PUBKEY, PUBKEY_2, AMOUNT, NOW, false));
        assertThat(missionControlService.getMinimumOfRecentFailures(PUBKEY, PUBKEY_2)).isEmpty();
    }

    @Test
    void known_slightly_too_old() {
        mockEntries(new MissionControlEntry(PUBKEY, PUBKEY_2, AMOUNT, ONE_HOUR_BEFORE, true));
        assertThat(missionControlService.getMinimumOfRecentFailures(PUBKEY, PUBKEY_2)).isEmpty();
    }

    @Test
    void known_very_recent() {
        mockEntries(new MissionControlEntry(PUBKEY, PUBKEY_2, AMOUNT, ONE_SECOND_BEFORE, true));
        assertThat(missionControlService.getMinimumOfRecentFailures(PUBKEY, PUBKEY_2)).contains(AMOUNT);
    }

    @Test
    void known_but_wrong_target() {
        mockEntries(new MissionControlEntry(PUBKEY, PUBKEY_3, AMOUNT, NOW, true));
        assertThat(missionControlService.getMinimumOfRecentFailures(PUBKEY, PUBKEY_2)).isEmpty();
    }

    @Test
    void known_but_wrong_source() {
        mockEntries(new MissionControlEntry(PUBKEY_3, PUBKEY_2, AMOUNT, NOW, true));
        assertThat(missionControlService.getMinimumOfRecentFailures(PUBKEY, PUBKEY_2)).isEmpty();
    }

    @Test
    void smaller_failure_most_recent() {
        mockEntries(
                new MissionControlEntry(PUBKEY, PUBKEY_2, AMOUNT, THREE_SECONDS_BEFORE, true),
                new MissionControlEntry(PUBKEY, PUBKEY_2, LARGER_AMOUNT, TWO_SECONDS_BEFORE, true),
                new MissionControlEntry(PUBKEY, PUBKEY_2, SMALLER_AMOUNT, ONE_SECOND_BEFORE, true)
        );
        assertThat(missionControlService.getMinimumOfRecentFailures(PUBKEY, PUBKEY_2)).contains(SMALLER_AMOUNT);
    }

    @Test
    void larger_failure_more_recent() {
        mockEntries(
                new MissionControlEntry(PUBKEY, PUBKEY_2, AMOUNT, THREE_SECONDS_BEFORE, true),
                new MissionControlEntry(PUBKEY, PUBKEY_2, SMALLER_AMOUNT, TWO_SECONDS_BEFORE, true),
                new MissionControlEntry(PUBKEY, PUBKEY_2, LARGER_AMOUNT, ONE_SECOND_BEFORE, true)
        );
        assertThat(missionControlService.getMinimumOfRecentFailures(PUBKEY, PUBKEY_2)).contains(SMALLER_AMOUNT);
    }

    @Test
    void smallest_failure_too_old() {
        mockEntries(
                new MissionControlEntry(PUBKEY, PUBKEY_2, SMALLER_AMOUNT, ONE_HOUR_BEFORE, true),
                new MissionControlEntry(PUBKEY, PUBKEY_2, LARGER_AMOUNT, TWO_SECONDS_BEFORE, true)
        );
        assertThat(missionControlService.getMinimumOfRecentFailures(PUBKEY, PUBKEY_2)).contains(LARGER_AMOUNT);
    }

    @Test
    void known_somewhat_recent() {
        Instant time = ONE_HOUR_BEFORE.plus(Duration.ofSeconds(10));
        mockEntries(new MissionControlEntry(PUBKEY, PUBKEY_2, AMOUNT, time, true));
        assertThat(missionControlService.getMinimumOfRecentFailures(PUBKEY, PUBKEY_2)).contains(AMOUNT);
    }

    private void mockEntries(MissionControlEntry... entries) {
        Set<MissionControlEntry> set = Arrays.stream(entries).collect(toCollection(LinkedHashSet::new));
        when(grpcMissionControl.getEntries()).thenReturn(Optional.of(set));
    }
}
