package de.cotto.lndmanagej.statistics.persistence;

import de.cotto.lndmanagej.model.Pubkey;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@IdClass(OnlinePeerId.class)
@Table(name = "online_peers")
public class OnlinePeerJpaDto {
    @Id
    @Nullable
    private String pubkey;

    @Id
    @SuppressWarnings({"PMD.SingularField", "unused"})
    private long timestamp;

    private boolean online;

    @SuppressWarnings("unused")
    public OnlinePeerJpaDto() {
        // for JPA
    }

    public OnlinePeerJpaDto(Pubkey pubkey, boolean online, LocalDateTime timestamp) {
        this.pubkey = pubkey.toString();
        this.online = online;
        this.timestamp = timestamp.toEpochSecond(ZoneOffset.UTC);
    }

    public boolean isOnline() {
        return online;
    }

    @CheckForNull
    public String getPubkey() {
        return pubkey;
    }
}
