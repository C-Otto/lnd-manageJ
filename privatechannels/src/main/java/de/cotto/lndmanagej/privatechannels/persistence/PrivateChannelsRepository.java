package de.cotto.lndmanagej.privatechannels.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PrivateChannelsRepository  extends JpaRepository<PrivateChannelJpaDto, Long> {
}
