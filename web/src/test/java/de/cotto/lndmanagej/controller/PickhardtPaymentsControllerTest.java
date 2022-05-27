package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.controller.dto.MultiPathPaymentDto;
import de.cotto.lndmanagej.controller.dto.PaymentOptionsDto;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.HexString;
import de.cotto.lndmanagej.pickhardtpayments.MultiPathPaymentSender;
import de.cotto.lndmanagej.pickhardtpayments.MultiPathPaymentSplitter;
import de.cotto.lndmanagej.pickhardtpayments.TopUpService;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentOptions;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentStatus;
import de.cotto.lndmanagej.service.GraphService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_4;
import static de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPaymentFixtures.MULTI_PATH_PAYMENT;
import static de.cotto.lndmanagej.pickhardtpayments.model.PaymentOptions.DEFAULT_PAYMENT_OPTIONS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PickhardtPaymentsControllerTest {

    private static final String PAYMENT_REQUEST = "xxx";
    private static final String STREAM_RESPONSE = "beep beep boop!";

    private static final PaymentOptions PAYMENT_OPTIONS;
    private static final PaymentOptionsDto PAYMENT_OPTIONS_DTO;

    static {
        PAYMENT_OPTIONS = new PaymentOptions(
                Optional.of(123),
                Optional.of(999L),
                Optional.of(777L),
                false,
                Optional.of(PUBKEY_4)
        );
        PAYMENT_OPTIONS_DTO = new PaymentOptionsDto();
        PAYMENT_OPTIONS_DTO.setFeeRateWeight(PAYMENT_OPTIONS.feeRateWeight().orElse(null));
        PAYMENT_OPTIONS_DTO.setFeeRateLimit(PAYMENT_OPTIONS.feeRateLimit().orElse(null));
        PAYMENT_OPTIONS_DTO.setFeeRateLimitExceptIncomingHops(
                PAYMENT_OPTIONS.feeRateLimitExceptIncomingHops().orElse(null)
        );
        PAYMENT_OPTIONS_DTO.setIgnoreFeesForOwnChannels(PAYMENT_OPTIONS.ignoreFeesForOwnChannels());
        PAYMENT_OPTIONS_DTO.setPeer(PAYMENT_OPTIONS.peer().orElse(null));
    }

    @InjectMocks
    private PickhardtPaymentsController controller;

    @Mock
    private MultiPathPaymentSplitter multiPathPaymentSplitter;

    @Mock
    private MultiPathPaymentSender multiPathPaymentSender;

    @Mock
    private PaymentStatusStream paymentStatusStream;

    @Mock
    private GraphService graphService;

    @Mock
    private TopUpService topUpService;

    private final PaymentStatus paymentStatus = new PaymentStatus(HexString.EMPTY);

    @BeforeEach
    void setUp() {
        lenient().when(paymentStatusStream.getFor(any()))
                .thenReturn(outputStream -> outputStream.write(STREAM_RESPONSE.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void payPaymentRequest() {
        when(multiPathPaymentSender.payPaymentRequest(PAYMENT_REQUEST, DEFAULT_PAYMENT_OPTIONS))
                .thenReturn(paymentStatus);
        assertThat(controller.payPaymentRequest(PAYMENT_REQUEST).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void payPaymentRequest_with_payment_options() {
        PaymentOptions paymentOptions = PaymentOptions.forFeeRateWeight(456);
        when(multiPathPaymentSender.payPaymentRequest(PAYMENT_REQUEST, paymentOptions)).thenReturn(paymentStatus);
        assertThat(controller.payPaymentRequest(PAYMENT_REQUEST, withFeeRateWeight(456)).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void sendTo() {
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(PUBKEY, Coins.ofSatoshis(456), DEFAULT_PAYMENT_OPTIONS))
                .thenReturn(MULTI_PATH_PAYMENT);
        assertThat(controller.sendTo(PUBKEY, 456))
                .isEqualTo(MultiPathPaymentDto.fromModel(MULTI_PATH_PAYMENT));
    }

    @Test
    void sendTo_with_payment_options() {
        int feeRateWeight = 10;
        PaymentOptions paymentOptions = PaymentOptions.forFeeRateWeight(feeRateWeight);
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(PUBKEY, Coins.ofSatoshis(456), paymentOptions))
                .thenReturn(MULTI_PATH_PAYMENT);
        assertThat(controller.sendTo(PUBKEY, 456, withFeeRateWeight(feeRateWeight)))
                .isEqualTo(MultiPathPaymentDto.fromModel(MULTI_PATH_PAYMENT));
    }

    @Test
    void send() {
        when(multiPathPaymentSplitter.getMultiPathPayment(
                PUBKEY,
                PUBKEY_2,
                Coins.ofSatoshis(123),
                DEFAULT_PAYMENT_OPTIONS
        )).thenReturn(MULTI_PATH_PAYMENT);
        assertThat(controller.send(PUBKEY, PUBKEY_2, 123))
                .isEqualTo(MultiPathPaymentDto.fromModel(MULTI_PATH_PAYMENT));
    }

    @Test
    void send_with_payment_options() {
        when(multiPathPaymentSplitter.getMultiPathPayment(
                PUBKEY,
                PUBKEY_2,
                Coins.ofSatoshis(123),
                PAYMENT_OPTIONS
        )).thenReturn(MULTI_PATH_PAYMENT);
        assertThat(controller.send(PUBKEY, PUBKEY_2, 123, PAYMENT_OPTIONS_DTO))
                .isEqualTo(MultiPathPaymentDto.fromModel(MULTI_PATH_PAYMENT));
    }

    @Test
    void topUp() {
        assertThat(controller.topUp(PUBKEY, 123).getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(topUpService).topUp(PUBKEY, Coins.ofSatoshis(123), DEFAULT_PAYMENT_OPTIONS);
    }

    @Test
    void topUp_with_payment_options() {
        assertThat(controller.topUp(PUBKEY, 123, PAYMENT_OPTIONS_DTO).getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(topUpService).topUp(PUBKEY, Coins.ofSatoshis(123), PAYMENT_OPTIONS);
    }

    @Test
    void resetCache() {
        controller.resetGraph();
        verify(graphService).resetCache();
    }

    private static PaymentOptionsDto withFeeRateWeight(int feeRateWeight) {
        PaymentOptionsDto dto = new PaymentOptionsDto();
        dto.setFeeRateWeight(feeRateWeight);
        return dto;
    }
}
