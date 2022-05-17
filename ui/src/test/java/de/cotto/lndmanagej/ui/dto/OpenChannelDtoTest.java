package de.cotto.lndmanagej.ui.dto;

import de.cotto.lndmanagej.controller.dto.BalanceInformationDto;
import de.cotto.lndmanagej.controller.dto.PoliciesDto;
import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.Coins;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.controller.dto.OpenChannelDtoFixture.CAPACITY_SAT;
import static de.cotto.lndmanagej.controller.dto.OpenChannelDtoFixture.OPEN_CHANNEL_DTO;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES_FOR_LOCAL_CHANNEL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;

class OpenChannelDtoTest {
    @Test
    void channelId() {
        assertThat(OPEN_CHANNEL_DTO.channelId()).isEqualTo(CHANNEL_ID);
    }

    @Test
    void remoteAlias() {
        assertThat(OPEN_CHANNEL_DTO.remoteAlias()).isEqualTo("Albert");
    }

    @Test
    void remotePubkey() {
        assertThat(OPEN_CHANNEL_DTO.remotePubkey()).isEqualTo(PUBKEY);
    }

    @Test
    void policies() {
        assertThat(OPEN_CHANNEL_DTO.policies()).isEqualTo(PoliciesDto.createFromModel(POLICIES_FOR_LOCAL_CHANNEL));
    }

    @Test
    void capacitySat() {
        assertThat(OPEN_CHANNEL_DTO.capacitySat()).isEqualTo(21_000_000);
    }

    @Test
    void balanceInformation() {
        assertThat(OPEN_CHANNEL_DTO.balanceInformation())
                .isEqualTo(BalanceInformationDto.createFromModel(BALANCE_INFORMATION));
    }

    @Test
    void getRatio() {
        assertThat(OPEN_CHANNEL_DTO.getRatio()).isEqualTo("· · | · · · · · · · · · · · · · · · · ·");
    }

    @Test
    void getRatio_no_dots_left_of_bar() {
        BalanceInformation balanceInformation = new BalanceInformation(
                Coins.ofSatoshis(199),
                Coins.NONE,
                Coins.ofSatoshis(1),
                Coins.NONE
        );
        assertThat(dtoWithBalance(balanceInformation).getRatio()).isEqualTo(" | · · · · · · · · · · · · · · · · · · ·");
    }

    private OpenChannelDto dtoWithBalance(BalanceInformation balanceInformation) {
        return new OpenChannelDto(
                CHANNEL_ID,
                "Albert",
                PUBKEY,
                PoliciesDto.createFromModel(POLICIES_FOR_LOCAL_CHANNEL),
                BalanceInformationDto.createFromModel(balanceInformation),
                CAPACITY_SAT
        );
    }
}
