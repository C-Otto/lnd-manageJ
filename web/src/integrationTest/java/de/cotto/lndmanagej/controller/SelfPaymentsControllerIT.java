package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.service.SelfPaymentsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static de.cotto.lndmanagej.SelfPaymentFixtures.SELF_PAYMENT;
import static de.cotto.lndmanagej.SelfPaymentFixtures.SELF_PAYMENT_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_4;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@WebMvcTest(controllers = SelfPaymentsController.class)
class SelfPaymentsControllerIT {
    private static final String CHANNEL_PREFIX = "/api/channel/" + CHANNEL_ID.getShortChannelId() + "/";
    private static final String NODE_PREFIX = "/api/node/" + PUBKEY + "/";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @SuppressWarnings("unused")
    private ChannelIdResolver channelIdResolver;

    @MockBean
    private SelfPaymentsService selfPaymentsService;

    @Test
    void getSelfPaymentsFromChannel() throws Exception {
        when(selfPaymentsService.getSelfPaymentsFromChannel(CHANNEL_ID))
                .thenReturn(List.of(SELF_PAYMENT_2, SELF_PAYMENT));
        mockMvc.perform(get(CHANNEL_PREFIX + "/self-payments-from-channel/"))
                .andExpect(jsonPath("$.selfPayments[0].memo", is(SELF_PAYMENT_2.memo())))
                .andExpect(jsonPath("$.selfPayments[1].memo", is(SELF_PAYMENT.memo())));
    }

    @Test
    void getSelfPaymentsFromPeer() throws Exception {
        when(selfPaymentsService.getSelfPaymentsFromPeer(PUBKEY))
                .thenReturn(List.of(SELF_PAYMENT_2, SELF_PAYMENT));
        mockMvc.perform(get(NODE_PREFIX + "/self-payments-from-peer/"))
                .andExpect(jsonPath("$.selfPayments[0].memo", is(SELF_PAYMENT_2.memo())))
                .andExpect(jsonPath("$.selfPayments[1].memo", is(SELF_PAYMENT.memo())));
    }

    @Test
    void getSelfPaymentsToChannel() throws Exception {
        when(selfPaymentsService.getSelfPaymentsToChannel(CHANNEL_ID))
                .thenReturn(List.of(SELF_PAYMENT, SELF_PAYMENT_2));
        mockMvc.perform(get(CHANNEL_PREFIX + "/self-payments-to-channel/"))
                .andExpect(jsonPath("$.selfPayments[0].memo", is(SELF_PAYMENT.memo())))
                .andExpect(jsonPath("$.selfPayments[0].settleDate", is(SELF_PAYMENT.settleDate().toString())))
                .andExpect(jsonPath("$.selfPayments[0].value", is(msat(SELF_PAYMENT.value()))))
                .andExpect(jsonPath("$.selfPayments[0].fees", is(msat(SELF_PAYMENT.fees()))))
                .andExpect(jsonPath("$.selfPayments[0].firstChannel", is(CHANNEL_ID_4.toString())))
                .andExpect(jsonPath("$.selfPayments[0].lastChannel", is(CHANNEL_ID_2.toString())))
                .andExpect(jsonPath("$.selfPayments[1].memo", is(SELF_PAYMENT_2.memo())))
                .andExpect(jsonPath("$.selfPayments[1].settleDate", is(SELF_PAYMENT_2.settleDate().toString())))
                .andExpect(jsonPath("$.selfPayments[1].value", is(msat(SELF_PAYMENT_2.value()))))
                .andExpect(jsonPath("$.selfPayments[1].fees", is(msat(SELF_PAYMENT_2.fees()))))
                .andExpect(jsonPath("$.selfPayments[1].firstChannel", is(not(empty()))))
                .andExpect(jsonPath("$.selfPayments[1].lastChannel", is(CHANNEL_ID.toString())));
    }

    @Test
    void getSelfPaymentsToPeer() throws Exception {
        when(selfPaymentsService.getSelfPaymentsToPeer(PUBKEY))
                .thenReturn(List.of(SELF_PAYMENT_2, SELF_PAYMENT));
        mockMvc.perform(get(NODE_PREFIX + "/self-payments-to-peer/"))
                .andExpect(jsonPath("$.selfPayments[0].memo", is(SELF_PAYMENT_2.memo())))
                .andExpect(jsonPath("$.selfPayments[1].memo", is(SELF_PAYMENT.memo())));
    }

    private String msat(Coins value) {
        return String.valueOf(value.milliSatoshis());
    }
}