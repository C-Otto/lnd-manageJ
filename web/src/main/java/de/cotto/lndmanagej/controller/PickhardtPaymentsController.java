package de.cotto.lndmanagej.controller;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.controller.dto.MultiPathPaymentDto;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.pickhardtpayments.MultiPathPaymentSplitter;
import de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPayment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static de.cotto.lndmanagej.pickhardtpayments.PickhardtPaymentsConfiguration.DEFAULT_FEE_RATE_WEIGHT;

@RestController
@RequestMapping("/beta/pickhardt-payments/")
public class PickhardtPaymentsController {
    private final MultiPathPaymentSplitter multiPathPaymentSplitter;

    public PickhardtPaymentsController(MultiPathPaymentSplitter multiPathPaymentSplitter) {
        this.multiPathPaymentSplitter = multiPathPaymentSplitter;
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
}
