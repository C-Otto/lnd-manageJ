package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ClosedChannel;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.OnChainCosts;
import de.cotto.lndmanagej.transactions.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH_3;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL_2;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL_3;
import static de.cotto.lndmanagej.model.ForceClosedChannelFixtures.FORCE_CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.ForceClosedChannelFixtures.FORCE_CLOSED_CHANNEL_2;
import static de.cotto.lndmanagej.model.ForceClosedChannelFixtures.FORCE_CLOSED_CHANNEL_OPEN_LOCAL;
import static de.cotto.lndmanagej.model.ForceClosedChannelFixtures.FORCE_CLOSED_CHANNEL_OPEN_REMOTE;
import static de.cotto.lndmanagej.model.ForceClosingChannelFixtures.FORCE_CLOSING_CHANNEL;
import static de.cotto.lndmanagej.model.ForceClosingChannelFixtures.FORCE_CLOSING_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_TO_NODE_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.WaitingCloseChannelFixtures.WAITING_CLOSE_CHANNEL;
import static de.cotto.lndmanagej.model.WaitingCloseChannelFixtures.WAITING_CLOSE_CHANNEL_2;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.FEES;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.FEES_2;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.TRANSACTION;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.TRANSACTION_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnChainCostServiceTest {
    private static final Coins OPEN_COSTS = TRANSACTION.fees();

    @InjectMocks
    private OnChainCostService onChainCostService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private ChannelService channelService;

    @Test
    void getOnChainCostsForChannelId() {
        mockOpenTransaction(FORCE_CLOSED_CHANNEL_OPEN_LOCAL);
        mockCloseTransaction(FORCE_CLOSED_CHANNEL_OPEN_LOCAL);
        when(channelService.isClosed(FORCE_CLOSED_CHANNEL_OPEN_LOCAL.getId())).thenReturn(true);
        when(channelService.getLocalChannel(FORCE_CLOSED_CHANNEL_OPEN_LOCAL.getId()))
                .thenReturn(Optional.of(FORCE_CLOSED_CHANNEL_OPEN_LOCAL));
        when(channelService.getClosedChannel(FORCE_CLOSED_CHANNEL_OPEN_LOCAL.getId()))
                .thenReturn(Optional.of(FORCE_CLOSED_CHANNEL_OPEN_LOCAL));
        OnChainCosts expected = new OnChainCosts(
                FEES,
                FEES_2,
                Coins.NONE
        );
        assertThat(onChainCostService.getOnChainCostsForChannelId(CHANNEL_ID)).isEqualTo(expected);
    }

    @Test
    void getOnChainCostsForChannelId_sweep_costs() {
        ChannelId channelId = mockForSweepCosts();
        when(channelService.getLocalChannel(channelId)).thenReturn(Optional.of(FORCE_CLOSED_CHANNEL));

        OnChainCosts expected = new OnChainCosts(
                Coins.NONE,
                Coins.NONE,
                TRANSACTION.fees()
        );
        assertThat(onChainCostService.getOnChainCostsForChannelId(channelId)).isEqualTo(expected);
    }

    @Test
    void getOnChainCostsForChannel() {
        mockOpenTransaction(FORCE_CLOSED_CHANNEL_OPEN_LOCAL);
        mockCloseTransaction(FORCE_CLOSED_CHANNEL_OPEN_LOCAL);
        when(channelService.isClosed(FORCE_CLOSED_CHANNEL_OPEN_LOCAL.getId())).thenReturn(true);
        when(channelService.getClosedChannel(FORCE_CLOSED_CHANNEL_OPEN_LOCAL.getId()))
                .thenReturn(Optional.of(FORCE_CLOSED_CHANNEL_OPEN_LOCAL));
        OnChainCosts expected = new OnChainCosts(
                FEES,
                FEES_2,
                Coins.NONE
        );
        assertThat(onChainCostService.getOnChainCostsForChannel(FORCE_CLOSED_CHANNEL_OPEN_LOCAL)).isEqualTo(expected);
    }

    @Test
    void getOnChainCostsForChannel_sweep_costs() {
        mockForSweepCosts();
        OnChainCosts expected = new OnChainCosts(
                Coins.NONE,
                Coins.NONE,
                TRANSACTION.fees()
        );
        assertThat(onChainCostService.getOnChainCostsForChannel(FORCE_CLOSED_CHANNEL)).isEqualTo(expected);
    }

    @Test
    void getOnChainCostsForPeer() {
        when(channelService.getAllLocalChannels())
                .thenReturn(Stream.of(LOCAL_OPEN_CHANNEL_TO_NODE_3, FORCE_CLOSED_CHANNEL_2))
                .thenReturn(Stream.of(LOCAL_OPEN_CHANNEL_TO_NODE_3, FORCE_CLOSED_CHANNEL_2));
        when(transactionService.getTransaction(LOCAL_OPEN_CHANNEL_TO_NODE_3.getChannelPoint().getTransactionHash()))
                .thenReturn(Optional.of(TRANSACTION));
        when(transactionService.getTransaction(FORCE_CLOSED_CHANNEL_2.getChannelPoint().getTransactionHash()))
                .thenReturn(Optional.of(TRANSACTION_2));
        when(channelService.isClosed(LOCAL_OPEN_CHANNEL_TO_NODE_3.getId())).thenReturn(false);
        when(channelService.isClosed(FORCE_CLOSED_CHANNEL_2.getId())).thenReturn(true);
        when(channelService.getClosedChannel(FORCE_CLOSED_CHANNEL_2.getId()))
                .thenReturn(Optional.of(FORCE_CLOSED_CHANNEL_2));
        when(channelService.getAllChannelsWith(PUBKEY))
                .thenReturn(Set.of(LOCAL_OPEN_CHANNEL_TO_NODE_3, FORCE_CLOSED_CHANNEL_2));
        OnChainCosts expected = new OnChainCosts(
                FEES.add(FEES_2),
                FEES,
                Coins.NONE
        );
        assertThat(onChainCostService.getOnChainCostsForPeer(PUBKEY)).isEqualTo(expected);
    }

    @Nested
    class GetOpenCosts {
        @Test
        void by_channel_id_not_resolved() {
            assertThat(onChainCostService.getOpenCostsForChannelId(CHANNEL_ID)).isEmpty();
        }

        @Test
        void by_channel_id_resolved() {
            mockOpenTransaction(LOCAL_OPEN_CHANNEL);
            when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
            assertThat(onChainCostService.getOpenCostsForChannelId(CHANNEL_ID)).contains(OPEN_COSTS);
        }

        @Test
        void for_local_open_channel_initiator_local_transaction_not_found() {
            assertThat(onChainCostService.getOpenCostsForChannel(LOCAL_OPEN_CHANNEL)).isEmpty();
        }

        @Test
        void for_local_open_channel_initiator_local() {
            mockOpenTransaction(LOCAL_OPEN_CHANNEL);
            assertThat(onChainCostService.getOpenCostsForChannel(LOCAL_OPEN_CHANNEL))
                    .contains(OPEN_COSTS);
        }

        @Test
        void for_local_open_channel_initiator_remote() {
            mockOpenTransaction(LOCAL_OPEN_CHANNEL_2);
            assertThat(onChainCostService.getOpenCostsForChannel(LOCAL_OPEN_CHANNEL_2))
                    .contains(Coins.NONE);
        }

        @Test
        void for_coop_closed_channel_initiator_local() {
            mockOpenTransaction(CLOSED_CHANNEL);
            assertThat(onChainCostService.getOpenCostsForChannel(CLOSED_CHANNEL))
                    .contains(OPEN_COSTS);
        }

        @Test
        void for_coop_closed_channel_initiator_unknown() {
            assertThat(onChainCostService.getOpenCostsForChannel(CLOSED_CHANNEL_2)).isEmpty();
        }

        @Test
        void for_coop_closed_channel_initiator_remote() {
            mockOpenTransaction(CLOSED_CHANNEL_3);
            assertThat(onChainCostService.getOpenCostsForChannel(CLOSED_CHANNEL_3))
                    .contains(Coins.NONE);
        }

        @Test
        void for_force_closed_channel_initiator_local() {
            mockOpenTransaction(FORCE_CLOSED_CHANNEL_OPEN_LOCAL);
            assertThat(onChainCostService.getOpenCostsForChannel(FORCE_CLOSED_CHANNEL_OPEN_LOCAL))
                    .contains(OPEN_COSTS);
        }

        @Test
        void for_force_closed_channel_initiator_remote() {
            mockOpenTransaction(FORCE_CLOSED_CHANNEL_OPEN_REMOTE);
            assertThat(onChainCostService.getOpenCostsForChannel(FORCE_CLOSED_CHANNEL_OPEN_REMOTE))
                    .contains(Coins.NONE);
        }

        @Test
        void for_waiting_close_channel_initiator_local() {
            mockOpenTransaction(WAITING_CLOSE_CHANNEL);
            assertThat(onChainCostService.getOpenCostsForChannel(WAITING_CLOSE_CHANNEL))
                    .contains(OPEN_COSTS);
        }

        @Test
        void for_waiting_close_channel_initiator_remote() {
            mockOpenTransaction(WAITING_CLOSE_CHANNEL_2);
            assertThat(onChainCostService.getOpenCostsForChannel(WAITING_CLOSE_CHANNEL_2))
                    .contains(Coins.NONE);
        }

        @Test
        void for_force_closing_channel_initiator_local() {
            mockOpenTransaction(FORCE_CLOSING_CHANNEL);
            assertThat(onChainCostService.getOpenCostsForChannel(FORCE_CLOSING_CHANNEL))
                    .contains(OPEN_COSTS);
        }

        @Test
        void for_force_closing_channel_initiator_remote() {
            mockOpenTransaction(FORCE_CLOSING_CHANNEL_2);
            assertThat(onChainCostService.getOpenCostsForChannel(FORCE_CLOSING_CHANNEL_2))
                    .contains(Coins.NONE);
        }

        @Test
        void for_transaction_opening_several_channels_divisible() {
            assertThat(OPEN_COSTS.satoshis()).isEqualTo(124);
            mockOpenTransaction(LOCAL_OPEN_CHANNEL);
            when(channelService.getAllLocalChannels()).thenReturn(Stream.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2));
            assertThat(onChainCostService.getOpenCostsForChannel(LOCAL_OPEN_CHANNEL))
                    .contains(Coins.ofSatoshis(62));
        }

        @Test
        void for_transaction_opening_several_channels_not_divisible() {
            assertThat(OPEN_COSTS.satoshis()).isEqualTo(124);
            mockOpenTransaction(LOCAL_OPEN_CHANNEL);
            when(channelService.getAllLocalChannels()).thenReturn(
                    Stream.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2, CLOSED_CHANNEL, CLOSED_CHANNEL_3)
            );
            assertThat(onChainCostService.getOpenCostsForChannel(LOCAL_OPEN_CHANNEL))
                    .contains(Coins.ofSatoshis(41));
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
        void by_channel_id_not_resolved() {
            assertThat(onChainCostService.getCloseCostsForChannelId(CHANNEL_ID)).isEmpty();
        }

        @Test
        void by_channel_id_not_closed() {
            when(channelService.isClosed(CHANNEL_ID)).thenReturn(false);
            assertThat(onChainCostService.getCloseCostsForChannelId(CHANNEL_ID)).contains(Coins.NONE);
        }

        @Test
        void by_channel_id_resolved() {
            mockCloseTransaction(CLOSED_CHANNEL);
            when(channelService.getClosedChannel(CHANNEL_ID)).thenReturn(Optional.of(CLOSED_CHANNEL));
            assertThat(onChainCostService.getCloseCostsForChannelId(CHANNEL_ID)).contains(CLOSE_COSTS);
        }

        @Test
        void for_coop_closed_channel_initiator_local_transaction_not_found() {
            assertThat(onChainCostService.getCloseCostsForChannel(CLOSED_CHANNEL)).isEmpty();
        }

        @Test
        void for_coop_closed_channel_initiator_local() {
            mockCloseTransaction(CLOSED_CHANNEL);
            assertThat(onChainCostService.getCloseCostsForChannel(CLOSED_CHANNEL))
                    .contains(CLOSE_COSTS);
        }

        @Test
        void for_coop_closed_channel_initiator_unknown() {
            assertThat(onChainCostService.getCloseCostsForChannel(CLOSED_CHANNEL_2)).isEmpty();
        }

        @Test
        void for_coop_closed_channel_initiator_remote() {
            mockCloseTransaction(CLOSED_CHANNEL_3);
            assertThat(onChainCostService.getCloseCostsForChannel(CLOSED_CHANNEL_3))
                    .contains(Coins.NONE);
        }

        @Test
        void for_force_closed_channel_initiator_local() {
            mockCloseTransaction(FORCE_CLOSED_CHANNEL_OPEN_LOCAL);
            assertThat(onChainCostService.getCloseCostsForChannel(FORCE_CLOSED_CHANNEL_OPEN_LOCAL))
                    .contains(CLOSE_COSTS);
        }

        @Test
        void for_force_closed_channel_initiator_remote() {
            mockCloseTransaction(FORCE_CLOSED_CHANNEL_OPEN_REMOTE);
            assertThat(onChainCostService.getCloseCostsForChannel(FORCE_CLOSED_CHANNEL_OPEN_REMOTE))
                    .contains(Coins.NONE);
        }
    }

    @Nested
    class GetSweepCosts {
        @BeforeEach
        void setUp() {
            lenient().when(channelService.isForceClosed(CHANNEL_ID)).thenReturn(true);
        }

        @Test
        void by_channel_id_not_resolved() {
            assertThat(onChainCostService.getSweepCostsForChannelId(CHANNEL_ID)).isEmpty();
        }

        @Test
        void by_channel_id_not_force_closed() {
            when(channelService.isForceClosed(CHANNEL_ID)).thenReturn(false);
            assertThat(onChainCostService.getSweepCostsForChannelId(CHANNEL_ID)).contains(Coins.NONE);
        }

        @Test
        void by_channel_id_resolved() {
            when(transactionService.getTransaction(TRANSACTION_HASH_3)).thenReturn(Optional.of(TRANSACTION_2));
            when(channelService.getForceClosedChannel(CHANNEL_ID)).thenReturn(Optional.of(FORCE_CLOSED_CHANNEL));
            assertThat(onChainCostService.getSweepCostsForChannelId(CHANNEL_ID)).contains(TRANSACTION_2.fees());
        }

        @Test
        void transaction_not_found() {
            when(transactionService.getTransaction(TRANSACTION_HASH_3)).thenReturn(Optional.empty());
            assertThat(onChainCostService.getSweepCostsForChannel(FORCE_CLOSED_CHANNEL)).isEqualTo(Coins.NONE);
        }

        @Test
        void no_sweep_transaction() {
            assertThat(onChainCostService.getSweepCostsForChannel(FORCE_CLOSED_CHANNEL_2)).isEqualTo(Coins.NONE);
        }

        @Test
        void with_sweep_transaction() {
            when(transactionService.getTransaction(TRANSACTION_HASH_3)).thenReturn(Optional.of(TRANSACTION_2));
            assertThat(onChainCostService.getSweepCostsForChannel(FORCE_CLOSED_CHANNEL))
                    .isEqualTo(TRANSACTION_2.fees());
        }

        @Test
        void with_sweep_transaction_for_peer() {
            lenient().when(transactionService.getTransaction(TRANSACTION_HASH_3))
                    .thenReturn(Optional.of(TRANSACTION_2));
            assertThat(onChainCostService.getSweepCostsForChannel(FORCE_CLOSED_CHANNEL_2)).isEqualTo(Coins.NONE);
        }
    }

    private void mockOpenTransaction(LocalChannel channel) {
        lenient().when(channelService.getAllLocalChannels()).thenReturn(Stream.of(channel));
        lenient().when(transactionService.getTransaction(channel.getChannelPoint().getTransactionHash()))
                .thenReturn(Optional.of(TRANSACTION));
    }

    private void mockCloseTransaction(ClosedChannel channel) {
        lenient().when(transactionService.getTransaction(channel.getCloseTransactionHash()))
                .thenReturn(Optional.of(TRANSACTION_2));
    }

    private ChannelId mockForSweepCosts() {
        ChannelId channelId = FORCE_CLOSED_CHANNEL.getId();
        when(transactionService.getTransaction(any())).thenReturn(Optional.empty());
        when(transactionService.getTransaction(TRANSACTION_HASH_3)).thenReturn(Optional.of(TRANSACTION));
        when(channelService.isForceClosed(channelId)).thenReturn(true);
        when(channelService.getForceClosedChannel(channelId)).thenReturn(Optional.of(FORCE_CLOSED_CHANNEL));
        return channelId;
    }
}