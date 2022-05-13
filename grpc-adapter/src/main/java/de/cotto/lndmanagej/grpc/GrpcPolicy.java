package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Policy;
import lnrpc.RoutingPolicy;
import org.springframework.stereotype.Component;

@Component
public class GrpcPolicy {
    public GrpcPolicy() {
        // default constructor
    }

    public Policy toPolicy(RoutingPolicy routingPolicy) {
        return new Policy(
                routingPolicy.getFeeRateMilliMsat(),
                Coins.ofMilliSatoshis(routingPolicy.getFeeBaseMsat()),
                !routingPolicy.getDisabled(),
                routingPolicy.getTimeLockDelta(),
                Coins.ofMilliSatoshis(routingPolicy.getMaxHtlcMsat())
        );
    }
}
