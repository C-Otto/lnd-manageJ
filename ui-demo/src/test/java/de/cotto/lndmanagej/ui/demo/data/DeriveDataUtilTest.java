package de.cotto.lndmanagej.ui.demo.data;

import de.cotto.lndmanagej.model.OpenInitiator;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static org.assertj.core.api.Assertions.assertThat;

class DeriveDataUtilTest {

    @Test
    void deriveRebalanceReport_sameChannelId_sameResult() {
        assertThat(DeriveDataUtil.deriveRebalanceReport(CHANNEL_ID).sourceAmountMilliSat()).isEqualTo("269000");
        assertThat(DeriveDataUtil.deriveRebalanceReport(CHANNEL_ID).sourceAmountMilliSat()).isEqualTo("269000");
    }

    @Test
    void deriveFeeReport_sameChannelId_sameResult() {
        assertThat(DeriveDataUtil.deriveFeeReport(CHANNEL_ID).earnedMilliSat()).isEqualTo("188477");
        assertThat(DeriveDataUtil.deriveFeeReport(CHANNEL_ID).earnedMilliSat()).isEqualTo("188477");
    }

    @Test
    void deriveFlowReport_sameChannelId_sameResult() {
        assertThat(DeriveDataUtil.deriveFlowReport(CHANNEL_ID).forwardedSentMilliSat()).isEqualTo("88477000");
        assertThat(DeriveDataUtil.deriveFlowReport(CHANNEL_ID).forwardedSentMilliSat()).isEqualTo("88477000");
    }

    @Test
    void derivePolicy_sameChannelId_sameResult() {
        assertThat(DeriveDataUtil.derivePolicy(CHANNEL_ID).feeRate()).isEqualTo(770L);
        assertThat(DeriveDataUtil.derivePolicy(CHANNEL_ID).feeRate()).isEqualTo(770L);
    }

    @Test
    void deriveOnChainCosts_sameChannelId_sameResult() {
        assertThat(DeriveDataUtil.deriveOnChainCosts(CHANNEL_ID).sweepCostsSat()).isEqualTo("1849");
        assertThat(DeriveDataUtil.deriveOnChainCosts(CHANNEL_ID).sweepCostsSat()).isEqualTo("1849");
    }

    @Test
    void deriveOpenInitiator_sameChannelId_sameResult() {
        assertThat(DeriveDataUtil.deriveOpenInitiator(CHANNEL_ID)).isEqualTo(OpenInitiator.LOCAL);
        assertThat(DeriveDataUtil.deriveOpenInitiator(CHANNEL_ID)).isEqualTo(OpenInitiator.LOCAL);
    }

    @Test
    void deriveRating_sameChannelId_sameResult() {
        assertThat(DeriveDataUtil.deriveRating(CHANNEL_ID)).isEqualTo(51_361);
        assertThat(DeriveDataUtil.deriveRating(CHANNEL_ID)).isEqualTo(51_361);
    }
}
