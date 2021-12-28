package de.cotto.lndmanagej.onlinepeers.persistence;

import de.cotto.lndmanagej.model.OnlineStatus;
import de.cotto.lndmanagej.model.Pubkey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnlinePeersDaoImplTest {
    private static final ZonedDateTime NOW = ZonedDateTime.now(ZoneOffset.UTC);
    private static final int DAY_THRESHOLD = 10;

    @InjectMocks
    private OnlinePeersDaoImpl dao;

    @Mock
    private OnlinePeersRepository repository;

    @Test
    void saveOnlineStatus_online() {
        dao.saveOnlineStatus(PUBKEY, true, NOW);
        verifySave(PUBKEY, true);
    }

    @Test
    void saveOnlineStatus_offline() {
        dao.saveOnlineStatus(PUBKEY_2, false, NOW);
        verifySave(PUBKEY_2, false);
    }

    @Test
    void getMostRecentOnlineStatus_not_found() {
        assertThat(dao.getMostRecentOnlineStatus(PUBKEY)).isEmpty();
    }

    @Test
    void getMostRecentOnlineStatus() {
        OnlinePeerJpaDto dto = new OnlinePeerJpaDto(PUBKEY, true, NOW);
        when(repository.findTopByPubkeyOrderByTimestampDesc(PUBKEY.toString())).thenReturn(Optional.of(dto));
        assertThat(dao.getMostRecentOnlineStatus(PUBKEY)).contains(new OnlineStatus(
                true,
                NOW.truncatedTo(ChronoUnit.SECONDS)
        ));
    }

    @Test
    void getAllForPeerAfterDays_threshold_covers_all_entries() {
        OnlinePeerJpaDto dto1 = new OnlinePeerJpaDto(PUBKEY, true, NOW);
        OnlinePeerJpaDto dto2 = new OnlinePeerJpaDto(PUBKEY, false, NOW.minusSeconds(1));
        when(repository.findByPubkeyOrderByTimestampDesc(PUBKEY.toString())).thenReturn(Stream.of(dto1, dto2));
        assertThat(dao.getAllForPeerUpToAgeInDays(PUBKEY, DAY_THRESHOLD))
                .containsExactly(dto1.toModel(), dto2.toModel());
    }

    @Test
    void getAllForPeerAfterDays_includes_first_entry_starting_after_threshold() {
        OnlinePeerJpaDto dto1 = new OnlinePeerJpaDto(PUBKEY, true, NOW);
        OnlinePeerJpaDto dto2 = new OnlinePeerJpaDto(PUBKEY, false, NOW.plusDays(DAY_THRESHOLD).minusYears(1));
        when(repository.findByPubkeyOrderByTimestampDesc(PUBKEY.toString())).thenReturn(Stream.of(dto1, dto2));
        assertThat(dao.getAllForPeerUpToAgeInDays(PUBKEY, DAY_THRESHOLD)).hasSize(2);
    }

    @Test
    void getAllForPeerAfterDays_ignores_older_entries() {
        OnlinePeerJpaDto dto1 = new OnlinePeerJpaDto(PUBKEY, true, NOW);
        OnlinePeerJpaDto dto2 = new OnlinePeerJpaDto(PUBKEY, false, NOW.minusYears(1));
        OnlinePeerJpaDto dto3 = new OnlinePeerJpaDto(PUBKEY, false, NOW.minusYears(2));
        when(repository.findByPubkeyOrderByTimestampDesc(PUBKEY.toString())).thenReturn(Stream.of(dto1, dto2, dto3));
        assertThat(dao.getAllForPeerUpToAgeInDays(PUBKEY, DAY_THRESHOLD)).hasSize(2);
    }

    private void verifySave(Pubkey pubkey, boolean expected) {
        verify(repository).save(argThat(privateChannelJpaDto -> privateChannelJpaDto.isOnline() == expected));
        verify(repository).save(argThat(dto -> pubkey.equals(Pubkey.create(Objects.requireNonNull(dto.getPubkey())))));
    }
}