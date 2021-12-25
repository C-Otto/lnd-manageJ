package de.cotto.lndmanagej.onlinepeers.persistence;

import de.cotto.lndmanagej.model.Pubkey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static de.cotto.lndmanagej.model.OnlineStatusFixtures.ONLINE_STATUS;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnlinePeersDaoImplTest {
    private static final ZonedDateTime TIMESTAMP = ONLINE_STATUS.since();

    @InjectMocks
    private OnlinePeersDaoImpl dao;

    @Mock
    private OnlinePeersRepository repository;

    @Test
    void saveOnlineStatus_online() {
        dao.saveOnlineStatus(PUBKEY, true, TIMESTAMP);
        verifySave(PUBKEY, true);
    }

    @Test
    void saveOnlineStatus_offline() {
        dao.saveOnlineStatus(PUBKEY_2, false, TIMESTAMP);
        verifySave(PUBKEY_2, false);
    }

    @Test
    void getMostRecentOnlineStatus_not_found() {
        assertThat(dao.getMostRecentOnlineStatus(PUBKEY)).isEmpty();
    }

    @Test
    void getMostRecentOnlineStatus() {
        OnlinePeerJpaDto dto = new OnlinePeerJpaDto(PUBKEY, true, TIMESTAMP);
        when(repository.findTopByPubkeyOrderByTimestampDesc(PUBKEY.toString())).thenReturn(Optional.of(dto));
        assertThat(dao.getMostRecentOnlineStatus(PUBKEY)).contains(ONLINE_STATUS);
    }

    @Test
    void getAllForPeer() {
        OnlinePeerJpaDto dto1 = new OnlinePeerJpaDto(PUBKEY, true, TIMESTAMP);
        OnlinePeerJpaDto dto2 = new OnlinePeerJpaDto(PUBKEY, false, TIMESTAMP.plusSeconds(1));
        when(repository.findByPubkeyOrderByTimestampDesc(PUBKEY.toString())).thenReturn(List.of(dto1, dto2));
        assertThat(dao.getAllForPeer(PUBKEY)).containsExactly(dto1.toModel(), dto2.toModel());
    }

    private void verifySave(Pubkey pubkey, boolean expected) {
        verify(repository).save(argThat(privateChannelJpaDto -> privateChannelJpaDto.isOnline() == expected));
        verify(repository).save(argThat(dto -> pubkey.equals(Pubkey.create(Objects.requireNonNull(dto.getPubkey())))));
    }
}