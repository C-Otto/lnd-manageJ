package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.ClosedChannel;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.transactions.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.stream.Stream;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL_2;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL_3;
import static de.cotto.lndmanagej.model.ForceClosedChannelFixtures.FORCE_CLOSED_CHANNEL_OPEN_LOCAL;
import static de.cotto.lndmanagej.model.ForceClosedChannelFixtures.FORCE_CLOSED_CHANNEL_OPEN_REMOTE;
import static de.cotto.lndmanagej.model.ForceClosingChannelFixtures.FORCE_CLOSING_CHANNEL;
import static de.cotto.lndmanagej.model.ForceClosingChannelFixtures.FORCE_CLOSING_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.WaitingCloseChannelFixtures.WAITING_CLOSE_CHANNEL;
import static de.cotto.lndmanagej.model.WaitingCloseChannelFixtures.WAITING_CLOSE_CHANNEL_2;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.TRANSACTION;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.TRANSACTION_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnChainCostServiceTest {
    @InjectMocks
    private OnChainCostService onChainCostService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private ChannelService channelService;

    @Nested
    class GetOpenCosts {
        private static final Coins OPEN_COSTS = TRANSACTION.fees();

        @Test
        void getOpenCosts_by_channel_id_not_resolved() {
            assertThat(onChainCostService.getOpenCosts(CHANNEL_ID)).isEmpty();
        }

        @Test
        void getOpenCosts_by_channel_id_resolved() {
            mockOpenTransaction(LOCAL_OPEN_CHANNEL);
            when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
            assertThat(onChainCostService.getOpenCosts(CHANNEL_ID)).contains(OPEN_COSTS);
        }

        @Test
        void getOpenCosts_for_local_open_channel_initiator_local_transaction_not_found() {
            assertThat(onChainCostService.getOpenCosts(LOCAL_OPEN_CHANNEL)).isEmpty();
        }

        @Test
        void getOpenCosts_for_local_open_channel_initiator_local() {
            mockOpenTransaction(LOCAL_OPEN_CHANNEL);
            assertThat(onChainCostService.getOpenCosts(LOCAL_OPEN_CHANNEL))
                    .contains(OPEN_COSTS);
        }

        @Test
        void getOpenCosts_for_local_open_channel_initiator_remote() {
            mockOpenTransaction(LOCAL_OPEN_CHANNEL_2);
            assertThat(onChainCostService.getOpenCosts(LOCAL_OPEN_CHANNEL_2))
                    .contains(Coins.NONE);
        }

        @Test
        void getOpenCosts_for_coop_closed_channel_initiator_local() {
            mockOpenTransaction(CLOSED_CHANNEL);
            assertThat(onChainCostService.getOpenCosts(CLOSED_CHANNEL))
                    .contains(OPEN_COSTS);
        }

        @Test
        void getOpenCosts_for_coop_closed_channel_initiator_unknown() {
            assertThat(onChainCostService.getOpenCosts(CLOSED_CHANNEL_2)).isEmpty();
        }

        @Test
        void getOpenCosts_for_coop_closed_channel_initiator_remote() {
            mockOpenTransaction(CLOSED_CHANNEL_3);
            assertThat(onChainCostService.getOpenCosts(CLOSED_CHANNEL_3))
                    .contains(Coins.NONE);
        }

        @Test
        void getOpenCosts_for_force_closed_channel_initiator_local() {
            mockOpenTransaction(FORCE_CLOSED_CHANNEL_OPEN_LOCAL);
            assertThat(onChainCostService.getOpenCosts(FORCE_CLOSED_CHANNEL_OPEN_LOCAL))
                    .contains(OPEN_COSTS);
        }

        @Test
        void getOpenCosts_for_force_closed_channel_initiator_remote() {
            mockOpenTransaction(FORCE_CLOSED_CHANNEL_OPEN_REMOTE);
            assertThat(onChainCostService.getOpenCosts(FORCE_CLOSED_CHANNEL_OPEN_REMOTE))
                    .contains(Coins.NONE);
        }

        @Test
        void getOpenCosts_for_waiting_close_channel_initiator_local() {
            mockOpenTransaction(WAITING_CLOSE_CHANNEL);
            assertThat(onChainCostService.getOpenCosts(WAITING_CLOSE_CHANNEL))
                    .contains(OPEN_COSTS);
        }

        @Test
        void getOpenCosts_for_waiting_close_channel_initiator_remote() {
            mockOpenTransaction(WAITING_CLOSE_CHANNEL_2);
            assertThat(onChainCostService.getOpenCosts(WAITING_CLOSE_CHANNEL_2))
                    .contains(Coins.NONE);
        }

        @Test
        void getOpenCosts_for_force_closing_channel_initiator_local() {
            mockOpenTransaction(FORCE_CLOSING_CHANNEL);
            assertThat(onChainCostService.getOpenCosts(FORCE_CLOSING_CHANNEL))
                    .contains(OPEN_COSTS);
        }

        @Test
        void getOpenCosts_for_force_closing_channel_initiator_remote() {
            mockOpenTransaction(FORCE_CLOSING_CHANNEL_2);
            assertThat(onChainCostService.getOpenCosts(FORCE_CLOSING_CHANNEL_2))
                    .contains(Coins.NONE);
        }

        @Test
        void getOpenCosts_for_transaction_opening_several_channels_divisible() {
            assertThat(OPEN_COSTS.satoshis()).isEqualTo(124);
            mockOpenTransaction(LOCAL_OPEN_CHANNEL);
            when(channelService.getAllLocalChannels()).thenReturn(Stream.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2));
            assertThat(onChainCostService.getOpenCosts(LOCAL_OPEN_CHANNEL))
                    .contains(Coins.ofSatoshis(62));
        }

        @Test
        void getOpenCosts_for_transaction_opening_several_channels_not_divisible() {
            assertThat(OPEN_COSTS.satoshis()).isEqualTo(124);
            mockOpenTransaction(LOCAL_OPEN_CHANNEL);
            when(channelService.getAllLocalChannels()).thenReturn(
                    Stream.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2, CLOSED_CHANNEL, CLOSED_CHANNEL_3)
            );
            assertThat(onChainCostService.getOpenCosts(LOCAL_OPEN_CHANNEL))
                    .contains(Coins.ofSatoshis(41));
        }

        private void mockOpenTransaction(LocalChannel channel) {
            lenient().when(channelService.getAllLocalChannels()).thenReturn(Stream.of(channel));
            lenient().when(transactionService.getTransaction(channel.getChannelPoint().getTransactionHash()))
                    .thenReturn(Optional.of(TRANSACTION));
        }
    }

    @Nested
    class GetCloseCosts {
        private static final Coins CLOSE_COSTS = TRANSACTION_2.fees();

        @BeforeEach
        void setUp() {
            lenient().when(channelService.isClosed(CHANNEL_ID)).thenReturn(true);
        }

        @Test
        void getCloseCosts_by_channel_id_not_resolved() {
            assertThat(onChainCostService.getCloseCosts(CHANNEL_ID)).isEmpty();
        }

        @Test
        void getCloseCosts_by_channel_id_not_closed() {
            when(channelService.isClosed(CHANNEL_ID)).thenReturn(false);
            assertThat(onChainCostService.getCloseCosts(CHANNEL_ID)).contains(Coins.NONE);
        }

        @Test
        void getCloseCosts_by_channel_id_resolved() {
            mockCloseTransaction(CLOSED_CHANNEL);
            when(channelService.getClosedChannel(CHANNEL_ID)).thenReturn(Optional.of(CLOSED_CHANNEL));
            assertThat(onChainCostService.getCloseCosts(CHANNEL_ID)).contains(CLOSE_COSTS);
        }

        @Test
        void getOpenCosts_for_coop_closed_channel_initiator_local_transaction_not_found() {
            assertThat(onChainCostService.getCloseCosts(CLOSED_CHANNEL)).isEmpty();
        }

        @Test
        void getOpenCosts_for_coop_closed_channel_initiator_local() {
            mockCloseTransaction(CLOSED_CHANNEL);
            assertThat(onChainCostService.getCloseCosts(CLOSED_CHANNEL))
                    .contains(CLOSE_COSTS);
        }

        @Test
        void getOpenCosts_for_coop_closed_channel_initiator_unknown() {
            assertThat(onChainCostService.getCloseCosts(CLOSED_CHANNEL_2)).isEmpty();
        }

        @Test
        void getOpenCosts_for_coop_closed_channel_initiator_remote() {
            mockCloseTransaction(CLOSED_CHANNEL_3);
            assertThat(onChainCostService.getCloseCosts(CLOSED_CHANNEL_3))
                    .contains(Coins.NONE);
        }

        @Test
        void getOpenCosts_for_force_closed_channel_initiator_local() {
            mockCloseTransaction(FORCE_CLOSED_CHANNEL_OPEN_LOCAL);
            assertThat(onChainCostService.getCloseCosts(FORCE_CLOSED_CHANNEL_OPEN_LOCAL))
                    .contains(CLOSE_COSTS);
        }

        @Test
        void getOpenCosts_for_force_closed_channel_initiator_remote() {
            mockCloseTransaction(FORCE_CLOSED_CHANNEL_OPEN_REMOTE);
            assertThat(onChainCostService.getCloseCosts(FORCE_CLOSED_CHANNEL_OPEN_REMOTE))
                    .contains(Coins.NONE);
        }

        private void mockCloseTransaction(ClosedChannel channel) {
            lenient().when(transactionService.getTransaction(channel.getCloseTransactionHash()))
                    .thenReturn(Optional.of(TRANSACTION_2));
        }
    }
}