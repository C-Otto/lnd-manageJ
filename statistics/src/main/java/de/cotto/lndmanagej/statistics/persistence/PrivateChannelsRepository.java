package de.cotto.lndmanagej.statistics.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PrivateChannelsRepository  extends JpaRepository<PrivateChannelJpaDto, Long> {
}
