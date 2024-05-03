package de.cotto.lndmanagej.grpc;

import com.google.protobuf.ByteString;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Policy;
import lnrpc.RoutingPolicy;
import org.springframework.stereotype.Component;

import java.nio.IntBuffer;

@Component
public class GrpcPolicy {

    private static final int FEE_RECORD_TYPE = 55_555;

    public GrpcPolicy() {
        // default constructor
    }

    public Policy toPolicy(RoutingPolicy routingPolicy) {
        long inboundFeeRate;
        Coins inboundBaseFee;
        if (routingPolicy.containsCustomRecords(FEE_RECORD_TYPE)) {
            ByteString record = routingPolicy.getCustomRecordsOrThrow(FEE_RECORD_TYPE);
            IntBuffer intBuffer = record.asReadOnlyByteBuffer().asIntBuffer();
            inboundBaseFee = Coins.ofMilliSatoshis(intBuffer.get());
            inboundFeeRate = intBuffer.get();
        } else {
            inboundFeeRate = 0;
            inboundBaseFee = Coins.NONE;
        }

        return new Policy(
                routingPolicy.getFeeRateMilliMsat(),
                Coins.ofMilliSatoshis(routingPolicy.getFeeBaseMsat()),
                inboundFeeRate,
                inboundBaseFee,
                !routingPolicy.getDisabled(),
                routingPolicy.getTimeLockDelta(),
                Coins.ofMilliSatoshis(routingPolicy.getMinHtlc()),
                Coins.ofMilliSatoshis(routingPolicy.getMaxHtlcMsat())
        );
    }
}
