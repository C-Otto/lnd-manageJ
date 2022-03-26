package de.cotto.lndmanagej.controller;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.controller.dto.MultiPathPaymentDto;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.pickhardtpayments.MultiPathPaymentSplitter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/beta/pickhardt-payments/")
public class PickhardtPaymentsController {
    private final MultiPathPaymentSplitter multiPathPaymentSplitter;

    public PickhardtPaymentsController(MultiPathPaymentSplitter multiPathPaymentSplitter) {
        this.multiPathPaymentSplitter = multiPathPaymentSplitter;
    }

    @Timed
    @GetMapping("/to/{pubkey}/amount/{amount}")
    public MultiPathPaymentDto sendTo(
            @PathVariable Pubkey pubkey,
            @PathVariable long amount
    ) {
        Coins coins = Coins.ofSatoshis(amount);
        return MultiPathPaymentDto.fromModel(multiPathPaymentSplitter.getMultiPathPaymentTo(pubkey, coins));
    }

    @Timed
    @GetMapping("/from/{source}/to/{target}/amount/{amount}")
    public MultiPathPaymentDto send(
            @PathVariable Pubkey source,
            @PathVariable Pubkey target,
            @PathVariable long amount
    ) {
        Coins coins = Coins.ofSatoshis(amount);
        return MultiPathPaymentDto.fromModel(multiPathPaymentSplitter.getMultiPathPayment(source, target, coins));
    }
}
