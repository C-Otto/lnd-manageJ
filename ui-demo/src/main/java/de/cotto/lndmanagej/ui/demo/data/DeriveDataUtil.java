package de.cotto.lndmanagej.ui.demo.data;

import de.cotto.lndmanagej.controller.dto.ChannelStatusDto;
import de.cotto.lndmanagej.controller.dto.FeeReportDto;
import de.cotto.lndmanagej.controller.dto.FlowReportDto;
import de.cotto.lndmanagej.controller.dto.OnChainCostsDto;
import de.cotto.lndmanagej.controller.dto.RebalanceReportDto;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ChannelStatus;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FeeReport;
import de.cotto.lndmanagej.model.FlowReport;
import de.cotto.lndmanagej.model.OpenInitiator;
import de.cotto.lndmanagej.model.PoliciesForLocalChannel;
import de.cotto.lndmanagej.model.Policy;
import de.cotto.lndmanagej.model.RebalanceReport;

import java.util.Random;
import java.util.random.RandomGenerator;

import static de.cotto.lndmanagej.model.OpenCloseStatus.OPEN;
import static de.cotto.lndmanagej.model.OpenInitiator.LOCAL;
import static de.cotto.lndmanagej.model.OpenInitiator.REMOTE;

@SuppressWarnings("PMD.TooManyMethods")
public final class DeriveDataUtil {

    private static final Coins MAX_HTLC = Coins.ofSatoshis(1_000_000);

    private DeriveDataUtil() {
        // util class
    }

    private static RandomGenerator createRandomGenerator(long seed) {
        return new Random(seed);
    }

    static RebalanceReportDto deriveRebalanceReport(ChannelId channelId) {
        RandomGenerator rand = createRandomGenerator(channelId.getShortChannelId());
        return RebalanceReportDto.createFromModel(new RebalanceReport(
                Coins.ofSatoshis(rand.nextInt(5000)),
                Coins.ofSatoshis(rand.nextInt(1000)),
                Coins.ofSatoshis(rand.nextInt(5000)),
                Coins.ofSatoshis(rand.nextInt(2000)),
                Coins.ofSatoshis(rand.nextInt(3000)),
                Coins.ofSatoshis(rand.nextInt(500))
        ));
    }

    static FlowReportDto deriveFlowReport(ChannelId channelId) {
        RandomGenerator rand = createRandomGenerator(channelId.getShortChannelId());
        FlowReport flowReport = new FlowReport(
                Coins.ofSatoshis(rand.nextLong(100_000)),
                Coins.ofSatoshis(rand.nextLong(100_000)),
                Coins.ofSatoshis(rand.nextLong(100)),
                Coins.ofSatoshis(rand.nextLong(100)),
                Coins.ofSatoshis(rand.nextLong(10)),
                Coins.ofSatoshis(rand.nextLong(100)),
                Coins.ofSatoshis(rand.nextLong(200)),
                Coins.ofSatoshis(rand.nextLong(10)),
                Coins.ofSatoshis(rand.nextLong(1000))
        );
        return FlowReportDto.createFromModel(flowReport);
    }

    static FeeReportDto deriveFeeReport(ChannelId channelId) {
        RandomGenerator rand = createRandomGenerator(channelId.getShortChannelId());
        long earned = rand.nextLong(1_000_000);
        long sourced = rand.nextLong(100_000);
        return FeeReportDto.createFromModel(
                new FeeReport(Coins.ofMilliSatoshis(earned), Coins.ofMilliSatoshis(sourced)));
    }

    static OnChainCostsDto deriveOnChainCosts(ChannelId channelId) {
        RandomGenerator rand = createRandomGenerator(channelId.getShortChannelId());
        return new OnChainCostsDto(
                String.valueOf(rand.nextLong(2000)),
                String.valueOf(rand.nextLong(2000)),
                String.valueOf(rand.nextLong(3000))
        );
    }

    static OpenInitiator deriveOpenInitiator(ChannelId channelId) {
        boolean local = createRandomGenerator(channelId.getShortChannelId()).nextInt(10) <= 3;
        return local ? LOCAL : REMOTE;
    }

    static PoliciesForLocalChannel derivePolicies(ChannelId channelId) {
        Policy local = derivePolicy(channelId.getShortChannelId() + 1);
        Policy remote = derivePolicy(channelId.getShortChannelId() + 2);
        return new PoliciesForLocalChannel(local, remote);
    }

    static Policy derivePolicy(long randomGeneratorSeed) {
        RandomGenerator rand = createRandomGenerator(randomGeneratorSeed);
        long feeRate = rand.nextLong(100) * 10;
        Coins baseFee = Coins.ofMilliSatoshis(rand.nextLong(2) * 1000);
        boolean enabled = rand.nextInt(10) != 0;
        int timeLockDelta = (rand.nextInt(5) + 1) * 10;
        return new Policy(feeRate, baseFee, enabled, timeLockDelta, MAX_HTLC);
    }

    static ChannelStatusDto deriveChannelStatus(ChannelId channelId) {
        boolean privateChannel = createRandomGenerator(channelId.getShortChannelId()).nextInt(10) <= 1;
        return ChannelStatusDto.createFromModel(new ChannelStatus(privateChannel, true, false, OPEN));
    }

    static long deriveRating(ChannelId channelId) {
        return createRandomGenerator(channelId.getShortChannelId()).nextInt(100_000);
    }

}
