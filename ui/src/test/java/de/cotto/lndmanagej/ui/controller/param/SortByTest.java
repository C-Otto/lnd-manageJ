package de.cotto.lndmanagej.ui.controller.param;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.ui.controller.param.SortBy.channelrating;
import static de.cotto.lndmanagej.ui.controller.param.SortBy.localfeerate;
import static de.cotto.lndmanagej.ui.controller.param.SortBy.remotebasefee;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SortByTest {

    @Test
    void channelRating() {
        assertThat(channelrating.name()).isEqualTo("channelrating");
    }

    @Test
    void localFeeRate() {
        assertThat(localfeerate.name()).isEqualTo("localfeerate");
    }

    @Test
    void remoteBaseFee() {
        assertThat(remotebasefee.name()).isEqualTo("remotebasefee");
    }

}