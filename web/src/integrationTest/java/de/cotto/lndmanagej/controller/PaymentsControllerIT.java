package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.controller.dto.ObjectMapperConfiguration;
import de.cotto.lndmanagej.model.ChannelIdParser;
import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.pickhardtpayments.MultiPathPaymentSender;
import de.cotto.lndmanagej.pickhardtpayments.MultiPathPaymentSplitter;
import de.cotto.lndmanagej.pickhardtpayments.TopUpService;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentOptions;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentStatus;
import de.cotto.lndmanagej.service.GraphService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_5;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.RouteFixtures.ROUTE;
import static de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPaymentFixtures.MULTI_PATH_PAYMENT;
import static de.cotto.lndmanagej.pickhardtpayments.model.PaymentOptions.DEFAULT_PAYMENT_OPTIONS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SuppressWarnings("CPD-START")
@WebFluxTest(PaymentsController.class)
@Import({ObjectMapperConfiguration.class, ChannelIdParser.class})
class PaymentsControllerIT {
    private static final String PREFIX = "/api/payments";
    private static final String PAYMENT_REQUEST = "xxx";
    private static final PaymentOptions PAYMENT_OPTIONS = new PaymentOptions(
            Optional.of(123),
            Optional.of(999L),
            Optional.empty(),
            false,
            Optional.empty(),
            Optional.empty()
    );
    private static final int FINAL_CLTV_EXPIRY = 0;
    private static final String DTO_AS_STRING = "{" +
            "  \"feeRateWeight\": 123," +
            "  \"feeRateLimit\": 999," +
            "  \"ignoreFeesForOwnChannels\": false" +
            "}";

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MultiPathPaymentSplitter multiPathPaymentSplitter;

    @MockBean
    private MultiPathPaymentSender multiPathPaymentSender;

    @MockBean
    private TopUpService topUpService;

    @MockBean
    @SuppressWarnings("unused")
    private GraphService graphService;

    @MockBean
    @SuppressWarnings("unused")
    private ChannelIdResolver channelIdResolver;

    private final PaymentStatus paymentStatus = PaymentStatus.createFailure("done");

    @Test
    void payPaymentRequest() {
        when(multiPathPaymentSender.payPaymentRequest(PAYMENT_REQUEST, DEFAULT_PAYMENT_OPTIONS))
                .thenReturn(paymentStatus);
        String url = "%s/pay-payment-request/%s".formatted(PREFIX, PAYMENT_REQUEST);
        webTestClient.get().uri(url).exchange()
                .expectStatus().isOk();
    }

    @Test
    void payPaymentRequest_with_payment_options() {
        when(multiPathPaymentSender.payPaymentRequest(PAYMENT_REQUEST, PAYMENT_OPTIONS)).thenReturn(paymentStatus);
        String url = "%s/pay-payment-request/%s".formatted(PREFIX, PAYMENT_REQUEST);
        webTestClient.post().uri(url).contentType(APPLICATION_JSON).bodyValue(DTO_AS_STRING).exchange()
                .expectStatus().isOk();
    }

