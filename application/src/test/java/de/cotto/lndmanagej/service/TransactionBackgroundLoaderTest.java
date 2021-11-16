package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.LocalOpenChannel;
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
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
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
    void update_all_known() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2));
        when(transactionService.isUnknown(any())).thenReturn(false);
        transactionBackgroundLoader.loadTransactionForOneChannel();
        verify(transactionService, never()).getTransaction(any());
    }

    @Test
    void update_all_unknown() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2));
        when(transactionService.isUnknown(any())).thenReturn(true);
        transactionBackgroundLoader.loadTransactionForOneChannel();
        verify(transactionService, times(1)).getTransaction(any());
    }

    @Test
    void update_one_unknown() {
        LocalOpenChannel channel1 =
                new LocalOpenChannel(CHANNEL_ID, CHANNEL_POINT, CAPACITY, PUBKEY, PUBKEY_2, BALANCE_INFORMATION);
        LocalOpenChannel channel2 =
                new LocalOpenChannel(CHANNEL_ID_2, CHANNEL_POINT_2, CAPACITY, PUBKEY, PUBKEY_2, BALANCE_INFORMATION);
        LocalOpenChannel channel3 =
                new LocalOpenChannel(CHANNEL_ID_3, CHANNEL_POINT_3, CAPACITY, PUBKEY, PUBKEY_2, BALANCE_INFORMATION);
        when(channelService.getOpenChannels()).thenReturn(Set.of(channel1, channel2, channel3));
        String unknownHash = CHANNEL_POINT_3.getTransactionHash();
        when(transactionService.isUnknown(unknownHash)).thenReturn(true);

        transactionBackgroundLoader.loadTransactionForOneChannel();

        verify(transactionService, times(1)).getTransaction(unknownHash);
    }
}