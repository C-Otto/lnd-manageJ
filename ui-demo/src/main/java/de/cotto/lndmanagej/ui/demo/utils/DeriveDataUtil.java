package de.cotto.lndmanagej.ui.demo.utils;

import de.cotto.lndmanagej.controller.dto.FeeReportDto;
import de.cotto.lndmanagej.controller.dto.FlowReportDto;
import de.cotto.lndmanagej.controller.dto.OnChainCostsDto;
import de.cotto.lndmanagej.controller.dto.RebalanceReportDto;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FeeReport;
import de.cotto.lndmanagej.model.FlowReport;
import de.cotto.lndmanagej.model.OpenInitiator;
import de.cotto.lndmanagej.model.PoliciesForLocalChannel;
import de.cotto.lndmanagej.model.Policy;
import de.cotto.lndmanagej.model.RebalanceReport;

import java.util.Random;
import java.util.Set;

import static de.cotto.lndmanagej.model.OpenInitiator.LOCAL;
import static de.cotto.lndmanagej.model.OpenInitiator.REMOTE;

public final class DeriveDataUtil {

    private DeriveDataUtil() {
        // util class
    }

    static RebalanceReportDto deriveRebalanceReport(ChannelId channelId) {
        Random generator = new Random(channelId.getShortChannelId());
        return RebalanceReportDto.createFromModel(new RebalanceReport(
                Coins.ofSatoshis(generator.nextInt(5000)),
                Coins.ofSatoshis(generator.nextInt(1000)),
                Coins.ofSatoshis(generator.nextInt(5000)),
                Coins.ofSatoshis(generator.nextInt(2000)),
                Coins.ofSatoshis(generator.nextInt(3000)),
                Coins.ofSatoshis(generator.nextInt(500))
        ));
    }

    static Set<String> deriveWarnings(ChannelId channelId) {
        Random generator = new Random(channelId.getShortChannelId());
        boolean showWarning = generator.nextInt(10) != 0;
        int updates = (generator.nextInt(10) + 5) * 100_000;
        return showWarning ? Set.of("Channel has accumulated " + updates + " updates.") : Set.of();
    }

    static FlowReportDto deriveFlowReport(ChannelId channelId) {
        Random generator = new Random(channelId.getShortChannelId());
        FlowReport flowReport = new FlowReport(
                Coins.ofSatoshis(generator.nextLong(100_000)),
                Coins.ofSatoshis(generator.nextLong(100_000)),
                Coins.ofSatoshis(generator.nextLong(100)),
                Coins.ofSatoshis(generator.nextLong(100)),
                Coins.ofSatoshis(generator.nextLong(10)),
                Coins.ofSatoshis(generator.nextLong(100)),
                Coins.ofSatoshis(generator.nextLong(200)),
                Coins.ofSatoshis(generator.nextLong(10)),
                Coins.ofSatoshis(generator.nextLong(1000))
        );
        return FlowReportDto.createFromModel(flowReport);
    }

    static FeeReportDto deriveFeeReport(ChannelId channelId) {
        Random generator = new Random(channelId.getShortChannelId());
        long earned = generator.nextLong(1_000_000);
        long sourced = generator.nextLong(100_000);
        return FeeReportDto.createFromModel(
                new FeeReport(Coins.ofMilliSatoshis(earned), Coins.ofMilliSatoshis(sourced)));
    }

    static OnChainCostsDto deriveOnChainCosts(ChannelId channelId) {
        Random generator = new Random(channelId.getShortChannelId());
        return new OnChainCostsDto(
                String.valueOf(generator.nextLong(2000)),
                String.valueOf(generator.nextLong(2000)),
                String.valueOf(generator.nextLong(3000))
        );
    }

    static OpenInitiator deriveOpenInitiator(ChannelId channelId) {
        Random generator = new Random(channelId.getShortChannelId());
        return generator.nextBoolean() ? LOCAL : REMOTE;
    }

    static PoliciesForLocalChannel derivePolicies(ChannelId channelId) {
        Random generator = new Random(channelId.getShortChannelId());
        return new PoliciesForLocalChannel(derivePolicy(generator), derivePolicy(generator));
    }

    static Policy derivePolicy(Random generator) {
        long feeRate = generator.nextLong(100) * 10;
        Coins baseFee = Coins.ofMilliSatoshis(generator.nextLong(2) * 1000);
        boolean enabled = generator.nextInt(10) == 0;
        int timeLockDelta = (generator.nextInt(5) + 1) * 10;
        return new Policy(feeRate, baseFee, enabled, timeLockDelta);
    }

    static Set<String> deriveChannelWarnings(ChannelId channelId) {
        Random generator = new Random(channelId.getShortChannelId());
        boolean showWarning = generator.nextInt(20) != 0;
        int days = generator.nextInt(30) + 30;
        return showWarning ? Set.of("No flow in the past " + days + " days.") : Set.of();
    }
}