    @Test
    void sendTo() {
        Coins amount = MULTI_PATH_PAYMENT.amount();
        String amountAsString = String.valueOf(amount.satoshis());
        String route1AmountAsString = String.valueOf(ROUTE.getAmount().satoshis());
        String feesAsString = String.valueOf(MULTI_PATH_PAYMENT.fees().milliSatoshis());
        String feesWithFirstHopAsString = String.valueOf(MULTI_PATH_PAYMENT.feesWithFirstHop().milliSatoshis());
        double expectedProbability = MULTI_PATH_PAYMENT.probability();
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(PUBKEY, amount, DEFAULT_PAYMENT_OPTIONS, FINAL_CLTV_EXPIRY))
                .thenReturn(MULTI_PATH_PAYMENT);
        webTestClient.get().uri("%s/to/%s/amount/%d".formatted(PREFIX, PUBKEY, amount.satoshis())).exchange()
                .expectBody()
                .jsonPath("$.probability").value(is(expectedProbability))
                .jsonPath("$.amountSat").value(is(amountAsString))
                .jsonPath("$.feesMilliSat").value(is(feesAsString))
                .jsonPath("$.feesWithFirstHopMilliSat").value(is(feesWithFirstHopAsString))
                .jsonPath("$.feeRate").value(is(266))
                .jsonPath("$.feeRateWithFirstHop").value(is(466))
                .jsonPath("$.routes").value(hasSize(2))
                .jsonPath("$.routes[0].amountSat").value(is(route1AmountAsString))
                .jsonPath("$.routes[0].channelIds").value(contains(
                        CHANNEL_ID.toString(),
                        CHANNEL_ID_3.toString(),
                        CHANNEL_ID_5.toString()
                ))
                .jsonPath("$.routes[0].probability").value(is(ROUTE.getProbability()))
                .jsonPath("$.routes[0].feesMilliSat").value(is(String.valueOf(ROUTE.getFees().milliSatoshis())))
                .jsonPath("$.routes[0].feesWithFirstHopMilliSat").value(
                        is(String.valueOf(ROUTE.getFeesWithFirstHop().milliSatoshis()))
                )
                .jsonPath("$.routes[0].feeRate").value(is((int) ROUTE.getFeeRate()))
                .jsonPath("$.routes[0].feeRateWithFirstHop").value(is((int) ROUTE.getFeeRateWithFirstHop()));
    }

    @Test
    void sendTo_with_payment_options() {
        Coins amount = MULTI_PATH_PAYMENT.amount();
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(PUBKEY, amount, PAYMENT_OPTIONS, FINAL_CLTV_EXPIRY))
                .thenReturn(MULTI_PATH_PAYMENT);
        String url = "%s/to/%s/amount/%d".formatted(PREFIX, PUBKEY, amount.satoshis());
        webTestClient.post().uri(url).contentType(APPLICATION_JSON).bodyValue(DTO_AS_STRING).exchange()
                .expectStatus().isOk();
    }

    @Test
    void send() {
        Coins amount = MULTI_PATH_PAYMENT.amount();
        String amountAsString = String.valueOf(amount.satoshis());
        String feesAsString = String.valueOf(MULTI_PATH_PAYMENT.fees().milliSatoshis());
        double expectedProbability = MULTI_PATH_PAYMENT.probability();
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(PUBKEY, amount, DEFAULT_PAYMENT_OPTIONS, FINAL_CLTV_EXPIRY))
                .thenReturn(MULTI_PATH_PAYMENT);
        when(multiPathPaymentSplitter.getMultiPathPayment(
                PUBKEY,
                PUBKEY_2,
                Coins.ofSatoshis(1_234),
                DEFAULT_PAYMENT_OPTIONS,
                FINAL_CLTV_EXPIRY
        )).thenReturn(MULTI_PATH_PAYMENT);
        webTestClient.get().uri("%s/from/%s/to/%s/amount/%d".formatted(PREFIX, PUBKEY, PUBKEY_2, 1_234))
                .exchange()
                .expectBody()
                .jsonPath("$.probability").value(is(expectedProbability))
                .jsonPath("$.amountSat").value(is(amountAsString))
                .jsonPath("$.feesMilliSat").value(is(feesAsString))
                .jsonPath("$.feeRate").value(is(266))
                .jsonPath("$.routes").value(hasSize(2));
    }

    @Test
    void send_with_payment_options() {
        Coins amount = MULTI_PATH_PAYMENT.amount();
        when(multiPathPaymentSplitter.getMultiPathPayment(PUBKEY, PUBKEY_2, amount, PAYMENT_OPTIONS, FINAL_CLTV_EXPIRY))
                .thenReturn(MULTI_PATH_PAYMENT);
        String url = "%s/from/%s/to/%s/amount/%d".formatted(PREFIX, PUBKEY, PUBKEY_2, amount.satoshis());
        webTestClient.post().uri(url).contentType(APPLICATION_JSON).bodyValue(DTO_AS_STRING).exchange()
                .expectStatus().isOk();
    }

    @Nested
    class TopUp {
        private final String url = "%s/top-up/%s/amount/%s".formatted(PREFIX, PUBKEY, "123");

        @Test
        void topUp() {
            when(topUpService.topUp(any(), any(), any(), any())).thenReturn(paymentStatus);
            PaymentOptions emptyPaymentOptions = new PaymentOptions(
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    true,
                    Optional.empty(),
                    Optional.empty()
            );
            webTestClient.get().uri(url).exchange().expectStatus().isOk();
            verify(topUpService).topUp(PUBKEY, Optional.empty(), Coins.ofSatoshis(123), emptyPaymentOptions);
        }

        @Test
        void topUp_with_peer_for_first_hop() {
            when(topUpService.topUp(any(), any(), any(), any())).thenReturn(paymentStatus);
            PaymentOptions emptyPaymentOptions = new PaymentOptions(
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    true,
                    Optional.empty(),
                    Optional.empty()
            );
            String uri = "%s/top-up/%s/amount/%s/via/%s".formatted(PREFIX, PUBKEY, "123", PUBKEY_2);
            webTestClient.get().uri(uri).exchange().expectStatus().isOk();
            verify(topUpService).topUp(PUBKEY, Optional.of(PUBKEY_2), Coins.ofSatoshis(123), emptyPaymentOptions);
        }

        @Test
        void with_payment_options() {
            when(topUpService.topUp(any(), any(), any(), any())).thenReturn(paymentStatus);
            webTestClient.post().uri(url).contentType(APPLICATION_JSON).bodyValue(DTO_AS_STRING).exchange()
                    .expectStatus().isOk();
            verify(topUpService).topUp(PUBKEY, Optional.empty(), Coins.ofSatoshis(123), PAYMENT_OPTIONS);
        }

        @Test
        void with_peer_for_first_hop_and_payment_options() {
            when(topUpService.topUp(any(), any(), any(), any())).thenReturn(paymentStatus);
            String uri = "%s/top-up/%s/amount/%s/via/%s".formatted(PREFIX, PUBKEY, "123", PUBKEY_2);
            webTestClient.post().uri(uri).contentType(APPLICATION_JSON).bodyValue(DTO_AS_STRING).exchange()
                    .expectStatus().isOk();
            verify(topUpService).topUp(PUBKEY, Optional.of(PUBKEY_2), Coins.ofSatoshis(123), PAYMENT_OPTIONS);
        }

        @Test
        void no_linebreaks_within_line() {
            when(topUpService.topUp(any(), any(), any(), any())).thenReturn(paymentStatus);
            webTestClient.get().uri(url).exchange().expectBody(String.class).value(string ->
                    assertThat(string.substring(0, string.length() - 1)).doesNotContain("\n")
            );
        }

        @Test
        void linebreak_at_end_of_line() {
            when(topUpService.topUp(any(), any(), any(), any())).thenReturn(paymentStatus);
            webTestClient.get().uri(url).exchange().expectBody(String.class).value(string ->
                    assertThat(string).endsWith("\n")
            );
        }
    }
}
