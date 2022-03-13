package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.MissionControlEntry;
import de.cotto.lndmanagej.model.Pubkey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import routerrpc.RouterOuterClass;
import routerrpc.RouterOuterClass.QueryMissionControlResponse;

import java.time.Instant;
import java.util.Optional;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_4;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrpcMissionControlTest {
    @InjectMocks
    private GrpcMissionControl grpcMissionControl;

    @Mock
    private GrpcRouterService grpcRouterService;

    @Test
    void empty_optional() {
        assertThat(grpcMissionControl.getEntries()).isEmpty();
    }

    @Test
    void no_entry() {
        QueryMissionControlResponse response = QueryMissionControlResponse.getDefaultInstance();
        when(grpcRouterService.queryMissionControl()).thenReturn(Optional.of(response));
        assertThat(grpcMissionControl.getEntries().orElseThrow()).isEmpty();
    }

    @Test
    void success_entry() {
        int amount = 123;
        int time = 456;
        mockResponse(pair(PUBKEY, PUBKEY_2, success(amount, time)));
        MissionControlEntry expectedEntry = new MissionControlEntry(
                PUBKEY,
                PUBKEY_2,
                Coins.ofMilliSatoshis(amount),
                Instant.ofEpochSecond(time),
                false
        );
        assertThat(grpcMissionControl.getEntries().orElseThrow()).containsExactly(expectedEntry);
    }

    @Test
    void failure_entry() {
        int amount = 789;
        int time = 1_234;
        mockResponse(pair(PUBKEY_3, PUBKEY_4, failure(amount, time)));
        MissionControlEntry expectedEntry = new MissionControlEntry(
                PUBKEY_3,
                PUBKEY_4,
                Coins.ofMilliSatoshis(amount),
                Instant.ofEpochSecond(time),
                true
        );
        assertThat(grpcMissionControl.getEntries().orElseThrow()).containsExactly(expectedEntry);
    }

    @Test
    void failure_and_success() {
        int amount = 789;
        int time = 1_234;
        mockResponse(
                pair(PUBKEY_3, PUBKEY_4, success(amount, time)),
                pair(PUBKEY, PUBKEY_2, failure(amount, time))
        );
        MissionControlEntry expectedEntry1 = new MissionControlEntry(
                PUBKEY_3,
                PUBKEY_4,
                Coins.ofMilliSatoshis(amount),
                Instant.ofEpochSecond(time),
                false
        );
        MissionControlEntry expectedEntry2 = new MissionControlEntry(
                PUBKEY,
                PUBKEY_2,
                Coins.ofMilliSatoshis(amount),
                Instant.ofEpochSecond(time),
                true
        );
        assertThat(grpcMissionControl.getEntries().orElseThrow())
                .containsExactlyInAnyOrder(expectedEntry1, expectedEntry2);
    }

    @Test
    void failure_and_success_same_pair() {
        int failureAmount = 789;
        int successAmount = 123;
        int failureTime = 1_234;
        int successTime = 1_235;
        mockResponse(
                pair(PUBKEY_3, PUBKEY_4, failureAndSuccess(failureAmount, failureTime, successAmount, successTime))
        );
        MissionControlEntry expectedEntry1 = new MissionControlEntry(
                PUBKEY_3,
                PUBKEY_4,
                Coins.ofMilliSatoshis(successAmount),
                Instant.ofEpochSecond(successTime),
                false
        );
        MissionControlEntry expectedEntry2 = new MissionControlEntry(
                PUBKEY_3,
                PUBKEY_4,
                Coins.ofMilliSatoshis(failureAmount),
                Instant.ofEpochSecond(failureTime),
                true
        );
        assertThat(grpcMissionControl.getEntries().orElseThrow())
                .containsExactlyInAnyOrder(expectedEntry1, expectedEntry2);
    }

    private void mockResponse(RouterOuterClass.PairHistory... pairs) {
        QueryMissionControlResponse.Builder builder = QueryMissionControlResponse.newBuilder();
        for (RouterOuterClass.PairHistory pair : pairs) {
            builder.addPairs(pair);
        }
        when(grpcRouterService.queryMissionControl()).thenReturn(Optional.of(builder.build()));
    }

    private RouterOuterClass.PairData failureAndSuccess(
            int failureAmount,
            int failureTime,
            int successAmount,
            int successTime
    ) {
        return RouterOuterClass.PairData.newBuilder()
                .setSuccessAmtMsat(successAmount)
                .setSuccessTime(successTime)
                .setFailAmtMsat(failureAmount)
                .setFailTime(failureTime)
                .build();
    }

    private RouterOuterClass.PairData success(int amount, int time) {
        return RouterOuterClass.PairData.newBuilder()
                .setSuccessAmtMsat(amount)
                .setSuccessTime(time)
                .build();
    }

    private RouterOuterClass.PairData failure(int amount, int time) {
        return RouterOuterClass.PairData.newBuilder()
                .setFailAmtMsat(amount)
                .setFailTime(time)
                .build();
    }

    private RouterOuterClass.PairHistory pair(Pubkey source, Pubkey target, RouterOuterClass.PairData... histories) {
        RouterOuterClass.PairHistory.Builder builder = RouterOuterClass.PairHistory.newBuilder()
                .setNodeFrom(ByteStringConverter.fromHexString(source.toString()))
                .setNodeTo(ByteStringConverter.fromHexString(target.toString()));
        for (RouterOuterClass.PairData history : histories) {
            builder.setHistory(history);
        }
        return builder.build();
    }
}