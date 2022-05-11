package de.cotto.lndmanagej.ui.demo.utils;

import de.cotto.lndmanagej.controller.dto.BalanceInformationDto;
import de.cotto.lndmanagej.controller.dto.PoliciesDto;
import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.PoliciesForLocalChannel;
import de.cotto.lndmanagej.model.Policy;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;

import java.util.Random;

public final class ChannelDataUtil {

    private ChannelDataUtil() {
        // util class
    }

    public static OpenChannelDto createOpenChannel(
            String compactChannelId,
            String alias,
            String pubkey,
            long local,
            long remote
    ) {
        ChannelId channelId = ChannelId.fromCompactForm(compactChannelId);
        return new OpenChannelDto(
                channelId,
                alias,
                Pubkey.create(pubkey),
                PoliciesDto.createFromModel(derivePolicies(channelId)),
                BalanceInformationDto.createFromModel(new BalanceInformation(
                        Coins.ofSatoshis(local),
                        Coins.ofSatoshis(200),
                        Coins.ofSatoshis(remote),
                        Coins.ofSatoshis(500)
                )));
    }

    private static PoliciesForLocalChannel derivePolicies(ChannelId channelId) {
        Random generator = new Random(channelId.getShortChannelId());
        return new PoliciesForLocalChannel(derivePolicy(generator), derivePolicy(generator));
    }

    private static Policy derivePolicy(Random generator) {
        long feeRate = generator.nextLong(100) * 10;
        Coins baseFee = Coins.ofMilliSatoshis(generator.nextLong(2) * 1000);
        boolean enabled = generator.nextInt(10) == 0;
        int timeLockDelta = (generator.nextInt(5) + 1) * 10;
        return new Policy(feeRate, baseFee, enabled, timeLockDelta);
    }

}
