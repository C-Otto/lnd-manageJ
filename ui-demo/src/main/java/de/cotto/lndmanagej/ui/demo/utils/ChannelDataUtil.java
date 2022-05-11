package de.cotto.lndmanagej.ui.demo.utils;

import de.cotto.lndmanagej.controller.dto.BalanceInformationDto;
import de.cotto.lndmanagej.controller.dto.PoliciesDto;
import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_4;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_5;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES_FOR_LOCAL_CHANNEL;

public class ChannelDataUtil {

    public static final long MILLION = 1_000_000;

    public static final OpenChannelDto ACINQ = new OpenChannelDto(
            CHANNEL_ID_2,
            "ACINQ",
            Pubkey.create("03864ef025fde8fb587d989186ce6a4a186895ee44a926bfc370e2c366597a3f8f"),
            PoliciesDto.createFromModel(POLICIES_FOR_LOCAL_CHANNEL),
            BalanceInformationDto.createFromModel(new BalanceInformation(
                    Coins.ofSatoshis(MILLION * 5),
                    Coins.ofSatoshis(200),
                    Coins.ofSatoshis(MILLION * 5),
                    Coins.ofSatoshis(500)
            )));

    public static final OpenChannelDto ACINQ2 = new OpenChannelDto(
            ChannelId.fromCompactForm("799999x456x7"),
            "ACINQ",
            Pubkey.create("03864ef025fde8fb587d989186ce6a4a186895ee44a926bfc370e2c366597a3f8f"),
            PoliciesDto.createFromModel(POLICIES_FOR_LOCAL_CHANNEL),
            BalanceInformationDto.createFromModel(new BalanceInformation(
                    Coins.ofSatoshis(100_000),
                    Coins.ofSatoshis(250),
                    Coins.ofSatoshis(MILLION * 10),
                    Coins.ofSatoshis(250)
            )));

    public static final OpenChannelDto WOS = new OpenChannelDto(
            CHANNEL_ID_3,
            "WalletOfSatoshi.com",
            Pubkey.create("035e4ff418fc8b5554c5d9eea66396c227bd429a3251c8cbc711002ba215bfc226"),
            PoliciesDto.createFromModel(POLICIES_FOR_LOCAL_CHANNEL),
            BalanceInformationDto.createFromModel(new BalanceInformation(
                    Coins.ofSatoshis(MILLION * 2),
                    Coins.ofSatoshis(400),
                    Coins.ofSatoshis(MILLION * 10),
                    Coins.ofSatoshis(500)
            )));

    public static final OpenChannelDto WOS2 = new OpenChannelDto(
            CHANNEL_ID_3,
            "WalletOfSatoshi.com",
            Pubkey.create("035e4ff418fc8b5554c5d9eea66396c227bd429a3251c8cbc711002ba215bfc226"),
            PoliciesDto.createFromModel(POLICIES_FOR_LOCAL_CHANNEL),
            BalanceInformationDto.createFromModel(new BalanceInformation(
                    Coins.ofSatoshis(MILLION * 10),
                    Coins.ofSatoshis(400),
                    Coins.ofSatoshis(100_000),
                    Coins.ofSatoshis(500)
            )));

    public static final OpenChannelDto BCASH = new OpenChannelDto(
            CHANNEL_ID_4,
            "BCash_Is_Trash",
            Pubkey.create("0298f6074a454a1f5345cb2a7c6f9fce206cd0bf675d177cdbf0ca7508dd28852f"),
            PoliciesDto.createFromModel(POLICIES_FOR_LOCAL_CHANNEL),
            BalanceInformationDto.createFromModel(new BalanceInformation(
                    Coins.ofSatoshis(MILLION * 10),
                    Coins.ofSatoshis(400),
                    Coins.ofSatoshis(MILLION * 20),
                    Coins.ofSatoshis(500)
            )));

    public static final OpenChannelDto C_OTTO = new OpenChannelDto(
            CHANNEL_ID_5,
            "c-otto.de",
            Pubkey.create("027ce055380348d7812d2ae7745701c9f93e70c1adeb2657f053f91df4f2843c71"),
            PoliciesDto.createFromModel(POLICIES_FOR_LOCAL_CHANNEL),
            BalanceInformationDto.createFromModel(new BalanceInformation(
                    Coins.ofSatoshis(MILLION * 20),
                    Coins.ofSatoshis(400),
                    Coins.ofSatoshis(MILLION * 10),
                    Coins.ofSatoshis(500)
            )));
}
