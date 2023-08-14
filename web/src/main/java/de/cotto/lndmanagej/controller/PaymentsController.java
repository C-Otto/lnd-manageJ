package de.cotto.lndmanagej.controller;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.controller.dto.MultiPathPaymentDto;
import de.cotto.lndmanagej.controller.dto.PaymentOptionsDto;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.pickhardtpayments.MultiPathPaymentSender;
import de.cotto.lndmanagej.pickhardtpayments.MultiPathPaymentSplitter;
import de.cotto.lndmanagej.pickhardtpayments.TopUpService;
import de.cotto.lndmanagej.pickhardtpayments.model.InstantWithString;
import de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPayment;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentOptions;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentStatus;
import de.cotto.lndmanagej.service.GraphService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_NDJSON;

@RestController
@RequestMapping("/api/payments/")
public class PaymentsController {
    private static final PaymentOptionsDto PAYMENT_OPTIONS_DTO = PaymentOptionsDto.DEFAULT;

    private final MultiPathPaymentSplitter multiPathPaymentSplitter;
    private final MultiPathPaymentSender multiPathPaymentSender;
    private final TopUpService topUpService;
    private final GraphService graphService;

    public PaymentsController(
            MultiPathPaymentSplitter multiPathPaymentSplitter,
            MultiPathPaymentSender multiPathPaymentSender,
            TopUpService topUpService,
            GraphService graphService
    ) {
        this.multiPathPaymentSplitter = multiPathPaymentSplitter;
        this.multiPathPaymentSender = multiPathPaymentSender;
        this.topUpService = topUpService;
        this.graphService = graphService;
    }

    @Timed
    @GetMapping("/pay-payment-request/{paymentRequest}")
    public ResponseEntity<Flux<String>> payPaymentRequest(@PathVariable String paymentRequest) {
        return payPaymentRequest(paymentRequest, PAYMENT_OPTIONS_DTO);
    }

    @Timed
    @PostMapping("/pay-payment-request/{paymentRequest}")
    public ResponseEntity<Flux<String>> payPaymentRequest(
            @PathVariable String paymentRequest,
            @RequestBody PaymentOptionsDto paymentOptionsDto
    ) {
        PaymentOptions paymentOptions = paymentOptionsDto.toModel();
        PaymentStatus paymentStatus = multiPathPaymentSender.payPaymentRequest(paymentRequest, paymentOptions);
        return toStream(paymentStatus);
    }

    @Timed
    @GetMapping("/to/{pubkey}/amount/{amount}")
    public MultiPathPaymentDto sendTo(
            @PathVariable Pubkey pubkey,
            @PathVariable long amount
    ) {
        return sendTo(pubkey, amount, PAYMENT_OPTIONS_DTO);
    }

    @Timed
    @PostMapping("/to/{pubkey}/amount/{amount}")
    public MultiPathPaymentDto sendTo(
            @PathVariable Pubkey pubkey,
            @PathVariable long amount,
            @RequestBody PaymentOptionsDto paymentOptionsDto
    ) {
        Coins coins = Coins.ofSatoshis(amount);
        PaymentOptions paymentOptions = paymentOptionsDto.toModel();
        int finalCltvDelta = 0;
        MultiPathPayment multiPathPaymentTo =
                multiPathPaymentSplitter.getMultiPathPaymentTo(pubkey, coins, paymentOptions, finalCltvDelta);
        return MultiPathPaymentDto.fromModel(multiPathPaymentTo);
    }

    @Timed
    @GetMapping("/from/{source}/to/{target}/amount/{amount}")
    public MultiPathPaymentDto send(
            @PathVariable Pubkey source,
            @PathVariable Pubkey target,
            @PathVariable long amount
    ) {
        return send(source, target, amount, PAYMENT_OPTIONS_DTO);
    }

    @Timed
    @PostMapping("/from/{source}/to/{target}/amount/{amount}")
    public MultiPathPaymentDto send(
            @PathVariable Pubkey source,
            @PathVariable Pubkey target,
            @PathVariable long amount,
            @RequestBody PaymentOptionsDto paymentOptionsDto
    ) {
        Coins coins = Coins.ofSatoshis(amount);
        PaymentOptions paymentOptions = paymentOptionsDto.toModel();
        int finalCltvDelta = 0;
        MultiPathPayment multiPathPayment =
                multiPathPaymentSplitter.getMultiPathPayment(source, target, coins, paymentOptions, finalCltvDelta);
        return MultiPathPaymentDto.fromModel(multiPathPayment);
    }

    @Timed
    @GetMapping("/top-up/{pubkey}/amount/{amount}")
    public ResponseEntity<Flux<String>> topUp(@PathVariable Pubkey pubkey, @PathVariable long amount) {
        return topUp(pubkey, amount, new PaymentOptionsDto());
    }

    @Timed
    @GetMapping("/top-up/{pubkey}/amount/{amount}/via/{peerForFirstHop}")
    public ResponseEntity<Flux<String>> topUp(
            @PathVariable Pubkey pubkey,
            @PathVariable long amount,
            @PathVariable Pubkey peerForFirstHop
    ) {
        return topUp(pubkey, amount, peerForFirstHop, new PaymentOptionsDto());
    }

    @Timed
    @PostMapping("/top-up/{pubkey}/amount/{amount}")
    public ResponseEntity<Flux<String>> topUp(
            @PathVariable Pubkey pubkey,
            @PathVariable long amount,
            @RequestBody PaymentOptionsDto paymentOptionsDto
    ) {
        PaymentStatus paymentStatus =
                topUpService.topUp(pubkey, Optional.empty(), Coins.ofSatoshis(amount), paymentOptionsDto.toModel());
        return toStream(paymentStatus);
    }

    @Timed
    @PostMapping("/top-up/{pubkey}/amount/{amount}/via/{peerForFirstHop}")
    public ResponseEntity<Flux<String>> topUp(
            @PathVariable Pubkey pubkey,
            @PathVariable long amount,
            @PathVariable Pubkey peerForFirstHop,
            @RequestBody PaymentOptionsDto paymentOptionsDto
    ) {
        PaymentStatus paymentStatus = topUpService.topUp(
                pubkey,
                Optional.of(peerForFirstHop),
                Coins.ofSatoshis(amount),
                paymentOptionsDto.toModel()
        );
        return toStream(paymentStatus);
    }

    private ResponseEntity<Flux<String>> toStream(PaymentStatus paymentStatus) {
        return ResponseEntity.ok()
                .contentType(APPLICATION_NDJSON)
                .body(paymentStatus.map(this::asString));
    }

    private String asString(InstantWithString instantWithString) {
        return "{\"timestamp\":\"%s\",\"message\":\"%s\"}\n"
                .formatted(instantWithString.instant(), instantWithString.string());
    }

    @Timed
    @GetMapping("/reset-graph-cache")
    public void resetGraph() {
        graphService.resetCache();
    }
}
