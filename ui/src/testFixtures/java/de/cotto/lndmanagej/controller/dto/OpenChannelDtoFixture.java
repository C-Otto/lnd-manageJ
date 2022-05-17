package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.ui.dto.OpenChannelDto;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES_FOR_LOCAL_CHANNEL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;

public class OpenChannelDtoFixture {

    public static final long CAPACITY_SAT = 21_000_000;

    public static final OpenChannelDto OPEN_CHANNEL_DTO = new OpenChannelDto(
            CHANNEL_ID,
            "Albert",
            PUBKEY,
            PoliciesDto.createFromModel(POLICIES_FOR_LOCAL_CHANNEL),
            BalanceInformationDto.createFromModel(BALANCE_INFORMATION),
            CAPACITY_SAT);

    public static final OpenChannelDto OPEN_CHANNEL_DTO2 = new OpenChannelDto(
            CHANNEL_ID_2,
            "Albert II",
            PUBKEY_2,
            PoliciesDto.createFromModel(POLICIES_FOR_LOCAL_CHANNEL),
            BalanceInformationDto.createFromModel(BALANCE_INFORMATION_2),
            CAPACITY_SAT);

}
