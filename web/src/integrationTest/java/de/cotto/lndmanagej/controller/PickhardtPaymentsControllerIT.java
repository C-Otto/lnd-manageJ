package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.controller.dto.ObjectMapperConfiguration;
import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.HexString;
import de.cotto.lndmanagej.pickhardtpayments.MultiPathPaymentSender;
import de.cotto.lndmanagej.pickhardtpayments.MultiPathPaymentSplitter;
import de.cotto.lndmanagej.pickhardtpayments.TopUpService;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentOptions;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentStatus;
import de.cotto.lndmanagej.service.GraphService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_5;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_4;
import static de.cotto.lndmanagej.model.RouteFixtures.ROUTE;
import static de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPaymentFixtures.MULTI_PATH_PAYMENT;
import static de.cotto.lndmanagej.pickhardtpayments.model.PaymentOptions.DEFAULT_PAYMENT_OPTIONS;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("CPD-START")
@Import({ObjectMapperConfiguration.class, PaymentStatusStream.class})
@WebMvcTest(controllers = PickhardtPaymentsController.class)
class PickhardtPaymentsControllerIT {
    private static final String PREFIX = "/beta/pickhardt-payments";
    private static final String PAYMENT_REQUEST = "xxx";
    private static final PaymentOptions PAYMENT_OPTIONS = new PaymentOptions(
            123,
            Optional.of(999L),
            Optional.of(777L),
            false,
            Optional.of(PUBKEY_4)
    );
    private static final String DTO_AS_STRING = "{" +
            "  \"feeRateWeight\": 123," +
            "  \"feeRateLimit\": 999," +
            "  \"feeRateLimitExceptIncomingHops\": 777," +
            "  \"ignoreFeesForOwnChannels\": false," +
            "  \"peer\": \"000000000000000000000000000000000000000000000000000000000000000004\"" +
            "}";

    @Autowired
    private MockMvc mockMvc;

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

    private final PaymentStatus paymentStatus = new PaymentStatus(HexString.EMPTY);

    @Test
    void payPaymentRequest() throws Exception {
        when(multiPathPaymentSender.payPaymentRequest(PAYMENT_REQUEST, DEFAULT_PAYMENT_OPTIONS))
                .thenReturn(paymentStatus);
        String url = "%s/pay-payment-request/%s".formatted(PREFIX, PAYMENT_REQUEST);
        mockMvc.perform(get(url))
                .andExpect(status().isOk());
    }

    @Test
    void payPaymentRequest_with_payment_options() throws Exception {
        when(multiPathPaymentSender.payPaymentRequest(PAYMENT_REQUEST, PAYMENT_OPTIONS)).thenReturn(paymentStatus);
        String url = "%s/pay-payment-request/%s".formatted(PREFIX, PAYMENT_REQUEST);
        mockMvc.perform(post(url).contentType(APPLICATION_JSON).content(DTO_AS_STRING))
                .andExpect(status().isOk());
    }

