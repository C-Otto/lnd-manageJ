package de.cotto.lndmanagej.controller;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.controller.dto.MultiPathPaymentDto;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.pickhardtpayments.MultiPathPaymentSender;
import de.cotto.lndmanagej.pickhardtpayments.MultiPathPaymentSplitter;
import de.cotto.lndmanagej.pickhardtpayments.TopUpService;
import de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPayment;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentOptions;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentStatus;
import de.cotto.lndmanagej.service.GraphService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import static de.cotto.lndmanagej.pickhardtpayments.model.PaymentOptions.DEFAULT_PAYMENT_OPTIONS;
import static org.springframework.http.MediaType.APPLICATION_NDJSON;

@RestController
@RequestMapping("/beta/pickhardt-payments/")
public class PickhardtPaymentsController {
    private final MultiPathPaymentSplitter multiPathPaymentSplitter;
    private final MultiPathPaymentSender multiPathPaymentSender;
    private final PaymentStatusStream paymentStatusStream;
    private final TopUpService topUpService;
    private final GraphService graphService;

    public PickhardtPaymentsController(
            MultiPathPaymentSplitter multiPathPaymentSplitter,
            MultiPathPaymentSender multiPathPaymentSender,
            PaymentStatusStream paymentStatusStream,
            TopUpService topUpService,
            GraphService graphService
    ) {
        this.multiPathPaymentSplitter = multiPathPaymentSplitter;
        this.multiPathPaymentSender = multiPathPaymentSender;
        this.paymentStatusStream = paymentStatusStream;
        this.topUpService = topUpService;
        this.graphService = graphService;
    }

    @Timed
    @GetMapping("/pay-payment-request/{paymentRequest}")
    public ResponseEntity<StreamingResponseBody> payPaymentRequest(@PathVariable String paymentRequest) {
        return payPaymentRequest(paymentRequest, DEFAULT_PAYMENT_OPTIONS.feeRateWeight());
    }

    @Timed
    @GetMapping("/pay-payment-request/{paymentRequest}/fee-rate-weight/{feeRateWeight}")
    public ResponseEntity<StreamingResponseBody> payPaymentRequest(
            @PathVariable String paymentRequest,
            @PathVariable int feeRateWeight
    ) {
        PaymentOptions paymentOptions = PaymentOptions.forFeeRateWeight(feeRateWeight);
        PaymentStatus paymentStatus = multiPathPaymentSender.payPaymentRequest(paymentRequest, paymentOptions);
        return toStream(paymentStatus);
    }

    @Timed
    @GetMapping("/to/{pubkey}/amount/{amount}/fee-rate-weight/{feeRateWeight}")
    public MultiPathPaymentDto sendTo(
            @PathVariable Pubkey pubkey,
            @PathVariable long amount,
            @PathVariable int feeRateWeight
    ) {
        Coins coins = Coins.ofSatoshis(amount);
        PaymentOptions paymentOptions = PaymentOptions.forFeeRateWeight(feeRateWeight);
        MultiPathPayment multiPathPaymentTo =
                multiPathPaymentSplitter.getMultiPathPaymentTo(pubkey, coins, paymentOptions);
        return MultiPathPaymentDto.fromModel(multiPathPaymentTo);
    }

    @Timed
    @GetMapping("/to/{pubkey}/amount/{amount}")
    public MultiPathPaymentDto sendTo(
            @PathVariable Pubkey pubkey,
            @PathVariable long amount
    ) {
        return sendTo(pubkey, amount, DEFAULT_PAYMENT_OPTIONS.feeRateWeight());
    }

    @Timed
    @GetMapping("/from/{source}/to/{target}/amount/{amount}/fee-rate-weight/{feeRateWeight}")
    public MultiPathPaymentDto send(
            @PathVariable Pubkey source,
            @PathVariable Pubkey target,
            @PathVariable long amount,
            @PathVariable int feeRateWeight
    ) {
        Coins coins = Coins.ofSatoshis(amount);
        PaymentOptions paymentOptions = PaymentOptions.forFeeRateWeight(feeRateWeight);
        MultiPathPayment multiPathPayment =
                multiPathPaymentSplitter.getMultiPathPayment(source, target, coins, paymentOptions);
        return MultiPathPaymentDto.fromModel(multiPathPayment);
    }

    @Timed
    @GetMapping("/from/{source}/to/{target}/amount/{amount}")
    public MultiPathPaymentDto send(
            @PathVariable Pubkey source,
            @PathVariable Pubkey target,
            @PathVariable long amount
    ) {
        return send(source, target, amount, DEFAULT_PAYMENT_OPTIONS.feeRateWeight());
    }

    @Timed
    @GetMapping("/top-up/{pubkey}/amount/{amount}")
    public ResponseEntity<StreamingResponseBody> topUp(@PathVariable Pubkey pubkey, @PathVariable long amount) {
        PaymentStatus paymentStatus = topUpService.topUp(pubkey, Coins.ofSatoshis(amount));
        return toStream(paymentStatus);
    }

    private ResponseEntity<StreamingResponseBody> toStream(PaymentStatus paymentStatus) {
        StreamingResponseBody streamingResponseBody = paymentStatusStream.getFor(paymentStatus);
        return ResponseEntity.ok()
                .contentType(APPLICATION_NDJSON)
                .body(streamingResponseBody);
    }

    @Timed
    @GetMapping("/reset-graph-cache")
    public void resetGraph() {
        graphService.resetCache();
    }
}
