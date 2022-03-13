package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.MissionControlEntry;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.stereotype.Component;
import routerrpc.RouterOuterClass;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.grpc.ByteStringConverter.toHexString;

@Component
public class GrpcMissionControl {
    private final GrpcRouterService grpcRouterService;

    public GrpcMissionControl(GrpcRouterService grpcRouterService) {
        this.grpcRouterService = grpcRouterService;
    }

    public Optional<Set<MissionControlEntry>> getEntries() {
        RouterOuterClass.QueryMissionControlResponse missionControlResponse =
                grpcRouterService.queryMissionControl().orElse(null);
        if (missionControlResponse == null) {
            return Optional.empty();
        }
        Set<MissionControlEntry> result = new LinkedHashSet<>();
        for (RouterOuterClass.PairHistory pairHistory : missionControlResponse.getPairsList()) {
            Pubkey source = Pubkey.create(toHexString(pairHistory.getNodeFrom()));
            Pubkey target = Pubkey.create(toHexString(pairHistory.getNodeTo()));
            RouterOuterClass.PairData history = pairHistory.getHistory();
            getFailure(source, target, history).ifPresent(result::add);
            getSuccess(source, target, history).ifPresent(result::add);
        }
        return Optional.of(result);
    }

    private Optional<MissionControlEntry> getFailure(Pubkey source, Pubkey target, RouterOuterClass.PairData pairData) {
        Coins failAmount = Coins.ofMilliSatoshis(pairData.getFailAmtMsat());
        if (failAmount.isPositive()) {
            return Optional.of(entry(source, target, failAmount, pairData.getFailTime(), true));
        }
        return Optional.empty();
    }

    private Optional<MissionControlEntry> getSuccess(Pubkey source, Pubkey target, RouterOuterClass.PairData pairData) {
        Coins successAmount = Coins.ofMilliSatoshis(pairData.getSuccessAmtMsat());
        if (successAmount.isPositive()) {
            return Optional.of(entry(source, target, successAmount, pairData.getSuccessTime(), false));
        }
        return Optional.empty();
    }

    private MissionControlEntry entry(Pubkey source, Pubkey target, Coins amount, long time, boolean failure) {
        return new MissionControlEntry(
                source,
                target,
                amount,
                Instant.ofEpochSecond(time),
                failure
        );
    }
}
