package de.cotto.lndmanagej.feerates.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneOffset;
import java.util.Optional;

import static de.cotto.lndmanagej.feerates.persistence.FeeRatesFixtures.FEE_RATES;
import static de.cotto.lndmanagej.feerates.persistence.FeeRatesFixtures.TIMESTAMP;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeeRatesDaoImplTest {

    @InjectMocks
    private FeeRatesDaoImpl dao;

    @Mock
    private FeeRatesRepository feeRatesRepository;

    @Test
    void saveStatistics() {
        dao.saveFeeRates(FEE_RATES);
        verify(feeRatesRepository).save(argThat(jpaDto ->
                jpaDto.getTimestamp() == TIMESTAMP.toEpochSecond(ZoneOffset.UTC)
        ));
        verify(feeRatesRepository).save(argThat(jpaDto ->
                jpaDto.getChannelId() == CHANNEL_ID.getShortChannelId()));

        verify(feeRatesRepository).save(argThat(jpaDto ->
                jpaDto.getBaseFeeLocal() == FEE_RATES.feeRates().baseFeeLocal().milliSatoshis()
        ));
        verify(feeRatesRepository).save(argThat(jpaDto ->
                jpaDto.getFeeRateLocal() == FEE_RATES.feeRates().feeRateLocal()
        ));
        verify(feeRatesRepository).save(argThat(jpaDto ->
                jpaDto.getInboundBaseFeeLocal() == FEE_RATES.feeRates().inboundBaseFeeLocal().milliSatoshis()
        ));
        verify(feeRatesRepository).save(argThat(jpaDto ->
                jpaDto.getInboundFeeRateLocal() == FEE_RATES.feeRates().inboundFeeRateLocal()
        ));

        verify(feeRatesRepository).save(argThat(jpaDto ->
                jpaDto.getBaseFeeRemote() == FEE_RATES.feeRates().baseFeeRemote().milliSatoshis()
        ));
        verify(feeRatesRepository).save(argThat(jpaDto ->
                jpaDto.getFeeRateRemote() == FEE_RATES.feeRates().feeRateRemote()
        ));
        verify(feeRatesRepository).save(argThat(jpaDto ->
                jpaDto.getInboundBaseFeeRemote() == FEE_RATES.feeRates().inboundBaseFeeRemote().milliSatoshis()
        ));
        verify(feeRatesRepository).save(argThat(jpaDto ->
                jpaDto.getInboundFeeRateRemote() == FEE_RATES.feeRates().inboundFeeRateRemote()
        ));
    }

    @Test
    void getMostRecentFeeRates_not_found() {
        assertThat(dao.getMostRecentFeeRates(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getMostRecentFeeRates() {
        when(feeRatesRepository.findTopByChannelIdOrderByTimestampDesc(CHANNEL_ID.getShortChannelId()))
                .thenReturn(Optional.of(FeeRatesJpaDto.fromModel(FEE_RATES)));
        assertThat(dao.getMostRecentFeeRates(CHANNEL_ID)).contains(FEE_RATES);
    }
}
