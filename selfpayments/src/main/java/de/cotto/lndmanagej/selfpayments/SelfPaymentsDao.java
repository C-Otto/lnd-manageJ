package de.cotto.lndmanagej.selfpayments;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.SelfPayment;

import java.time.Duration;
import java.util.List;

public interface SelfPaymentsDao {
    List<SelfPayment> getAllSelfPayments();

    List<SelfPayment> getSelfPaymentsToChannel(ChannelId channelId, Duration maxAge);

    List<SelfPayment> getSelfPaymentsFromChannel(ChannelId channelId, Duration maxAge);
}
