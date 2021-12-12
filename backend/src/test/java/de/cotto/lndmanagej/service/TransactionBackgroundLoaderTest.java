package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.ChannelCoreInformation;
import de.cotto.lndmanagej.model.ChannelPoint;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.TransactionHash;
import de.cotto.lndmanagej.transactions.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT_2;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT_3;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH_2;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH_3;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.ForceClosedChannelFixtures.FORCE_CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.ForceClosedChannelFixtures.FORCE_CLOSED_CHANNEL_2;
import static de.cotto.lndmanagej.model.ForceClosingChannelFixtures.FORCE_CLOSING_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.TOTAL_RECEIVED;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.TOTAL_SENT;
import static de.cotto.lndmanagej.model.OpenInitiator.LOCAL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.WaitingCloseChannelFixtures.WAITING_CLOSE_CHANNEL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionBackgroundLoaderTest {
    @InjectMocks
    private TransactionBackgroundLoader transactionBackgroundLoader;

    @Mock
    private TransactionService transactionService;

    @Mock
    private ChannelService channelService;

    @Test
    void update_no_channel() {
        transactionBackgroundLoader.loadTransactionForOneChannel();
        verify(transactionService, never()).getTransaction(any());
    }

    @Test
    void update_from_open_channels_all_known() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2));
        when(transactionService.isUnknown(any())).thenReturn(false);
        transactionBackgroundLoader.loadTransactionForOneChannel();
        verify(transactionService, never()).getTransaction(any());
    }

    @Test
    void update_from_open_channels_all_unknown() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2));
        when(transactionService.isUnknown(any())).thenReturn(true);
        transactionBackgroundLoader.loadTransactionForOneChannel();
        verify(transactionService, times(1)).getTransaction(any());
    }

    @Test
    void update_from_closed_channels() {
        TransactionHash transactionHash = CLOSED_CHANNEL.getChannelPoint().getTransactionHash();
        when(channelService.getClosedChannels()).thenReturn(Set.of(CLOSED_CHANNEL));
        when(transactionService.isUnknown(transactionHash)).thenReturn(true);
        transactionBackgroundLoader.loadTransactionForOneChannel();
        verify(transactionService).getTransaction(transactionHash);
    }

    @Test
    void update_from_closed_channels_close_transaction() {
        TransactionHash closeTransactionHash = CLOSED_CHANNEL.getCloseTransactionHash();
        when(channelService.getClosedChannels()).thenReturn(Set.of(CLOSED_CHANNEL));
        when(transactionService.isUnknown(CLOSED_CHANNEL.getChannelPoint().getTransactionHash())).thenReturn(false);
        when(transactionService.isUnknown(closeTransactionHash)).thenReturn(true);
        transactionBackgroundLoader.loadTransactionForOneChannel();
        verify(transactionService).getTransaction(closeTransactionHash);
    }

    @Test
    void update_from_waiting_close_channels() {
        TransactionHash transactionHash = WAITING_CLOSE_CHANNEL.getChannelPoint().getTransactionHash();
        when(channelService.getWaitingCloseChannels()).thenReturn(Set.of(WAITING_CLOSE_CHANNEL));
        when(transactionService.isUnknown(transactionHash)).thenReturn(true);
        transactionBackgroundLoader.loadTransactionForOneChannel();
        verify(transactionService).getTransaction(transactionHash);
    }

    @Test
    void update_from_force_closing_channels() {
        TransactionHash transactionHash = FORCE_CLOSING_CHANNEL.getChannelPoint().getTransactionHash();
        when(transactionService.isUnknown(transactionHash)).thenReturn(true);

        when(channelService.getForceClosingChannels()).thenReturn(Set.of(FORCE_CLOSING_CHANNEL));
        transactionBackgroundLoader.loadTransactionForOneChannel();
        verify(transactionService).getTransaction(transactionHash);
    }

    @Test
    void update_from_force_closing_channels_close_transaction() {
        TransactionHash closeTransactionHash = FORCE_CLOSING_CHANNEL.getCloseTransactionHash();
        when(transactionService.isUnknown(closeTransactionHash)).thenReturn(true);

        TransactionHash openTransactionHash = FORCE_CLOSING_CHANNEL.getChannelPoint().getTransactionHash();
        when(transactionService.isUnknown(openTransactionHash)).thenReturn(false);

        when(channelService.getForceClosingChannels()).thenReturn(Set.of(FORCE_CLOSING_CHANNEL));
        transactionBackgroundLoader.loadTransactionForOneChannel();
        verify(transactionService).getTransaction(closeTransactionHash);
    }

    @Test
    void update_from_force_closing_channels_ignores_pending_htlc_output() {
        TransactionHash htlcOutpointHash = FORCE_CLOSING_CHANNEL.getHtlcOutpoints().stream()
                .map(ChannelPoint::getTransactionHash)
                .findFirst()
                .orElseThrow();

        TransactionHash openTransactionHash = FORCE_CLOSING_CHANNEL.getChannelPoint().getTransactionHash();
        when(transactionService.isUnknown(openTransactionHash)).thenReturn(false);
        when(transactionService.isUnknown(FORCE_CLOSING_CHANNEL.getCloseTransactionHash())).thenReturn(false);

        when(channelService.getForceClosingChannels()).thenReturn(Set.of(FORCE_CLOSING_CHANNEL));
        transactionBackgroundLoader.loadTransactionForOneChannel();
        verify(transactionService, never()).getTransaction(htlcOutpointHash);
    }

    @Test
    void update_from_force_closed_channels_sweep_transaction() {
        when(transactionService.isUnknown(TRANSACTION_HASH)).thenReturn(false);
        when(transactionService.isUnknown(TRANSACTION_HASH_2)).thenReturn(false);
        when(transactionService.isUnknown(TRANSACTION_HASH_3)).thenReturn(true);
        when(channelService.getClosedChannels()).thenReturn(Set.of(FORCE_CLOSED_CHANNEL));
        transactionBackgroundLoader.loadTransactionForOneChannel();
        verify(transactionService).getTransaction(TRANSACTION_HASH_3);
    }

    @Test
    void update_from_force_closed_channels_sweep_transaction_ignores_peer_sweeps() {
        when(transactionService.isUnknown(TRANSACTION_HASH)).thenReturn(false);
        when(transactionService.isUnknown(TRANSACTION_HASH_2)).thenReturn(false);
        when(channelService.getClosedChannels()).thenReturn(Set.of(FORCE_CLOSED_CHANNEL_2));
        transactionBackgroundLoader.loadTransactionForOneChannel();
        verify(transactionService, never()).getTransaction(any());
    }

    @Test
    void update_one_unknown() {
        LocalOpenChannel channel1 = new LocalOpenChannel(
                new ChannelCoreInformation(CHANNEL_ID, CHANNEL_POINT, CAPACITY),
                PUBKEY,
                PUBKEY_2,
                BALANCE_INFORMATION,
                LOCAL,
                TOTAL_SENT,
                TOTAL_RECEIVED,
                false,
                true
        );
        LocalOpenChannel channel2 = new LocalOpenChannel(
                new ChannelCoreInformation(CHANNEL_ID_2, CHANNEL_POINT_2, CAPACITY),
                PUBKEY,
                PUBKEY_2,
                BALANCE_INFORMATION,
                LOCAL,
                TOTAL_SENT,
                TOTAL_RECEIVED,
                false,
                true
        );
        LocalOpenChannel channel3 = new LocalOpenChannel(
                new ChannelCoreInformation(CHANNEL_ID_3, CHANNEL_POINT_3, CAPACITY),
                PUBKEY,
                PUBKEY_2,
                BALANCE_INFORMATION,
                LOCAL,
                TOTAL_SENT,
                TOTAL_RECEIVED,
                false,
                true
        );
        when(channelService.getOpenChannels()).thenReturn(Set.of(channel1, channel2, channel3));
        TransactionHash unknownHash = CHANNEL_POINT_3.getTransactionHash();
        when(transactionService.isUnknown(any())).thenReturn(false);
        when(transactionService.isUnknown(unknownHash)).thenReturn(true);

        transactionBackgroundLoader.loadTransactionForOneChannel();

        verify(transactionService).getTransaction(unknownHash);
    }
}