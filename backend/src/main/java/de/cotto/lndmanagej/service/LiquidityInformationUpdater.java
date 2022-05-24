package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcChannelPolicy;
import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FailureCode;
import de.cotto.lndmanagej.model.HexString;
import de.cotto.lndmanagej.model.LiquidityChangeListener;
import de.cotto.lndmanagej.model.PaymentAttemptHop;
import de.cotto.lndmanagej.model.PaymentListener;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static de.cotto.lndmanagej.model.FailureCode.TEMPORARY_CHANNEL_FAILURE;

@Service
public class LiquidityInformationUpdater implements PaymentListener {
    private final GrpcGetInfo grpcGetInfo;
    private final GrpcChannelPolicy grpcChannelPolicy;
    private final LiquidityBoundsService liquidityBoundsService;
    private final List<LiquidityChangeListener> liquidityChangeListeners;

    public LiquidityInformationUpdater(
            GrpcGetInfo grpcGetInfo,
            GrpcChannelPolicy grpcChannelPolicy,
            LiquidityBoundsService liquidityBoundsService,
            List<LiquidityChangeListener> liquidityChangeListeners
    ) {
        this.grpcGetInfo = grpcGetInfo;
        this.grpcChannelPolicy = grpcChannelPolicy;
        this.liquidityBoundsService = liquidityBoundsService;
        this.liquidityChangeListeners = liquidityChangeListeners;
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
        if (TEMPORARY_CHANNEL_FAILURE.equals(failureCode)) {
            markAvailableAndUnavailable(paymentAttemptHops, failureSourceIndex, PaymentAttemptHop::amount);
        } else if (failureCode.isErrorFromFinalNode()) {
            markAllAvailable(paymentAttemptHops, failureSourceIndex);
        } else {
            markAvailableAndUnavailable(paymentAttemptHops, failureSourceIndex, hop -> Coins.ofSatoshis(2));
        }
    }

    public void removeInFlight(List<PaymentAttemptHop> paymentAttemptHops) {
        updateInFlight(paymentAttemptHops, true);
    }

    private void addInFlight(List<PaymentAttemptHop> paymentAttemptHops) {
        updateInFlight(paymentAttemptHops, false);
    }

    private void updateInFlight(List<PaymentAttemptHop> paymentAttemptHops, boolean negate) {
        Pubkey ownNode = grpcGetInfo.getPubkey();
        Pubkey startNode = ownNode;
        for (PaymentAttemptHop hop : paymentAttemptHops) {
            Pubkey endNode = getOtherNode(hop, startNode).orElse(null);
            if (endNode == null) {
                return;
            }
            if (ownNode.equals(startNode)) {
                liquidityChangeListeners.forEach(l -> l.amountChanged(endNode));
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
