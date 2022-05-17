package de.cotto.lndmanagej.controller;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.controller.dto.MultiPathPaymentDto;
import de.cotto.lndmanagej.grpc.GrpcGraph;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.pickhardtpayments.MultiPathPaymentSender;
import de.cotto.lndmanagej.pickhardtpayments.MultiPathPaymentSplitter;
import de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPayment;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import static de.cotto.lndmanagej.pickhardtpayments.PickhardtPaymentsConfiguration.DEFAULT_FEE_RATE_WEIGHT;
import static org.springframework.http.MediaType.APPLICATION_NDJSON;

@RestController
@RequestMapping("/beta/pickhardt-payments/")
public class PickhardtPaymentsController {
    private final MultiPathPaymentSplitter multiPathPaymentSplitter;
    private final MultiPathPaymentSender multiPathPaymentSender;
    private final PaymentStatusStream paymentStatusStream;
    private final GrpcGraph grpcGraph;

    public PickhardtPaymentsController(
            MultiPathPaymentSplitter multiPathPaymentSplitter,
            MultiPathPaymentSender multiPathPaymentSender,
            PaymentStatusStream paymentStatusStream,
            GrpcGraph grpcGraph
    ) {
        this.multiPathPaymentSplitter = multiPathPaymentSplitter;
        this.multiPathPaymentSender = multiPathPaymentSender;
        this.paymentStatusStream = paymentStatusStream;
        this.grpcGraph = grpcGraph;
    }

    @Timed
    @GetMapping("/pay-payment-request/{paymentRequest}")
    public ResponseEntity<StreamingResponseBody> payPaymentRequest(@PathVariable String paymentRequest) {
        return payPaymentRequest(paymentRequest, DEFAULT_FEE_RATE_WEIGHT);
    }

    @Timed
    @GetMapping("/pay-payment-request/{paymentRequest}/fee-rate-weight/{feeRateWeight}")
    public ResponseEntity<StreamingResponseBody> payPaymentRequest(
            @PathVariable String paymentRequest,
            @PathVariable int feeRateWeight
    ) {
        PaymentStatus paymentStatus = multiPathPaymentSender.payPaymentRequest(paymentRequest, feeRateWeight);
        StreamingResponseBody streamingResponseBody = paymentStatusStream.getFor(paymentStatus);
        return ResponseEntity.ok()
                .contentType(APPLICATION_NDJSON)
                .body(streamingResponseBody);
    }

    @Timed
    @GetMapping("/to/{pubkey}/amount/{amount}/fee-rate-weight/{feeRateWeight}")
    public MultiPathPaymentDto sendTo(
            @PathVariable Pubkey pubkey,
            @PathVariable long amount,
            @PathVariable int feeRateWeight
    ) {
        Coins coins = Coins.ofSatoshis(amount);
        MultiPathPayment multiPathPaymentTo =
                multiPathPaymentSplitter.getMultiPathPaymentTo(pubkey, coins, feeRateWeight);
        return MultiPathPaymentDto.fromModel(multiPathPaymentTo);
    }

    @Timed
    @GetMapping("/to/{pubkey}/amount/{amount}")
    public MultiPathPaymentDto sendTo(
            @PathVariable Pubkey pubkey,
            @PathVariable long amount
    ) {
        return sendTo(pubkey, amount, DEFAULT_FEE_RATE_WEIGHT);
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
        MultiPathPayment multiPathPayment =
                multiPathPaymentSplitter.getMultiPathPayment(source, target, coins, feeRateWeight);
        return MultiPathPaymentDto.fromModel(multiPathPayment);
    }

    @Timed
    @GetMapping("/from/{source}/to/{target}/amount/{amount}")
    public MultiPathPaymentDto send(
            @PathVariable Pubkey source,
            @PathVariable Pubkey target,
            @PathVariable long amount
    ) {
        return send(source, target, amount, DEFAULT_FEE_RATE_WEIGHT);
    }

    @Timed
    @GetMapping("/reset-graph-cache")
    public void resetGraph() {
        grpcGraph.resetCache();
    }
}
