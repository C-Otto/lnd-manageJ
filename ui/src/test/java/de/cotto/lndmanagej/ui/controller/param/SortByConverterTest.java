package de.cotto.lndmanagej.ui.controller.param;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.cotto.lndmanagej.ui.controller.param.SortBy.DEFAULT_SORT;
import static de.cotto.lndmanagej.ui.controller.param.SortBy.LOCAL_BASE_FEE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class SortByConverterTest {

    @InjectMocks
    private SortByConverter converter;

    @Test
    void convert_queryParam_enumValue() {
        assertThat(converter.convert("local-base-fee")).isEqualTo(LOCAL_BASE_FEE);
    }

    @Test
    void channelRating_unknownParam_default() {
        assertThat(converter.convert("unknown")).isEqualTo(DEFAULT_SORT);
    }
}