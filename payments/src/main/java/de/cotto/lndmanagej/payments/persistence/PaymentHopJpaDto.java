package de.cotto.lndmanagej.payments.persistence;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.PaymentHop;

import javax.persistence.Embeddable;

@Embeddable
public class PaymentHopJpaDto {
    private long shortChannelId;
    private long amount;

    @SuppressWarnings("unused")
    public PaymentHopJpaDto() {
        // for JPA
    }

    public PaymentHopJpaDto(long shortChannelId, long amount) {
        this.shortChannelId = shortChannelId;
        this.amount = amount;
    }

    public static PaymentHopJpaDto createFromModel(PaymentHop paymentHop) {
        return new PaymentHopJpaDto(paymentHop.channelId().getShortChannelId(), paymentHop.amount().milliSatoshis());
    }

    public PaymentHop toModel() {
        return new PaymentHop(ChannelId.fromShortChannelId(shortChannelId), Coins.ofMilliSatoshis(amount));
    }
}
