package de.cotto.lndmanagej.invoices.persistence;

import javax.persistence.Embeddable;

@Embeddable
public class ReceivedViaJpaDto {
    private long channelId;
    private long amount;

    @SuppressWarnings("unused")
    public ReceivedViaJpaDto() {
        // for JPA
    }

    public ReceivedViaJpaDto(long channelId, long amount) {
        this.channelId = channelId;
        this.amount = amount;
    }

    public long getChannelId() {
        return channelId;
    }

    public long getAmount() {
        return amount;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
}
