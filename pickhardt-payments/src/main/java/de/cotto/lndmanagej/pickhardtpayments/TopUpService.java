package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.grpc.GrpcInvoices;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.DecodedPaymentRequest;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentStatus;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.NodeService;
import org.springframework.stereotype.Component;

import static de.cotto.lndmanagej.configuration.TopUpConfigurationSettings.THRESHOLD;

@Component
public class TopUpService {
    private static final Coins DEFAULT_THRESHOLD = Coins.ofSatoshis(10_000);
    private final BalanceService balanceService;
    private final GrpcInvoices grpcInvoices;
    private final NodeService nodeService;
    private final ConfigurationService configurationService;
    private final ChannelService channelService;
    private final MultiPathPaymentSender multiPathPaymentSender;

    public TopUpService(
            BalanceService balanceService,
            GrpcInvoices grpcInvoices,
            NodeService nodeService,
            ConfigurationService configurationService,
            ChannelService channelService,
            MultiPathPaymentSender multiPathPaymentSender
    ) {
        this.balanceService = balanceService;
        this.grpcInvoices = grpcInvoices;
        this.nodeService = nodeService;
        this.configurationService = configurationService;
        this.channelService = channelService;
        this.multiPathPaymentSender = multiPathPaymentSender;
    }

    public PaymentStatus topUp(Pubkey pubkey, Coins amount) {
        Coins balance = balanceService.getAvailableLocalBalanceForPeer(pubkey);
        Coins topUpAmount = amount.subtract(balance);
        Coins threshold = getThreshold();
        if (topUpAmount.compareTo(threshold) < 0) {
            String reason = "Amount %s below threshold %s (balance %s)".formatted(topUpAmount, threshold, balance);
            return PaymentStatus.createFailure(reason);
        }

        if (noChannelWith(pubkey)) {
            String alias = nodeService.getAlias(pubkey);
            return PaymentStatus.createFailure("No channel with " + pubkey + " (" + alias + ")");
        }

        String description = getDescription(pubkey, topUpAmount);
        DecodedPaymentRequest paymentRequest =
                grpcInvoices.createPaymentRequest(topUpAmount, description).orElse(null);
        if (paymentRequest == null) {
            String alias = nodeService.getAlias(pubkey);
            return PaymentStatus.createFailure("Unable to create payment request (%s, %s)".formatted(pubkey, alias));
        }
        return multiPathPaymentSender.payPaymentRequest(paymentRequest, 0);
    }

    private Coins getThreshold() {
        return configurationService.getIntegerValue(THRESHOLD).map(Coins::ofSatoshis).orElse(DEFAULT_THRESHOLD);
    }

    private boolean noChannelWith(Pubkey pubkey) {
        return channelService.getOpenChannelsWith(pubkey).isEmpty();
    }

    private String getDescription(Pubkey pubkey, Coins amount) {
        String alias = nodeService.getAlias(pubkey);
        return "Topping up channel with %s (%s), adding %s".formatted(pubkey, alias, amount);
    }
}
