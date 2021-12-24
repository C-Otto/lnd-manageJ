package de.cotto.lndmanagej.onlinepeers.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OnlinePeersRepository extends JpaRepository<OnlinePeerJpaDto, Long> {
    Optional<OnlinePeerJpaDto> findTopByPubkeyOrderByTimestampDesc(String pubkey);
}
