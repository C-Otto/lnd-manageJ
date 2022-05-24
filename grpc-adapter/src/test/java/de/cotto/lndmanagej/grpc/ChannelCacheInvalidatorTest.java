package de.cotto.lndmanagej.grpc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_4;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChannelCacheInvalidatorTest {
    @InjectMocks
    private ChannelCacheInvalidator channelCacheInvalidator;

    @Mock
    private GrpcService grpcService;

    @Test
    void invalidatesChannelCache() {
        channelCacheInvalidator.amountChanged(PUBKEY_4);
        verify(grpcService).invalidateChannelsCache();
    }
}
