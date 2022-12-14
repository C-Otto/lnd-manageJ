package de.cotto.lndmanagej.privatechannels.persistence;

import de.cotto.lndmanagej.model.ChannelId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "private_channels")
public class PrivateChannelJpaDto {
    @Id
    @SuppressWarnings({"unused", "PMD.SingularField"})
    private long channelId;

    @SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
    private boolean isPrivate;

    @SuppressWarnings("unused")
    public PrivateChannelJpaDto() {
        // for JPA
    }

    public PrivateChannelJpaDto(ChannelId channelId, boolean isPrivate) {
        this.channelId = channelId.getShortChannelId();
        this.isPrivate = isPrivate;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public long getChannelId() {
        return channelId;
    }
}
