package de.cotto.lndmanagej.onlinepeers.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OnlinePeersRepositoryIT {
    private static final ZonedDateTime TIMESTAMP = ZonedDateTime.now(ZoneOffset.UTC);

    @Autowired
    private OnlinePeersRepository repository;

    @Test
    void save() {
        repository.save(new OnlinePeerJpaDto(PUBKEY, true, TIMESTAMP));
        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    void findTopByPubkeyOrderByTimestampDesc_not_found() {
        assertThat(repository.findTopByPubkeyOrderByTimestampDesc(PUBKEY.toString()))
                .isEmpty();
    }

    @Test
    void findTopByPubkeyOrderByTimestampDesc() {
        repository.save(new OnlinePeerJpaDto(PUBKEY, true, TIMESTAMP));
        assertThat(repository.findTopByPubkeyOrderByTimestampDesc(PUBKEY.toString()))
                .map(OnlinePeerJpaDto::isOnline).contains(true);
    }

    @Test
    void findTopByPubkeyOrderByTimestampDesc_wrong_pubkey() {
        repository.save(new OnlinePeerJpaDto(PUBKEY, true, TIMESTAMP));
        assertThat(repository.findTopByPubkeyOrderByTimestampDesc(PUBKEY_2.toString()))
                .isEmpty();
    }

    @Test
    void findTopByPubkeyOrderByTimestampDesc_returns_most_recent() {
        repository.save(new OnlinePeerJpaDto(PUBKEY, true, TIMESTAMP));
        repository.save(new OnlinePeerJpaDto(PUBKEY, false, TIMESTAMP.minusSeconds(1)));
        assertThat(repository.findTopByPubkeyOrderByTimestampDesc(PUBKEY.toString()))
                .map(OnlinePeerJpaDto::isOnline).contains(true);
    }
}