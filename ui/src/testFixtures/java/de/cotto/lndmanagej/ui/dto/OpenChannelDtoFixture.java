package de.cotto.lndmanagej.ui.dto;

import de.cotto.lndmanagej.controller.dto.PoliciesDto;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES_FOR_LOCAL_CHANNEL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.ui.dto.BalanceInformationModelFixture.BALANCE_INFORMATION_MODEL;
import static de.cotto.lndmanagej.ui.dto.BalanceInformationModelFixture.BALANCE_INFORMATION_MODEL_2;

public class OpenChannelDtoFixture {

    public static final long CAPACITY_SAT = 21_000_000;

    public static final OpenChannelDto OPEN_CHANNEL_DTO = new OpenChannelDto(
            CHANNEL_ID,
            "Albert",
            PUBKEY,
            PoliciesDto.createFromModel(POLICIES_FOR_LOCAL_CHANNEL),
            BALANCE_INFORMATION_MODEL,
            CAPACITY_SAT);

    public static final OpenChannelDto OPEN_CHANNEL_DTO2 = new OpenChannelDto(
            CHANNEL_ID_2,
            "Albert II",
            PUBKEY_2,
            PoliciesDto.createFromModel(POLICIES_FOR_LOCAL_CHANNEL),
            BALANCE_INFORMATION_MODEL_2,
            CAPACITY_SAT);

}
