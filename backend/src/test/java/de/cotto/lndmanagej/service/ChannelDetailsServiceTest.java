package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelDetails;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.Policies;
import de.cotto.lndmanagej.model.RebalanceReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelDetailsFixtures.CHANNEL_DETAILS;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.FeeReportFixtures.FEE_REPORT;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_PRIVATE;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_2;
import static de.cotto.lndmanagej.model.OnChainCostsFixtures.ON_CHAIN_COSTS;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.RebalanceReportFixtures.REBALANCE_REPORT;
import static de.cotto.lndmanagej.model.WaitingCloseChannelFixtures.WAITING_CLOSE_CHANNEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelDetailsServiceTest {
    @InjectMocks
    private ChannelDetailsService channelDetailsService;

    @Mock
    private OnChainCostService onChainCostService;

    @Mock
    private RebalanceService rebalanceService;

    @Mock
    private NodeService nodeService;

    @Mock
    private BalanceService balanceService;

    @Mock
    private FeeService feeService;

    @Mock
    private PolicyService policyService;

    @BeforeEach
    void setUp() {
        lenient().when(onChainCostService.getOnChainCostsForChannelId(CHANNEL_ID)).thenReturn(ON_CHAIN_COSTS);
        lenient().when(rebalanceService.getReportForChannel(CHANNEL_ID)).thenReturn(RebalanceReport.EMPTY);
        lenient().when(feeService.getFeeReportForChannel(CHANNEL_ID)).thenReturn(FEE_REPORT);
        lenient().when(policyService.getPolicies(CHANNEL_ID)).thenReturn(POLICIES);
    }

    @Test
    void getDetails() {
        when(nodeService.getAlias(PUBKEY_2)).thenReturn(ALIAS);
        when(balanceService.getBalanceInformation(CHANNEL_ID))
                .thenReturn(Optional.ofNullable(LOCAL_OPEN_CHANNEL_PRIVATE.getBalanceInformation()));
        when(rebalanceService.getReportForChannel(CHANNEL_ID)).thenReturn(REBALANCE_REPORT);
        assertThat(channelDetailsService.getDetails(LOCAL_OPEN_CHANNEL_PRIVATE)).isEqualTo(CHANNEL_DETAILS);
    }

    @Test
    void getDetails_private() {
        ChannelDetails expectedDetails = new ChannelDetails(
                LOCAL_OPEN_CHANNEL_PRIVATE,
                ALIAS_2,
                LOCAL_OPEN_CHANNEL_PRIVATE.getBalanceInformation(),
                ON_CHAIN_COSTS,
                POLICIES,
                FEE_REPORT,
                RebalanceReport.EMPTY
        );
        when(nodeService.getAlias(PUBKEY_2)).thenReturn(ALIAS_2);
        when(balanceService.getBalanceInformation(CHANNEL_ID))
                .thenReturn(Optional.ofNullable(LOCAL_OPEN_CHANNEL_PRIVATE.getBalanceInformation()));

        assertThat(channelDetailsService.getDetails(LOCAL_OPEN_CHANNEL_PRIVATE)).isEqualTo(expectedDetails);
    }

    @Test
    void getDetails_closed() {
        ChannelDetails expectedDetails = mockForChannelWithoutPolicies(CLOSED_CHANNEL);
        assertThat(channelDetailsService.getDetails(CLOSED_CHANNEL)).isEqualTo(expectedDetails);
    }

    @Test
    void getDetails_waiting_close() {
        ChannelDetails expectedDetails = mockForChannelWithoutPolicies(WAITING_CLOSE_CHANNEL);
        assertThat(channelDetailsService.getDetails(WAITING_CLOSE_CHANNEL)).isEqualTo(expectedDetails);
    }

    private ChannelDetails mockForChannelWithoutPolicies(LocalChannel channel) {
        when(nodeService.getAlias(PUBKEY_2)).thenReturn(ALIAS_2);
        when(balanceService.getBalanceInformation(CHANNEL_ID)).thenReturn(Optional.empty());
        return new ChannelDetails(
                channel,
                ALIAS_2,
                BalanceInformation.EMPTY,
                ON_CHAIN_COSTS,
                Policies.UNKNOWN,
                FEE_REPORT,
                RebalanceReport.EMPTY
        );
    }
}