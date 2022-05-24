package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.LiquidityChangeListener;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.stereotype.Component;

@Component
public class ChannelCacheInvalidator implements LiquidityChangeListener {
    private final GrpcService grpcService;

    public ChannelCacheInvalidator(GrpcService grpcService) {
        this.grpcService = grpcService;
    }

    @Override
    public void amountChanged(Pubkey peer) {
        grpcService.invalidateChannelsCache();
    }
}
