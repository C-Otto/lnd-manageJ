package de.cotto.lndmanagej.ui.dto;

import de.cotto.lndmanagej.controller.dto.FeeReportDto;
import de.cotto.lndmanagej.controller.dto.FlowReportDto;
import de.cotto.lndmanagej.controller.dto.OnChainCostsDto;
import de.cotto.lndmanagej.controller.dto.PoliciesDto;
import de.cotto.lndmanagej.controller.dto.RatingDto;
import de.cotto.lndmanagej.controller.dto.RebalanceReportDto;
import de.cotto.lndmanagej.model.OpenInitiator;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelRatingFixtures.RATING;
import static de.cotto.lndmanagej.model.FeeReportFixtures.FEE_REPORT;
import static de.cotto.lndmanagej.model.FlowReportFixtures.FLOW_REPORT;
import static de.cotto.lndmanagej.model.OnChainCostsFixtures.ON_CHAIN_COSTS;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES_FOR_LOCAL_CHANNEL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.RebalanceReportFixtures.REBALANCE_REPORT;
import static de.cotto.lndmanagej.model.warnings.ChannelWarningsFixtures.CHANNEL_WARNINGS;
import static de.cotto.lndmanagej.ui.dto.ChannelDetailsDtoFixture.CHANNEL_DETAILS_DTO;
import static org.assertj.core.api.Assertions.assertThat;

class ChannelDetailsDtoTest {
    // CPD-OFF
    @Test
    void channelId() {
        assertThat(CHANNEL_DETAILS_DTO.channelId()).isEqualTo(CHANNEL_ID);
    }

    @Test
    void remotePubkey() {
        assertThat(CHANNEL_DETAILS_DTO.remotePubkey()).isEqualTo(PUBKEY);
    }

    @Test
    void remoteAlias() {
        assertThat(CHANNEL_DETAILS_DTO.remoteAlias()).isEqualTo("Albert");
    }

    @Test
    void openInitiator() {
        assertThat(CHANNEL_DETAILS_DTO.openInitiator()).isEqualTo(OpenInitiator.LOCAL);
    }

    @Test
    void balanceInformation() {
        assertThat(CHANNEL_DETAILS_DTO.balanceInformation())
                .isEqualTo(BalanceInformationModel.createFromModel(BALANCE_INFORMATION));
    }

    @Test
    void capacitySat() {
        assertThat(CHANNEL_DETAILS_DTO.capacitySat()).isEqualTo(21_000_000);
    }

    @Test
    void onChainCosts() {
        assertThat(CHANNEL_DETAILS_DTO.onChainCosts()).isEqualTo(OnChainCostsDto.createFromModel(ON_CHAIN_COSTS));
    }

    @Test
    void policies() {
        assertThat(CHANNEL_DETAILS_DTO.policies()).isEqualTo(PoliciesDto.createFromModel(POLICIES_FOR_LOCAL_CHANNEL));
    }

    @Test
    void feeReport() {
        assertThat(CHANNEL_DETAILS_DTO.feeReport()).isEqualTo(FeeReportDto.createFromModel(FEE_REPORT));
    }

    @Test
    void flowReport() {
        assertThat(CHANNEL_DETAILS_DTO.flowReport()).isEqualTo(FlowReportDto.createFromModel(FLOW_REPORT));
    }

    @Test
    void rebalanceReport() {
        assertThat(CHANNEL_DETAILS_DTO.rebalanceReport())
                .isEqualTo(RebalanceReportDto.createFromModel(REBALANCE_REPORT));
    }

    @Test
    void warnings() {
        assertThat(CHANNEL_DETAILS_DTO.warnings()).isEqualTo(CHANNEL_WARNINGS.descriptions());
    }

    @Test
    void rating() {
        assertThat(CHANNEL_DETAILS_DTO.rating()).isEqualTo(RatingDto.fromModel(RATING));
    }
    // CPD-ON
}
