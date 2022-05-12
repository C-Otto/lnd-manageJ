package de.cotto.lndmanagej.ui.demo.data;

import de.cotto.lndmanagej.model.OpenInitiator;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.ui.demo.data.DemoDataService.POCKET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeriveDataUtilTest {

    @Test
    void deriveRebalanceReport_sameChannelId_sameResult() {
        assertEquals("269000", DeriveDataUtil.deriveRebalanceReport(CHANNEL_ID).sourceAmountMilliSat());
        assertEquals("269000", DeriveDataUtil.deriveRebalanceReport(CHANNEL_ID).sourceAmountMilliSat());
    }

    @Test
    void deriveFeeReport_sameChannelId_sameResult() {
        assertEquals("188477", DeriveDataUtil.deriveFeeReport(CHANNEL_ID).earnedMilliSat());
        assertEquals("188477", DeriveDataUtil.deriveFeeReport(CHANNEL_ID).earnedMilliSat());
    }

    @Test
    void deriveFlowReport_sameChannelId_sameResult() {
        assertEquals("88477000", DeriveDataUtil.deriveFlowReport(CHANNEL_ID).forwardedSentMilliSat());
        assertEquals("88477000", DeriveDataUtil.deriveFlowReport(CHANNEL_ID).forwardedSentMilliSat());
    }

    @Test
    void derivePolicy_sameChannelId_sameResult() {
        assertEquals(770L, DeriveDataUtil.derivePolicy(CHANNEL_ID).feeRate());
        assertEquals(770L, DeriveDataUtil.derivePolicy(CHANNEL_ID).feeRate());
    }

    @Test
    void deriveWarnings_noWarnings() {
        assertFalse(DeriveDataUtil.deriveWarnings(CHANNEL_ID).isEmpty());
    }

    @Test
    void deriveWarnings_hasWarnings() {
        assertTrue(DeriveDataUtil.deriveWarnings(POCKET.channelId()).isEmpty());
    }

    @Test
    void deriveChannelWarnings_noWarnings() {
        assertFalse(DeriveDataUtil.deriveChannelWarnings(CHANNEL_ID).isEmpty());
    }

    @Test
    void deriveChannelWarnings_hasWarnings() {
        assertTrue(DeriveDataUtil.deriveChannelWarnings(POCKET.channelId()).isEmpty());
    }

    @Test
    void deriveOnChainCosts_sameChannelId_sameResult() {
        assertEquals("1849", DeriveDataUtil.deriveOnChainCosts(CHANNEL_ID).sweepCostsSat());
        assertEquals("1849", DeriveDataUtil.deriveOnChainCosts(CHANNEL_ID).sweepCostsSat());
    }

    @Test
    void deriveOpenInitiator_sameChannelId_sameResult() {
        assertEquals(OpenInitiator.LOCAL, DeriveDataUtil.deriveOpenInitiator(CHANNEL_ID));
        assertEquals(OpenInitiator.LOCAL, DeriveDataUtil.deriveOpenInitiator(CHANNEL_ID));
    }
}