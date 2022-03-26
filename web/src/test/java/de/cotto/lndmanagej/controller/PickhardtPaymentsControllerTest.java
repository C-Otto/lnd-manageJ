package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.controller.dto.MultiPathPaymentDto;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.pickhardtpayments.MultiPathPaymentSplitter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPaymentFixtures.MULTI_PATH_PAYMENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PickhardtPaymentsControllerTest {

    @InjectMocks
    private PickhardtPaymentsController controller;

    @Mock
    private MultiPathPaymentSplitter multiPathPaymentSplitter;

    @Test
    void sendTo() {
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(PUBKEY, Coins.ofSatoshis(456)))
                .thenReturn(MULTI_PATH_PAYMENT);
        assertThat(controller.sendTo(PUBKEY, 456))
                .isEqualTo(MultiPathPaymentDto.fromModel(MULTI_PATH_PAYMENT));
    }

    @Test
    void send() {
        when(multiPathPaymentSplitter.getMultiPathPayment(PUBKEY, PUBKEY_2, Coins.ofSatoshis(123)))
                .thenReturn(MULTI_PATH_PAYMENT);
        assertThat(controller.send(PUBKEY, PUBKEY_2, 123))
                .isEqualTo(MultiPathPaymentDto.fromModel(MULTI_PATH_PAYMENT));
    }
}
