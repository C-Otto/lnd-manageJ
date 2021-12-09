package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.controller.dto.SelfPaymentDto;
import de.cotto.lndmanagej.service.SelfPaymentsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static de.cotto.lndmanagej.SelfPaymentFixtures.SELF_PAYMENT;
import static de.cotto.lndmanagej.SelfPaymentFixtures.SELF_PAYMENT_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SelfPaymentsControllerTest {
    @InjectMocks
    private SelfPaymentsController selfPaymentsController;

    @Mock
    private SelfPaymentsService service;

    @Test
    void getSelfPaymentsFromChannel() {
        when(service.getSelfPaymentsFromChannel(CHANNEL_ID)).thenReturn(List.of(SELF_PAYMENT, SELF_PAYMENT_2));
        assertThat(selfPaymentsController.getSelfPaymentsFromChannel(CHANNEL_ID)).containsExactly(
                SelfPaymentDto.createFromModel(SELF_PAYMENT),
                SelfPaymentDto.createFromModel(SELF_PAYMENT_2)
        );
    }

    @Test
    void getSelfPaymentsFromPeer() {
        when(service.getSelfPaymentsFromPeer(PUBKEY)).thenReturn(List.of(SELF_PAYMENT, SELF_PAYMENT_2));
        assertThat(selfPaymentsController.getSelfPaymentsFromPeer(PUBKEY)).containsExactly(
                SelfPaymentDto.createFromModel(SELF_PAYMENT),
                SelfPaymentDto.createFromModel(SELF_PAYMENT_2)
        );
    }

    @Test
    void getSelfPaymentsToChannel() {
        when(service.getSelfPaymentsToChannel(CHANNEL_ID)).thenReturn(List.of(SELF_PAYMENT, SELF_PAYMENT_2));
        assertThat(selfPaymentsController.getSelfPaymentsToChannel(CHANNEL_ID)).containsExactly(
                SelfPaymentDto.createFromModel(SELF_PAYMENT),
                SelfPaymentDto.createFromModel(SELF_PAYMENT_2)
        );
    }

    @Test
    void getSelfPaymentsToPeer() {
        when(service.getSelfPaymentsToPeer(PUBKEY)).thenReturn(List.of(SELF_PAYMENT, SELF_PAYMENT_2));
        assertThat(selfPaymentsController.getSelfPaymentsToPeer(PUBKEY)).containsExactly(
                SelfPaymentDto.createFromModel(SELF_PAYMENT),
                SelfPaymentDto.createFromModel(SELF_PAYMENT_2)
        );
    }

}