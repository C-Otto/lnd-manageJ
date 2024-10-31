package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.DecodedPaymentRequest;
import de.cotto.lndmanagej.model.Edge;
import de.cotto.lndmanagej.model.Policy;
import de.cotto.lndmanagej.model.RouteHint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

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
    private static final Coins ONE_MILLI_SATOSHI = Coins.ofMilliSatoshis(1);
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
        Policy policy1 = new Policy(123, Coins.NONE, true, 9, ONE_MILLI_SATOSHI, FIFTY_COINS);
        Policy policy2 = new Policy(1234, ONE_MILLI_SATOSHI, true, 40, ONE_MILLI_SATOSHI, FIFTY_COINS);
        Edge edge1 =
                new Edge(CHANNEL_ID, PUBKEY, PUBKEY_4, FIFTY_COINS, policy1, Policy.UNKNOWN);
        Edge edge2 =
                new Edge(CHANNEL_ID_2, PUBKEY_3, PUBKEY_4, FIFTY_COINS, policy2, Policy.UNKNOWN);
        assertThat(routeHintService.getEdgesFromPaymentHints()).contains(edge1, edge2);
    }

    @Test
    void ignores_duplicate_channel_id() {
        routeHintService.addDecodedPaymentRequest(new DecodedPaymentRequest(
                DECODED_PAYMENT_REQUEST.paymentRequest(),
                DECODED_PAYMENT_REQUEST.cltvExpiry(),
                DECODED_PAYMENT_REQUEST.description(),
                DECODED_PAYMENT_REQUEST.destination(),
                DECODED_PAYMENT_REQUEST.amount(),
                DECODED_PAYMENT_REQUEST.paymentHash(),
                DECODED_PAYMENT_REQUEST.paymentAddress(),
                DECODED_PAYMENT_REQUEST.creation(),
                DECODED_PAYMENT_REQUEST.expiry(),
                Set.of(new RouteHint(
                        PUBKEY, PUBKEY_4, CHANNEL_ID, Coins.NONE, 123, 9
                ), new RouteHint(
                        PUBKEY_3, PUBKEY_4, CHANNEL_ID, Coins.ofMilliSatoshis(1), 1234, 40
                ))
        ));
        assertThat(routeHintService.getEdgesFromPaymentHints()).hasSize(1);
    }

    @Test
    void clean() {
        assertThatCode(routeHintService::clean).doesNotThrowAnyException();
    }
}
