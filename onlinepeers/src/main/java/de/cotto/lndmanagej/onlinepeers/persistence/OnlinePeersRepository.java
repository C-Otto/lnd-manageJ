package de.cotto.lndmanagej.onlinepeers.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.stream.Stream;

public interface OnlinePeersRepository extends JpaRepository<OnlinePeerJpaDto, Long> {
    Optional<OnlinePeerJpaDto> findTopByPubkeyOrderByTimestampDesc(String pubkey);

    Stream<OnlinePeerJpaDto> findByPubkeyOrderByTimestampDesc(String pubkey);
}
