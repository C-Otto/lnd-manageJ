package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.ChannelStatus;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.OpenInitiator;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS;
import static de.cotto.lndmanagej.model.OpenCloseStatus.OPEN;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;

class ChannelDetailsDtoTest {

    private static final OnChainCostsDto ON_CHAIN_COSTS = new OnChainCostsDto(Coins.ofSatoshis(1), Coins.ofSatoshis(2));
    private static final ClosedChannelDetailsDto CLOSE_DETAILS = new ClosedChannelDetailsDto("abc", 123);
    private static final FeeReportDto FEE_REPORT =
            new FeeReportDto(Coins.ofMilliSatoshis(1234), Coins.ofMilliSatoshis(567));
    private static final ChannelDetailsDto CHANNEL_DETAILS_DTO = new ChannelDetailsDto(
            CLOSED_CHANNEL,
            ALIAS,
            BALANCE_INFORMATION,
            ON_CHAIN_COSTS,
            PoliciesDto.EMPTY,
            CLOSE_DETAILS,
            FEE_REPORT
    );

    @Test
    void channelIdShort() {
        assertThat(CHANNEL_DETAILS_DTO.channelIdShort()).isEqualTo(String.valueOf(CHANNEL_ID.getShortChannelId()));
    }

    @Test
    void channelIdCompact() {
        assertThat(CHANNEL_DETAILS_DTO.channelIdCompact()).isEqualTo(CHANNEL_ID.getCompactForm());
    }

    @Test
    void channelIdCompactLnd() {
        assertThat(CHANNEL_DETAILS_DTO.channelIdCompactLnd()).isEqualTo(CHANNEL_ID.getCompactFormLnd());
    }

    @Test
    void channelPoint() {
        assertThat(CHANNEL_DETAILS_DTO.channelPoint()).isEqualTo(CHANNEL_POINT);
    }

    @Test
    void openHeight() {
        assertThat(CHANNEL_DETAILS_DTO.openHeight()).isEqualTo(CHANNEL_ID.getBlockHeight());
    }

    @Test
    void remotePubkey() {
        assertThat(CHANNEL_DETAILS_DTO.remotePubkey()).isEqualTo(PUBKEY_2);
    }

    @Test
    void remoteAlias() {
        assertThat(CHANNEL_DETAILS_DTO.remoteAlias()).isEqualTo(ALIAS);
    }

    @Test
    void capacity() {
        assertThat(CHANNEL_DETAILS_DTO.capacity()).isEqualTo(String.valueOf(CAPACITY.satoshis()));
    }

    @Test
    void openInitiator() {
        assertThat(CHANNEL_DETAILS_DTO.openInitiator()).isEqualTo(OpenInitiator.LOCAL);
    }

    @Test
    void status() {
        ChannelDetailsDto dto = new ChannelDetailsDto(
                LOCAL_OPEN_CHANNEL,
                ALIAS,
                BALANCE_INFORMATION,
                ON_CHAIN_COSTS,
                PoliciesDto.EMPTY,
                CLOSE_DETAILS,
                FEE_REPORT
        );
        ChannelStatusDto channelStatusDto =
                ChannelStatusDto.createFromModel(new ChannelStatus(false, true, false, OPEN));
        assertThat(dto.status()).isEqualTo(channelStatusDto);
    }

    @Test
    void closeDetails() {
        assertThat(CHANNEL_DETAILS_DTO.closeDetails()).isEqualTo(CLOSE_DETAILS);
    }

    @Test
    void balance() {
        assertThat(CHANNEL_DETAILS_DTO.balance()).isEqualTo(BalanceInformationDto.createFromModel(BALANCE_INFORMATION));
    }

    @Test
    void feeReport() {
        assertThat(CHANNEL_DETAILS_DTO.feeReport()).isEqualTo(FEE_REPORT);
    }

    @Test
    void onChainCosts() {
        assertThat(CHANNEL_DETAILS_DTO.onChainCosts()).isEqualTo(ON_CHAIN_COSTS);
    }
}