package de.cotto.lndmanagej.payments.persistence;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.PaymentHop;
import jakarta.persistence.Embeddable;

@Embeddable
public class PaymentHopJpaDto {
    private long channelId;
    private long amount;
    private boolean first;

    @SuppressWarnings("unused")
    public PaymentHopJpaDto() {
        // for JPA
    }

    PaymentHopJpaDto(long channelId, long amount, boolean first) {
        this.channelId = channelId;
        this.amount = amount;
        this.first = first;
    }

    static PaymentHopJpaDto createFromModel(PaymentHop paymentHop) {
        return new PaymentHopJpaDto(
                paymentHop.channelId().getShortChannelId(),
                paymentHop.amount().milliSatoshis(),
                paymentHop.first()
        );
    }

    PaymentHop toModel() {
        return new PaymentHop(ChannelId.fromShortChannelId(channelId), Coins.ofMilliSatoshis(amount), first);
    }
}
