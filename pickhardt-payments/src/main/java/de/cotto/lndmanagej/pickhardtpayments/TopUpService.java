package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.grpc.GrpcInvoices;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.DecodedPaymentRequest;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentOptions;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentStatus;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.PolicyService;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

import static de.cotto.lndmanagej.configuration.TopUpConfigurationSettings.EXPIRY;
import static de.cotto.lndmanagej.configuration.TopUpConfigurationSettings.THRESHOLD;

@Component
public class TopUpService {
    private static final Coins DEFAULT_THRESHOLD = Coins.ofSatoshis(10_000);
    private static final Integer DEFAULT_EXPIRY = 30 * 60;

    private final BalanceService balanceService;
    private final GrpcInvoices grpcInvoices;
    private final NodeService nodeService;
    private final ConfigurationService configurationService;
    private final ChannelService channelService;
    private final MultiPathPaymentSender multiPathPaymentSender;
    private final PolicyService policyService;

    public TopUpService(
            BalanceService balanceService,
            GrpcInvoices grpcInvoices,
            NodeService nodeService,
            ConfigurationService configurationService,
            ChannelService channelService,
            MultiPathPaymentSender multiPathPaymentSender,
            PolicyService policyService
    ) {
        this.balanceService = balanceService;
        this.grpcInvoices = grpcInvoices;
        this.nodeService = nodeService;
        this.configurationService = configurationService;
        this.channelService = channelService;
        this.multiPathPaymentSender = multiPathPaymentSender;
        this.policyService = policyService;
    }

    public PaymentStatus topUp(Pubkey pubkey, Coins amount) {
        if (noChannelWith(pubkey)) {
            String alias = nodeService.getAlias(pubkey);
            return PaymentStatus.createFailure("No channel with %s (%s)".formatted(pubkey, alias));
        }

        Coins localBalance = balanceService.getAvailableLocalBalanceForPeer(pubkey);
        Coins topUpAmount = amount.subtract(localBalance);
        Coins threshold = getThreshold();
        if (topUpAmount.compareTo(threshold) < 0) {
            String reason = "Amount %s below threshold %s (balance %s)".formatted(topUpAmount, threshold, localBalance);
            return PaymentStatus.createFailure(reason);
        }

        Coins remoteBalance = balanceService.getAvailableRemoteBalanceForPeer(pubkey);
        if (topUpAmount.compareTo(remoteBalance) > 0) {
            String reason = "Amount %s above remote balance %s".formatted(topUpAmount, remoteBalance);
            return PaymentStatus.createFailure(reason);
        }

        return sendPayment(pubkey, topUpAmount);
    }

    private PaymentStatus sendPayment(Pubkey pubkey, Coins topUpAmount) {
        long ourFeeRate = policyService.getMinimumFeeRateTo(pubkey).orElse(0L);
        long peerFeeRate = policyService.getMinimumFeeRateFrom(pubkey).orElse(0L);
        if (peerFeeRate >= ourFeeRate) {
            String failureReason = "Peer charges too much: %s >= %s (%s, %s)"
                    .formatted(peerFeeRate, ourFeeRate, pubkey, nodeService.getAlias(pubkey));
            return PaymentStatus.createFailure(failureReason);
        }

        DecodedPaymentRequest paymentRequest = getPaymentRequest(pubkey, topUpAmount).orElse(null);
        if (paymentRequest == null) {
            String alias = nodeService.getAlias(pubkey);
            return PaymentStatus.createFailure("Unable to create payment request (%s, %s)".formatted(pubkey, alias));
        }
        PaymentOptions paymentOptions = PaymentOptions.forTopUp(ourFeeRate, pubkey);
        return multiPathPaymentSender.payPaymentRequest(paymentRequest, paymentOptions);
    }

    private Coins getThreshold() {
        return configurationService.getIntegerValue(THRESHOLD).map(Coins::ofSatoshis).orElse(DEFAULT_THRESHOLD);
    }

    private boolean noChannelWith(Pubkey pubkey) {
        return channelService.getOpenChannelsWith(pubkey).isEmpty();
    }

    private Optional<DecodedPaymentRequest> getPaymentRequest(Pubkey pubkey, Coins topUpAmount) {
        String description = getDescription(pubkey);
        return grpcInvoices.createPaymentRequest(topUpAmount, description, getExpiry());
    }

    private Duration getExpiry() {
        return Duration.ofSeconds(configurationService.getIntegerValue(EXPIRY).orElse(DEFAULT_EXPIRY));
    }

    private String getDescription(Pubkey pubkey) {
        String alias = nodeService.getAlias(pubkey);
        ChannelId channelId = channelService.getOpenChannelsWith(pubkey).iterator().next().getId();
        return "Topping up channel %s with %s (%s)".formatted(channelId, pubkey, alias);
    }
}