    @Test
    void sendTo() throws Exception {
        Coins amount = MULTI_PATH_PAYMENT.amount();
        String amountAsString = String.valueOf(amount.satoshis());
        String route1AmountAsString = String.valueOf(ROUTE.getAmount().satoshis());
        String feesAsString = String.valueOf(MULTI_PATH_PAYMENT.fees().milliSatoshis());
        String feesWithFirstHopAsString = String.valueOf(MULTI_PATH_PAYMENT.feesWithFirstHop().milliSatoshis());
        double expectedProbability = MULTI_PATH_PAYMENT.probability();
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(PUBKEY, amount, DEFAULT_PAYMENT_OPTIONS))
                .thenReturn(MULTI_PATH_PAYMENT);
        mockMvc.perform(get("%s/to/%s/amount/%d".formatted(PREFIX, PUBKEY, amount.satoshis())))
                .andExpect(jsonPath("$.probability", is(expectedProbability)))
                .andExpect(jsonPath("$.amountSat", is(amountAsString)))
                .andExpect(jsonPath("$.feesMilliSat", is(feesAsString)))
                .andExpect(jsonPath("$.feesWithFirstHopMilliSat", is(feesWithFirstHopAsString)))
                .andExpect(jsonPath("$.feeRate", is(266)))
                .andExpect(jsonPath("$.feeRateWithFirstHop", is(466)))
                .andExpect(jsonPath("$.routes", hasSize(2)))
                .andExpect(jsonPath("$.routes[0].amountSat", is(route1AmountAsString)))
                .andExpect(jsonPath("$.routes[0].channelIds", contains(
                        CHANNEL_ID.toString(),
                        CHANNEL_ID_3.toString(),
                        CHANNEL_ID_5.toString()
                )))
                .andExpect(jsonPath("$.routes[0].probability",
                        is(ROUTE.getProbability())))
                .andExpect(jsonPath("$.routes[0].feesMilliSat",
                        is(String.valueOf(ROUTE.getFees().milliSatoshis()))))
                .andExpect(jsonPath("$.routes[0].feesWithFirstHopMilliSat",
                        is(String.valueOf(ROUTE.getFeesWithFirstHop().milliSatoshis()))))
                .andExpect(jsonPath("$.routes[0].feeRate",
                        is((int) ROUTE.getFeeRate())))
                .andExpect(jsonPath("$.routes[0].feeRateWithFirstHop",
                        is((int) ROUTE.getFeeRateWithFirstHop())));
    }

    @Test
    void sendTo_with_payment_options() throws Exception {
        Coins amount = MULTI_PATH_PAYMENT.amount();
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(PUBKEY, amount, PAYMENT_OPTIONS))
                .thenReturn(MULTI_PATH_PAYMENT);
        String url = "%s/to/%s/amount/%d".formatted(PREFIX, PUBKEY, amount.satoshis());
        mockMvc.perform(post(url).contentType(APPLICATION_JSON).content(DTO_AS_STRING))
                .andExpect(status().isOk());
    }

    @Test
    void send() throws Exception {
        Coins amount = MULTI_PATH_PAYMENT.amount();
        String amountAsString = String.valueOf(amount.satoshis());
        String feesAsString = String.valueOf(MULTI_PATH_PAYMENT.fees().milliSatoshis());
        double expectedProbability = MULTI_PATH_PAYMENT.probability();
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(PUBKEY, amount, DEFAULT_PAYMENT_OPTIONS))
                .thenReturn(MULTI_PATH_PAYMENT);
        when(multiPathPaymentSplitter.getMultiPathPayment(
                PUBKEY,
                PUBKEY_2,
                Coins.ofSatoshis(1_234),
                DEFAULT_PAYMENT_OPTIONS
        )).thenReturn(MULTI_PATH_PAYMENT);
        mockMvc.perform(get("%s/from/%s/to/%s/amount/%d".formatted(PREFIX, PUBKEY, PUBKEY_2, 1_234)))
                .andExpect(jsonPath("$.probability", is(expectedProbability)))
                .andExpect(jsonPath("$.amountSat", is(amountAsString)))
                .andExpect(jsonPath("$.feesMilliSat", is(feesAsString)))
                .andExpect(jsonPath("$.feeRate", is(266)))
                .andExpect(jsonPath("$.routes", hasSize(2)));
    }

    @Test
    void send_with_payment_options() throws Exception {
        Coins amount = MULTI_PATH_PAYMENT.amount();
        when(multiPathPaymentSplitter.getMultiPathPayment(PUBKEY, PUBKEY_2, amount, PAYMENT_OPTIONS))
                .thenReturn(MULTI_PATH_PAYMENT);
        String url = "%s/from/%s/to/%s/amount/%d".formatted(PREFIX, PUBKEY, PUBKEY_2, amount.satoshis());
        mockMvc.perform(post(url).contentType(APPLICATION_JSON).content(DTO_AS_STRING))
                .andExpect(status().isOk());
    }

    @Test
    void topUp() throws Exception {
        String url = "%s/top-up/%s/amount/%s".formatted(PREFIX, PUBKEY, "123");
        mockMvc.perform(get(url)).andExpect(status().isOk());
        verify(topUpService).topUp(PUBKEY, Coins.ofSatoshis(123), DEFAULT_PAYMENT_OPTIONS);
    }

    @Test
    void topUp_with_payment_options() throws Exception {
        when(topUpService.topUp(any(), any(), any())).thenReturn(new PaymentStatus(HexString.EMPTY));
        String url = "%s/top-up/%s/amount/%s".formatted(PREFIX, PUBKEY, "123");
        mockMvc.perform(post(url).contentType(APPLICATION_JSON).content(DTO_AS_STRING)).andExpect(status().isOk());
        verify(topUpService).topUp(PUBKEY, Coins.ofSatoshis(123), PAYMENT_OPTIONS);
    }
}
