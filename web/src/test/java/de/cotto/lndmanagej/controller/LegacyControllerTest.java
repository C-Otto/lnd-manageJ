package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.PolicyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_COMPACT;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_COMPACT_4;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_TO_NODE_3;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_2;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_3;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_1;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LegacyControllerTest {
    @InjectMocks
    private LegacyController legacyController;

    @Mock
    private NodeService nodeService;

    @Mock
    private ChannelService channelService;

    @Mock
    private PolicyService policyService;

    @Test
    void getOpenChannelIdsPretty() {
        Coins balance1 = LOCAL_OPEN_CHANNEL.getBalanceInformation().localAvailable();
        Coins balance2 = LOCAL_OPEN_CHANNEL_TO_NODE_3.getBalanceInformation().localAvailable();
        long ppm1 = POLICY_1.feeRate();
        long ppm2 = POLICY_2.feeRate();
        when(policyService.getPolicyTo(LOCAL_OPEN_CHANNEL.getId(), LOCAL_OPEN_CHANNEL.getRemotePubkey()))
                .thenReturn(Optional.of(POLICY_1));
        when(policyService.getPolicyTo(LOCAL_OPEN_CHANNEL_TO_NODE_3.getId(), PUBKEY_3))
                .thenReturn(Optional.of(POLICY_2));
        when(nodeService.getAlias(PUBKEY_2)).thenReturn(ALIAS_2);
        when(nodeService.getAlias(PUBKEY_3)).thenReturn(ALIAS_3);
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_TO_NODE_3));
        assertThat(legacyController.getOpenChannelIdsPretty())
                .isEqualTo("%s\t%s %s %s %s %s%n%s\t%s %s %s %s %s".formatted(
                        CHANNEL_ID_COMPACT, PUBKEY_2, padded(CAPACITY), padded(balance1), padded(ppm1), ALIAS_2,
                        CHANNEL_ID_COMPACT_4, PUBKEY_3, padded(CAPACITY_2), padded(balance2), padded(ppm2), ALIAS_3
                ));
    }

    private String padded(long longValue) {
        return String.format("%,6d", longValue);
    }

    private String padded(Coins coins) {
        return padded(coins.toStringSat());
    }

    private static String padded(String string) {
        return String.format("%11s", string);
    }

    @Test
    void getOpenChannelIdsPretty_ordered() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL_TO_NODE_3, LOCAL_OPEN_CHANNEL));
        assertThat(legacyController.getOpenChannelIdsPretty())
                .matches(CHANNEL_ID_COMPACT + ".*\n" + CHANNEL_ID_COMPACT_4 + ".*");
    }
}
