package de.cotto.lndmanagej.onlinepeers.persistence;

import de.cotto.lndmanagej.model.OnlineStatus;
import de.cotto.lndmanagej.model.Pubkey;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static java.time.ZoneOffset.UTC;

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

    public OnlinePeerJpaDto(Pubkey pubkey, boolean online, ZonedDateTime timestamp) {
        this.pubkey = pubkey.toString();
        this.online = online;
        this.timestamp = timestamp.toEpochSecond();
    }

    public OnlineStatus toModel() {
        ZonedDateTime zonedDateTime = LocalDateTime.ofEpochSecond(timestamp, 0, UTC).atZone(UTC);
        return new OnlineStatus(online, zonedDateTime);
    }

    public boolean isOnline() {
        return online;
    }

    @CheckForNull
    public String getPubkey() {
        return pubkey;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
