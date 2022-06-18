package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.PubkeyAndFeeRate;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;

class PubkeyAndFeeRateDtoTest {
    @Test
    void pubkey() {
        assertThat(new PubkeyAndFeeRateDto(PUBKEY, 123).pubkey()).isEqualTo(PUBKEY);
    }

    @Test
    void feeRate() {
        assertThat(new PubkeyAndFeeRateDto(PUBKEY, 123).feeRate()).isEqualTo(123);
    }

    @Test
    void fromModel() {
        assertThat(PubkeyAndFeeRateDto.fromModel(new PubkeyAndFeeRate(PUBKEY, 123)))
                .isEqualTo(new PubkeyAndFeeRateDto(PUBKEY, 123));
    }
}