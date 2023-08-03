package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelIdParser;
import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.service.SelfPaymentsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_4;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.SelfPaymentFixtures.SELF_PAYMENT;
import static de.cotto.lndmanagej.model.SelfPaymentFixtures.SELF_PAYMENT_2;
import static de.cotto.lndmanagej.model.SelfPaymentFixtures.SELF_PAYMENT_4;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@SuppressWarnings({"CPD-START", "PMD.AvoidDuplicateLiterals"})
@WebFluxTest(SelfPaymentsController.class)
@Import(ChannelIdParser.class)
class SelfPaymentsControllerIT {
    private static final String CHANNEL_PREFIX = "/api/channel/" + CHANNEL_ID.getShortChannelId() + "/";
    private static final String NODE_PREFIX = "/api/node/" + PUBKEY + "/";

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    @SuppressWarnings("unused")
    private ChannelIdResolver channelIdResolver;

    @MockBean
    private SelfPaymentsService selfPaymentsService;

    @Test
    void getSelfPaymentsFromChannel() {
        when(selfPaymentsService.getSelfPaymentsFromChannel(CHANNEL_ID))
                .thenReturn(List.of(SELF_PAYMENT_2, SELF_PAYMENT));
        webTestClient.get().uri(CHANNEL_PREFIX + "/self-payments-from-channel").exchange().expectBody()
                .jsonPath("$.selfPayments[0].memo").value(is(SELF_PAYMENT_2.memo()))
                .jsonPath("$.selfPayments[1].memo").value(is(SELF_PAYMENT.memo()))
                .jsonPath("$.feesMilliSat").value(is("20"))
                .jsonPath("$.amountPaidMilliSat").value(is("4690"));
    }

    @Test
    void getSelfPaymentsFromPeer() {
        when(selfPaymentsService.getSelfPaymentsFromPeer(PUBKEY))
                .thenReturn(List.of(SELF_PAYMENT_2, SELF_PAYMENT));
        webTestClient.get().uri(NODE_PREFIX + "/self-payments-from-peer").exchange().expectBody()
                .jsonPath("$.selfPayments[0].memo").value(is(SELF_PAYMENT_2.memo()))
                .jsonPath("$.selfPayments[1].memo").value(is(SELF_PAYMENT.memo()))
                .jsonPath("$.feesMilliSat").value(is("20"))
                .jsonPath("$.amountPaidMilliSat").value(is("4690"));
    }

    @Test
    void getSelfPaymentsToChannel() {
        when(selfPaymentsService.getSelfPaymentsToChannel(CHANNEL_ID))
                .thenReturn(List.of(SELF_PAYMENT, SELF_PAYMENT_4));
        webTestClient.get().uri(CHANNEL_PREFIX + "/self-payments-to-channel").exchange().expectBody()
                .jsonPath("$.selfPayments[0].memo").value(is(SELF_PAYMENT.memo()))
                .jsonPath("$.selfPayments[0].settleDate").value(is(SELF_PAYMENT.settleDate().toString()))
                .jsonPath("$.selfPayments[0].amountPaidMilliSat").value(is(msat(SELF_PAYMENT.amountPaid())))
                .jsonPath("$.selfPayments[0].feesMilliSat").value(is(msat(SELF_PAYMENT.fees())))
                .jsonPath("$.selfPayments[0].routes[0].channelIdOut").value(is(CHANNEL_ID_4.toString()))
                .jsonPath("$.selfPayments[0].routes[0].amountMilliSat").value(is("2000"))
                .jsonPath("$.selfPayments[0].routes[0].channelIdIn").value(is(CHANNEL_ID_2.toString()))
                .jsonPath("$.selfPayments[1].memo").value(is(SELF_PAYMENT_4.memo()))
                .jsonPath("$.selfPayments[1].settleDate").value(is(SELF_PAYMENT_4.settleDate().toString()))
                .jsonPath("$.selfPayments[1].amountPaidMilliSat").value(is(msat(SELF_PAYMENT_4.amountPaid())))
                .jsonPath("$.selfPayments[1].feesMilliSat").value(is(msat(SELF_PAYMENT_4.fees())))
                .jsonPath("$.selfPayments[1].routes[0].channelIdOut").value(is(CHANNEL_ID_4.toString()))
                .jsonPath("$.selfPayments[1].routes[0].channelIdIn").value(is(CHANNEL_ID.toString()))
                .jsonPath("$.selfPayments[1].routes[1].channelIdOut").value(is(CHANNEL_ID_3.toString()))
                .jsonPath("$.selfPayments[1].routes[1].channelIdIn").value(is(CHANNEL_ID_2.toString()))
                .jsonPath("$.feesMilliSat").value(is("20"))
                .jsonPath("$.amountPaidMilliSat").value(is("4690"));
    }

    @Test
    void getSelfPaymentsToPeer() {
        when(selfPaymentsService.getSelfPaymentsToPeer(PUBKEY))
                .thenReturn(List.of(SELF_PAYMENT_2, SELF_PAYMENT));
        webTestClient.get().uri(NODE_PREFIX + "/self-payments-to-peer").exchange().expectBody()
                .jsonPath("$.selfPayments[0].memo").value(is(SELF_PAYMENT_2.memo()))
                .jsonPath("$.selfPayments[1].memo").value(is(SELF_PAYMENT.memo()))
                .jsonPath("$.feesMilliSat").value(is("20"))
                .jsonPath("$.amountPaidMilliSat").value(is("4690"));
    }

    private String msat(Coins value) {
        return String.valueOf(value.milliSatoshis());
    }
}
