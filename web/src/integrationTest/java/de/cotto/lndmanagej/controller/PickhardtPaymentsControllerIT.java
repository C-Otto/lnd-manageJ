package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.controller.dto.ObjectMapperConfiguration;
import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.pickhardtpayments.MultiPathPaymentSplitter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.pickhardtpayments.PickhardtPaymentsConfiguration.DEFAULT_FEE_RATE_WEIGHT;
import static de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPaymentFixtures.MULTI_PATH_PAYMENT;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("CPD-START")
@Import(ObjectMapperConfiguration.class)
@WebMvcTest(controllers = PickhardtPaymentsController.class)
class PickhardtPaymentsControllerIT {
    private static final String PREFIX = "/beta/pickhardt-payments";
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MultiPathPaymentSplitter multiPathPaymentSplitter;

    @MockBean
    @SuppressWarnings("unused")
    private ChannelIdResolver channelIdResolver;

    @Test
    void sendTo() throws Exception {
        Coins amount = MULTI_PATH_PAYMENT.amount();
        String amountAsString = String.valueOf(amount.satoshis());
        String feesAsString = String.valueOf(MULTI_PATH_PAYMENT.fees().milliSatoshis());
        double expectedProbability = MULTI_PATH_PAYMENT.probability();
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(PUBKEY, amount, DEFAULT_FEE_RATE_WEIGHT))
                .thenReturn(MULTI_PATH_PAYMENT);
        mockMvc.perform(get("%s/to/%s/amount/%d".formatted(PREFIX, PUBKEY, amount.satoshis())))
                .andExpect(jsonPath("$.probability", is(expectedProbability)))
                .andExpect(jsonPath("$.amountSat", is(amountAsString)))
                .andExpect(jsonPath("$.feesMilliSat", is(feesAsString)))
                .andExpect(jsonPath("$.feeRate", is(200)))
                .andExpect(jsonPath("$.routes", hasSize(1)))
                .andExpect(jsonPath("$.routes[0].amountSat", is(amountAsString)))
                .andExpect(jsonPath("$.routes[0].channelIds", contains(CHANNEL_ID.toString())))
                .andExpect(jsonPath("$.routes[0].probability", is(expectedProbability)))
                .andExpect(jsonPath("$.routes[0].feesMilliSat", is(feesAsString)))
                .andExpect(jsonPath("$.routes[0].feeRate", is(200)));
    }

    @Test
    void sendTo_with_fee_rate_weight() throws Exception {
        int feeRateWeight = 999;
        Coins amount = MULTI_PATH_PAYMENT.amount();
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(PUBKEY, amount, feeRateWeight))
                .thenReturn(MULTI_PATH_PAYMENT);
        String url = "%s/to/%s/amount/%d/fee-rate-weight/%d"
                .formatted(PREFIX, PUBKEY, amount.satoshis(), feeRateWeight);
        mockMvc.perform(get(url)).andExpect(status().isOk());
    }

    @Test
    void send() throws Exception {
        Coins amount = MULTI_PATH_PAYMENT.amount();
        String amountAsString = String.valueOf(amount.satoshis());
        String feesAsString = String.valueOf(MULTI_PATH_PAYMENT.fees().milliSatoshis());
        double expectedProbability = MULTI_PATH_PAYMENT.probability();
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(PUBKEY, amount))
                .thenReturn(MULTI_PATH_PAYMENT);
        when(multiPathPaymentSplitter.getMultiPathPayment(
                PUBKEY,
                PUBKEY_2,
                Coins.ofSatoshis(1_234),
                DEFAULT_FEE_RATE_WEIGHT
        )).thenReturn(MULTI_PATH_PAYMENT);
        mockMvc.perform(get("%s/from/%s/to/%s/amount/%d".formatted(PREFIX, PUBKEY, PUBKEY_2, 1_234)))
                .andExpect(jsonPath("$.probability", is(expectedProbability)))
                .andExpect(jsonPath("$.amountSat", is(amountAsString)))
                .andExpect(jsonPath("$.feesMilliSat", is(feesAsString)))
                .andExpect(jsonPath("$.feeRate", is(200)))
                .andExpect(jsonPath("$.routes", hasSize(1)))
                .andExpect(jsonPath("$.routes[0].amountSat", is(amountAsString)))
                .andExpect(jsonPath("$.routes[0].channelIds", contains(CHANNEL_ID.toString())))
                .andExpect(jsonPath("$.routes[0].probability", is(expectedProbability)))
                .andExpect(jsonPath("$.routes[0].feesMilliSat", is(feesAsString)))
                .andExpect(jsonPath("$.routes[0].feeRate", is(200)));
    }

    @Test
    void send_with_fee_rate_weight() throws Exception {
        int feeRateWeight = 999;
        Coins amount = MULTI_PATH_PAYMENT.amount();
        when(multiPathPaymentSplitter.getMultiPathPayment(PUBKEY, PUBKEY_2, amount, feeRateWeight))
                .thenReturn(MULTI_PATH_PAYMENT);
        String url = "%s/from/%s/to/%s/amount/%d/fee-rate-weight/%d"
                .formatted(PREFIX, PUBKEY, PUBKEY_2, amount.satoshis(), feeRateWeight);
        mockMvc.perform(get(url)).andExpect(status().isOk());
    }
}
