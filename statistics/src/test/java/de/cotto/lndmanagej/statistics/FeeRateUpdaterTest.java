package de.cotto.lndmanagej.statistics;

import de.cotto.lndmanagej.feerates.FeeRates;
import de.cotto.lndmanagej.feerates.FeeRatesDao;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.FeeRateInformation;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.PoliciesForLocalChannel;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.PolicyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES_FOR_LOCAL_CHANNEL;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES_WITH_NEGATIVE_INBOUND_FEES;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeeRateUpdaterTest {
    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.now(ZoneOffset.UTC);
    @InjectMocks
    private FeeRateUpdater feeRateUpdater;

    @Mock
    private ChannelService channelService;

    @Mock
    private FeeRatesDao dao;

    @Mock
    private PolicyService policyService;

    @BeforeEach
    void setUp() {
        lenient().when(policyService.getPolicies(LOCAL_OPEN_CHANNEL)).thenReturn(POLICIES_FOR_LOCAL_CHANNEL);
        lenient().when(policyService.getPolicies(LOCAL_OPEN_CHANNEL_2)).thenReturn(POLICIES_WITH_NEGATIVE_INBOUND_FEES);
    }

    @Test
    void storeFeeRates_nothing_stored() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(
                LOCAL_OPEN_CHANNEL,
                LOCAL_OPEN_CHANNEL_2
        ));
        feeRateUpdater.storeFeeRates();
        verify(dao).saveFeeRates(argThat(withFeeRates(POLICIES_FOR_LOCAL_CHANNEL)));
        verify(dao).saveFeeRates(argThat(withChannelId(LOCAL_OPEN_CHANNEL_2)));
        verify(dao).saveFeeRates(argThat(withFeeRates(POLICIES_WITH_NEGATIVE_INBOUND_FEES)));
        verify(dao).saveFeeRates(argThat(withChannelId(LOCAL_OPEN_CHANNEL)));
        verify(dao, times(2)).saveFeeRates(any());
    }

    @Test
    void storeFeeRates_persists_changed_data() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL));
        ChannelId channelId = LOCAL_OPEN_CHANNEL.getId();
        FeeRates feeRates = new FeeRates(
                LOCAL_DATE_TIME,
                channelId,
                FeeRateInformation.fromPolicies(POLICIES_WITH_NEGATIVE_INBOUND_FEES)
        );
        when(dao.getMostRecentFeeRates(channelId)).thenReturn(Optional.of(feeRates));
        feeRateUpdater.storeFeeRates();
        verify(dao).saveFeeRates(any());
    }

    @Test
    void storeFeeRates_does_not_persist_unchanged_data() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL));
        ChannelId channelId = LOCAL_OPEN_CHANNEL.getId();
        FeeRates feeRates = new FeeRates(
                LOCAL_DATE_TIME,
                channelId,
                FeeRateInformation.fromPolicies(POLICIES_FOR_LOCAL_CHANNEL)
        );
        when(dao.getMostRecentFeeRates(channelId)).thenReturn(Optional.of(feeRates));
        feeRateUpdater.storeFeeRates();
        verify(dao, never()).saveFeeRates(any());
    }

    private ArgumentMatcher<FeeRates> withChannelId(LocalOpenChannel channel) {
        return statistics -> statistics.channelId().equals(channel.getId());
    }

    private ArgumentMatcher<FeeRates> withFeeRates(PoliciesForLocalChannel policies) {
        return statistics -> statistics.feeRates().equals(FeeRateInformation.fromPolicies(policies));
    }
}
