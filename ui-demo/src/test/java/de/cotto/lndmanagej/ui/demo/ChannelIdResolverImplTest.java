package de.cotto.lndmanagej.ui.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ChannelIdResolverImplTest {

    @InjectMocks
    ChannelIdResolverImpl channelIdResolver;

    @Test
    void resolveFromChannelPoint_alwaysEmpty() {
        assertNotNull(channelIdResolver.resolveFromChannelPoint(CHANNEL_POINT));
        assertTrue(channelIdResolver.resolveFromChannelPoint(CHANNEL_POINT).isEmpty());
    }
}