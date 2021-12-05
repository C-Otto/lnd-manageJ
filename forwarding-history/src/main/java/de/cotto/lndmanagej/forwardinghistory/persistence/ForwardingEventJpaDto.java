package de.cotto.lndmanagej.forwardinghistory.persistence;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.ForwardingEvent;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(
        name = "forwarding_events",
        indexes = {@Index(columnList = "channelIncoming"), @Index(columnList = "channelOutgoing")}
)
class ForwardingEventJpaDto {
    @Id
    private int eventIndex;

    private long amountIncoming;
    private long amountOutgoing;
    private long channelIncoming;
    private long channelOutgoing;
    private long timestamp;

    public ForwardingEventJpaDto() {
        // for JPA
    }

    public static ForwardingEventJpaDto createFromModel(ForwardingEvent forwardingEvent) {
        ForwardingEventJpaDto jpaDto = new ForwardingEventJpaDto();
        jpaDto.eventIndex = forwardingEvent.index();
        jpaDto.amountIncoming = forwardingEvent.amountIn().milliSatoshis();
        jpaDto.amountOutgoing = forwardingEvent.amountOut().milliSatoshis();
        jpaDto.channelIncoming = forwardingEvent.channelIn().getShortChannelId();
        jpaDto.channelOutgoing = forwardingEvent.channelOut().getShortChannelId();
        jpaDto.timestamp = forwardingEvent.timestamp().toInstant(ZoneOffset.UTC).toEpochMilli();
        return jpaDto;
    }

    public ForwardingEvent toModel() {
        long epochSecond = timestamp / 1_000;
        int milliseconds = (int) (timestamp % 1_000);
        int nanoseconds = milliseconds * 1_000_000;
        return new ForwardingEvent(
                eventIndex,
                Coins.ofMilliSatoshis(amountIncoming),
                Coins.ofMilliSatoshis(amountOutgoing),
                ChannelId.fromShortChannelId(channelIncoming),
                ChannelId.fromShortChannelId(channelOutgoing),
                LocalDateTime.ofEpochSecond(epochSecond, nanoseconds, ZoneOffset.UTC)
        );
    }

    public int getIndex() {
        return eventIndex;
    }

    public long getAmountIncoming() {
        return amountIncoming;
    }

    public long getAmountOutgoing() {
        return amountOutgoing;
    }

    public long getChannelIncoming() {
        return channelIncoming;
    }

    public long getChannelOutgoing() {
        return channelOutgoing;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
