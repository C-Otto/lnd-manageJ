package de.cotto.lndmanagej.feerates.persistence;

import de.cotto.lndmanagej.model.FeeRateInformation;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;

import static de.cotto.lndmanagej.feerates.persistence.FeeRatesFixtures.FEE_RATES;
import static de.cotto.lndmanagej.feerates.persistence.FeeRatesFixtures.TIMESTAMP;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static org.assertj.core.api.Assertions.assertThat;

class FeeRatesJpaDtoTest {
    @Test
    @SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
    void fromModel() {
        FeeRatesJpaDto jpaDto = FeeRatesJpaDto.fromModel(FEE_RATES);
        assertThat(jpaDto.getTimestamp()).isEqualTo(TIMESTAMP.toEpochSecond(ZoneOffset.UTC));
        assertThat(jpaDto.getChannelId()).isEqualTo(CHANNEL_ID.getShortChannelId());

        FeeRateInformation info = FEE_RATES.feeRates();
        assertThat(jpaDto.getBaseFeeLocal()).isEqualTo(info.baseFeeLocal().milliSatoshis());
        assertThat(jpaDto.getFeeRateLocal()).isEqualTo(info.feeRateLocal());
        assertThat(jpaDto.getInboundBaseFeeLocal()).isEqualTo(info.inboundBaseFeeLocal().milliSatoshis());
        assertThat(jpaDto.getInboundFeeRateLocal()).isEqualTo(info.inboundFeeRateLocal());

        assertThat(jpaDto.getBaseFeeRemote()).isEqualTo(info.baseFeeRemote().milliSatoshis());
        assertThat(jpaDto.getFeeRateRemote()).isEqualTo(info.feeRateRemote());
        assertThat(jpaDto.getInboundBaseFeeRemote()).isEqualTo(info.inboundBaseFeeRemote().milliSatoshis());
        assertThat(jpaDto.getInboundFeeRateRemote()).isEqualTo(info.inboundFeeRateRemote());
    }

    @Test
    void toModel() {
        assertThat(FeeRatesJpaDto.fromModel(FEE_RATES).toModel()).isEqualTo(FEE_RATES);
    }
}
