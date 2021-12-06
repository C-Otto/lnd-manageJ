package de.cotto.lndmanagej.payments.persistence;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.PaymentHop;

import javax.persistence.Embeddable;

@Embeddable
public class PaymentHopJpaDto {
    private long channelId;
    private long amount;

    @SuppressWarnings("unused")
    public PaymentHopJpaDto() {
        // for JPA
    }

    public PaymentHopJpaDto(long channelId, long amount) {
        this.channelId = channelId;
        this.amount = amount;
    }

    public static PaymentHopJpaDto createFromModel(PaymentHop paymentHop) {
        return new PaymentHopJpaDto(paymentHop.channelId().getShortChannelId(), paymentHop.amount().milliSatoshis());
    }

    public PaymentHop toModel() {
        return new PaymentHop(ChannelId.fromShortChannelId(channelId), Coins.ofMilliSatoshis(amount));
    }
}
