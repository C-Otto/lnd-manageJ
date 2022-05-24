package de.cotto.lndmanagej.ui.dto;

import de.cotto.lndmanagej.controller.dto.PoliciesDto;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES_FOR_LOCAL_CHANNEL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.ui.dto.OpenChannelDtoFixture.OPEN_CHANNEL_DTO;
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
                .isEqualTo(BalanceInformationModel.createFromModel(BALANCE_INFORMATION));
    }
}
