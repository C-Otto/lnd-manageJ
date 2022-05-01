package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcChannelPolicy;
import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FailureCode;
import de.cotto.lndmanagej.model.HexString;
import de.cotto.lndmanagej.model.PaymentAttemptHop;
import de.cotto.lndmanagej.model.PaymentListener;
import de.cotto.lndmanagej.model.Pubkey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static de.cotto.lndmanagej.model.FailureCode.CHANNEL_DISABLED;
import static de.cotto.lndmanagej.model.FailureCode.INCORRECT_OR_UNKNOWN_PAYMENT_DETAILS;
import static de.cotto.lndmanagej.model.FailureCode.MPP_TIMEOUT;
import static de.cotto.lndmanagej.model.FailureCode.TEMPORARY_CHANNEL_FAILURE;
import static de.cotto.lndmanagej.model.FailureCode.UNKNOWN_NEXT_PEER;

@Service
public class LiquidityInformationUpdater implements PaymentListener {
    private final GrpcGetInfo grpcGetInfo;
    private final GrpcChannelPolicy grpcChannelPolicy;
    private final LiquidityBoundsService liquidityBoundsService;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public LiquidityInformationUpdater(
            GrpcGetInfo grpcGetInfo,
            GrpcChannelPolicy grpcChannelPolicy,
            LiquidityBoundsService liquidityBoundsService
    ) {
        this.grpcGetInfo = grpcGetInfo;
        this.grpcChannelPolicy = grpcChannelPolicy;
        this.liquidityBoundsService = liquidityBoundsService;
    }

    @Override
    public void success(HexString preimage, List<PaymentAttemptHop> paymentAttemptHops) {
        Pubkey startNode = grpcGetInfo.getPubkey();
        for (PaymentAttemptHop hop : paymentAttemptHops) {
            Pubkey endNode = getOtherNode(hop, startNode).orElse(null);
            if (endNode == null) {
                return;
            }
            liquidityBoundsService.markAsMoved(startNode, endNode, hop.amount());
            startNode = endNode;
        }
    }

    @Override
    public void failure(List<PaymentAttemptHop> paymentAttemptHops, FailureCode failureCode, int failureSourceIndex) {
        if (TEMPORARY_CHANNEL_FAILURE.equals(failureCode)) {
            markAvailableAndUnavailable(paymentAttemptHops, failureSourceIndex, PaymentAttemptHop::amount);
        } else if (UNKNOWN_NEXT_PEER.equals(failureCode) || CHANNEL_DISABLED.equals(failureCode)) {
            markAvailableAndUnavailable(paymentAttemptHops, failureSourceIndex, hop -> Coins.ofSatoshis(1));
        } else if (MPP_TIMEOUT.equals(failureCode) || INCORRECT_OR_UNKNOWN_PAYMENT_DETAILS.equals(failureCode)) {
            markAllAvailable(paymentAttemptHops, failureSourceIndex);
        } else {
            logger.warn("Unknown failure code {}", failureCode);
        }
    }

    private void markAvailableAndUnavailable(
            List<PaymentAttemptHop> paymentAttemptHops,
            int failureSourceIndex,
            Function<PaymentAttemptHop, Coins> unavailableAmountForHop
    ) {
        Pubkey startNode = grpcGetInfo.getPubkey();
        for (int i = 0; i < paymentAttemptHops.size(); i++) {
            PaymentAttemptHop hop = paymentAttemptHops.get(i);
            Pubkey endNode = getOtherNode(hop, startNode).orElse(null);
            if (endNode == null) {
                return;
            }
            if (i < failureSourceIndex) {
                liquidityBoundsService.markAsAvailable(startNode, endNode, hop.amount());
            } else {
                liquidityBoundsService.markAsUnavailable(startNode, endNode, unavailableAmountForHop.apply(hop));
                return;
            }
            startNode = endNode;
        }
    }

    private void markAllAvailable(List<PaymentAttemptHop> paymentAttemptHops, int failureSourceIndex) {
        if (failureSourceIndex != paymentAttemptHops.size()) {
            return;
        }
        Pubkey startNode = grpcGetInfo.getPubkey();
        for (PaymentAttemptHop hop : paymentAttemptHops) {
            Pubkey endNode = getOtherNode(hop, startNode).orElse(null);
            if (endNode == null) {
                return;
            }
            liquidityBoundsService.markAsAvailable(startNode, endNode, hop.amount());
            startNode = endNode;
        }
    }

    private Optional<Pubkey> getOtherNode(PaymentAttemptHop hop, Pubkey startNode) {
        if (hop.targetPubkey().isPresent()) {
            return hop.targetPubkey();
        }
        if (hop.channelId().isEmpty()) {
            return Optional.empty();
        }
        ChannelId channelId = hop.channelId().get();
        return grpcChannelPolicy.getOtherPubkey(channelId, startNode);
    }
}
