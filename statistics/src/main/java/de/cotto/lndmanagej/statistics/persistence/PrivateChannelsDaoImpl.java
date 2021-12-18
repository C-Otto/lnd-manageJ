package de.cotto.lndmanagej.statistics.persistence;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.statistics.PrivateChannelsDao;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Optional;

@Component
@Transactional
class PrivateChannelsDaoImpl implements PrivateChannelsDao {
    private final PrivateChannelsRepository privateChannelsRepository;

    public PrivateChannelsDaoImpl(PrivateChannelsRepository privateChannelsRepository) {
        this.privateChannelsRepository = privateChannelsRepository;
    }

    @Override
    public Optional<Boolean> isPrivate(ChannelId channelId) {
        return privateChannelsRepository.findById(channelId.getShortChannelId()).map(PrivateChannelJpaDto::isPrivate);
    }

    @Override
    public void savePrivateFlag(ChannelId channelId, boolean isPrivate) {
        privateChannelsRepository.save(new PrivateChannelJpaDto(channelId, isPrivate));
    }
}
