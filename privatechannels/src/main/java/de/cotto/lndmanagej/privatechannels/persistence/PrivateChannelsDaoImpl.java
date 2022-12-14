package de.cotto.lndmanagej.privatechannels.persistence;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.privatechannels.PrivateChannelsDao;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

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
