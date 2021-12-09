package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.SelfPayment;
import de.cotto.lndmanagej.selfpayments.SelfPaymentsDao;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SelfPaymentsService {
    private final SelfPaymentsDao dao;

    public SelfPaymentsService(SelfPaymentsDao dao) {
        this.dao = dao;
    }

    public List<SelfPayment> getSelfPaymentsToChannel(ChannelId channelId) {
        return dao.getSelfPaymentsToChannel(channelId);
    }

    public List<SelfPayment> getSelfPaymentsFromChannel(ChannelId channelId) {
        return dao.getSelfPaymentsFromChannel(channelId).stream().distinct().toList();
    }
}
