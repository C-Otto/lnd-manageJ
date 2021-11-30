package de.cotto.lndmanagej.statistics.persistence;

import de.cotto.lndmanagej.model.ForwardingEvent;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.ZoneOffset;

@Entity
@Table(name = "forwarding_events")
class ForwardingEventJpaDto {
    @Id
    private int index;

    private long amountIn;
    private long amountOut;
    private long channelIn;
    private long channelOut;
    private long timestamp;

    public ForwardingEventJpaDto() {
        // for JPA
    }

    public static ForwardingEventJpaDto createFromForwardingEvent(ForwardingEvent forwardingEvent) {
        ForwardingEventJpaDto jpaDto = new ForwardingEventJpaDto();
        jpaDto.index = forwardingEvent.index();
        jpaDto.amountIn = forwardingEvent.amountIn().milliSatoshis();
        jpaDto.amountOut = forwardingEvent.amountOut().milliSatoshis();
        jpaDto.channelIn = forwardingEvent.channelIn().getShortChannelId();
        jpaDto.channelOut = forwardingEvent.channelOut().getShortChannelId();
        jpaDto.timestamp = forwardingEvent.timestamp().toInstant(ZoneOffset.UTC).toEpochMilli();
        return jpaDto;
    }

    public int getIndex() {
        return index;
    }

    public long getAmountIn() {
        return amountIn;
    }

    public long getAmountOut() {
        return amountOut;
    }

    public long getChannelIn() {
        return channelIn;
    }

    public long getChannelOut() {
        return channelOut;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
