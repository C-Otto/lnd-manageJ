package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcChannelPolicy;
import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.FailureCode;
import de.cotto.lndmanagej.model.HexString;
import de.cotto.lndmanagej.model.PaymentAttemptHop;
import de.cotto.lndmanagej.model.PaymentListener;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static de.cotto.lndmanagej.model.FailureCode.TEMPORARY_CHANNEL_FAILURE;

@Service
public class LiquidityInformationUpdater implements PaymentListener {
    private final GrpcGetInfo grpcGetInfo;
    private final GrpcChannelPolicy grpcChannelPolicy;
    private final LiquidityBoundsService liquidityBoundsService;

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
        if (!TEMPORARY_CHANNEL_FAILURE.equals(failureCode)) {
            return;
        }
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
                liquidityBoundsService.markAsUnavailable(startNode, endNode, hop.amount());
                return;
            }
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
