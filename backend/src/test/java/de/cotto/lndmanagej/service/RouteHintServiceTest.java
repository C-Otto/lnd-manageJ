package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.DirectedChannelEdge;
import de.cotto.lndmanagej.model.Policy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.DecodedPaymentRequestFixtures.DECODED_PAYMENT_REQUEST;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_4;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
class RouteHintServiceTest {
    private static final Coins FIFTY_COINS = Coins.ofSatoshis(5_000_000_000L);

    @InjectMocks
    private RouteHintService routeHintService;

    @Test
    void get_initially_empty() {
        assertThat(routeHintService.getEdgesFromPaymentHints()).isEmpty();
    }

    @Test
    void get_after_adding_decoded_payment_request() {
        routeHintService.addDecodedPaymentRequest(DECODED_PAYMENT_REQUEST);
        Policy policy1 = new Policy(123, Coins.NONE, true, 9, FIFTY_COINS);
        Policy policy2 = new Policy(1234, Coins.ofMilliSatoshis(1), true, 40, FIFTY_COINS);
        DirectedChannelEdge edge1 = new DirectedChannelEdge(CHANNEL_ID, FIFTY_COINS, PUBKEY, PUBKEY_4, policy1);
        DirectedChannelEdge edge2 = new DirectedChannelEdge(CHANNEL_ID_2, FIFTY_COINS, PUBKEY_3, PUBKEY_4, policy2);
        assertThat(routeHintService.getEdgesFromPaymentHints()).contains(edge1, edge2);
    }

    @Test
    void clean() {
        assertThatCode(routeHintService::clean).doesNotThrowAnyException();
    }
}
