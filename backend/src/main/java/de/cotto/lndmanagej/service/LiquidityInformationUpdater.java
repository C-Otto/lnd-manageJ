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
    public void forNewPaymentAttempt(List<PaymentAttemptHop> paymentAttemptHops) {
        addInFlight(paymentAttemptHops);
    }

    @Override
    public void success(HexString preimage, List<PaymentAttemptHop> paymentAttemptHops) {
        removeInFlight(paymentAttemptHops);
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
        removeInFlight(paymentAttemptHops);
        switch (failureCode) {
            case TEMPORARY_CHANNEL_FAILURE ->
                    markAvailableAndUnavailable(paymentAttemptHops, failureSourceIndex, PaymentAttemptHop::amount);
            case UNKNOWN_NEXT_PEER, CHANNEL_DISABLED, FEE_INSUFFICIENT ->
                    markAvailableAndUnavailable(paymentAttemptHops, failureSourceIndex, hop -> Coins.ofSatoshis(1));
            case MPP_TIMEOUT, INCORRECT_OR_UNKNOWN_PAYMENT_DETAILS ->
                    markAllAvailable(paymentAttemptHops, failureSourceIndex);
            default -> logger.warn("Unknown failure code {}", failureCode);
        }
    }

    public void removeInFlight(List<PaymentAttemptHop> paymentAttemptHops) {
        updateInFlight(paymentAttemptHops, true);
    }

    private void addInFlight(List<PaymentAttemptHop> paymentAttemptHops) {
        updateInFlight(paymentAttemptHops, false);
    }

    private void updateInFlight(List<PaymentAttemptHop> paymentAttemptHops, boolean negate) {
        Pubkey startNode = grpcGetInfo.getPubkey();
        for (PaymentAttemptHop hop : paymentAttemptHops) {
            Pubkey endNode = getOtherNode(hop, startNode).orElse(null);
            if (endNode == null) {
                return;
            }
            Coins amount = hop.amount();
            if (negate) {
                liquidityBoundsService.markAsInFlight(startNode, endNode, amount.negate());
            } else {
                liquidityBoundsService.markAsInFlight(startNode, endNode, amount);
            }
            startNode = endNode;
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
