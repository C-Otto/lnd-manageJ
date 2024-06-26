package de.cotto.lndmanagej.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SuppressWarnings("PMD.GodClass")
class CoinsTest {

    private static final Coins ONE_COIN = Coins.ofSatoshis(100_000_000);

    @Test
    void add() {
        assertThat(Coins.ofSatoshis(123).add(Coins.ofSatoshis(456))).isEqualTo(Coins.ofSatoshis(123 + 456));
    }

    @Test
    void add_milli_satoshis() {
        assertThat(Coins.ofMilliSatoshis(400).add(Coins.ofMilliSatoshis(600))).isEqualTo(Coins.ofSatoshis(1));
    }

    @Test
    void zero_sum_is_none_object() {
        assertThat(Coins.ofMilliSatoshis(400).add(Coins.ofMilliSatoshis(-400))).isSameAs(Coins.NONE);
    }

    @Test
    void difference_resulting_in_zero_is_none_object() {
        assertThat(Coins.ofMilliSatoshis(400).subtract(Coins.ofMilliSatoshis(400))).isSameAs(Coins.NONE);
    }

    @Test
    void zero_milli_satoshis_is_non_object() {
        assertThat(Coins.ofMilliSatoshis(0)).isSameAs(Coins.NONE);
    }

    @Test
    void zero_satoshis_is_non_object() {
        assertThat(Coins.ofSatoshis(0)).isSameAs(Coins.NONE);
    }

    @Test
    void add_zero_gives_same_instance() {
        Coins original = Coins.ofSatoshis(1);
        assertThat(original.add(Coins.NONE)).isSameAs(original);
    }

    @Test
    void adding_to_zero_gives_same_instance() {
        Coins original = Coins.ofSatoshis(1);
        assertThat(Coins.NONE.add(original)).isSameAs(original);
    }

    @Test
    void subtract_zero_gives_same_instance() {
        Coins original = Coins.ofSatoshis(1);
        assertThat(original.subtract(Coins.NONE)).isSameAs(original);
    }

    @Test
    void subtract() {
        assertThat(Coins.ofSatoshis(456).subtract(Coins.ofSatoshis(123))).isEqualTo(Coins.ofSatoshis(456 - 123));
    }

    @Test
    void absolute_for_positive() {
        assertThat(Coins.ofSatoshis(456).absolute()).isEqualTo(Coins.ofSatoshis(456));
    }

    @Test
    void absolute_for_negative() {
        assertThat(Coins.ofSatoshis(-456).absolute()).isEqualTo(Coins.ofSatoshis(456));
    }

    @Test
    void compareTo_greater_than() {
        assertThat(Coins.ofSatoshis(2).compareTo(Coins.ofSatoshis(1))).isGreaterThan(0);
    }

    @Test
    void compareTo_equal() {
        assertThat(Coins.ofSatoshis(0).compareTo(Coins.NONE)).isEqualTo(0);
    }

    @Test
    void compareTo_smaller_than() {
        assertThat(Coins.ofSatoshis(1).compareTo(Coins.ofSatoshis(2))).isLessThan(0);
    }

    @Test
    void isPositive() {
        assertThat(Coins.ofSatoshis(100).isPositive()).isTrue();
        assertThat(Coins.ofSatoshis(-100).isPositive()).isFalse();
    }

    @Test
    void isNonPositive() {
        assertThat(Coins.ofSatoshis(-100).isNonPositive()).isTrue();
        assertThat(Coins.ofSatoshis(0).isNonPositive()).isTrue();
        assertThat(Coins.ofSatoshis(100).isNonPositive()).isFalse();
    }

    @Test
    void isNegative() {
        assertThat(Coins.ofSatoshis(-100).isNegative()).isTrue();
        assertThat(Coins.ofSatoshis(100).isNegative()).isFalse();
    }

    @Test
    void isNonNegative() {
        assertThat(Coins.ofSatoshis(-100).isNonNegative()).isFalse();
        assertThat(Coins.ofSatoshis(0).isNonNegative()).isTrue();
        assertThat(Coins.ofSatoshis(100).isNonNegative()).isTrue();
    }

    @Test
    void isNegative_isPositive_zero_coins() {
        assertThat(Coins.NONE.isPositive()).isFalse();
        assertThat(Coins.NONE.isNegative()).isFalse();
    }

    @Test
    void getMillionSatoshis() {
        assertThat(Coins.NONE.getMillionSatoshis()).isEqualTo(0d);
        assertThat(Coins.ofSatoshis(1_500_000).getMillionSatoshis()).isEqualTo(1.5d);
    }

    @Test
    void none() {
        assertThat(Coins.NONE).isEqualTo(Coins.ofSatoshis(0));
    }

    @Test
    void ofSatoshis() {
        Coins coins = Coins.ofSatoshis(100_000_000);
        assertThat(coins).isEqualTo(ONE_COIN);
    }

    @Test
    void ofMilliSatoshis() {
        Coins coins = Coins.ofMilliSatoshis(100_000_000_000L);
        assertThat(coins).isEqualTo(ONE_COIN);
    }

    @Test
    void getSatoshis() {
        long satoshis = 1_234L;
        assertThat(Coins.ofSatoshis(satoshis).satoshis()).isEqualTo(satoshis);
    }

    @Test
    void getMilliSatoshis() {
        long milliSatoshis = 1_234L;
        assertThat(Coins.ofMilliSatoshis(milliSatoshis).milliSatoshis()).isEqualTo(milliSatoshis);
    }

    @Test
    void getSatoshis_with_fraction() {
        long milliSatoshis = 1_234L;
        //noinspection ResultOfMethodCallIgnored
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(
                () -> Coins.ofMilliSatoshis(milliSatoshis).satoshis()
        );
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(Coins.class).usingGetClass().verify();
    }

    @Test
    void justSatoshi() {
        assertThat(Coins.ofSatoshis(12_345)).hasToString("12,345.000");
    }

    @Test
    void toStringSat() {
        assertThat(Coins.ofSatoshis(12_345).toStringSat()).isEqualTo("12,345");
    }

    @Test
    void toStringSat_with_milli_sat() {
        assertThat(Coins.ofMilliSatoshis(12_345_999).toStringSat()).isEqualTo("12,345");
    }

    @Test
    void justMilliCoins() {
        assertThat(Coins.ofSatoshis(12_300_000)).hasToString("12,300,000.000");
    }

    @Test
    void formats_decimal_point_with_english_locale() {
        Locale defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.GERMAN);
        try {
            assertThat(Coins.ofMilliSatoshis(1_234)).hasToString("1.234");
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }

    @Test
    void justCoins() {
        assertThat(Coins.ofSatoshis(12_300_000_000L)).hasToString("12,300,000,000.000");
    }

    @Test
    void everything() {
        assertThat(Coins.ofMilliSatoshis(12_345_678_123_987L)).hasToString("12,345,678,123.987");
    }

    @Test
    void negative() {
        assertThat(Coins.ofSatoshis(-12_345_678_123L)).hasToString("-12,345,678,123.000");
    }

    @Test
    void mixed() {
        assertThat(Coins.ofMilliSatoshis(12_000_678_100_010L)).hasToString("12,000,678,100.010");
    }

    @Test
    void manyCoins() {
        assertThat(Coins.ofSatoshis(321_000_678_100L)).hasToString("321,000,678,100.000");
    }

    @Test
    void manyCoins_negative() {
        assertThat(Coins.ofSatoshis(-321_000_678_100L)).hasToString("-321,000,678,100.000");
    }

    @Test
    void minimum_first() {
        assertThat(Coins.ofMilliSatoshis(1).minimum(Coins.ofMilliSatoshis(2))).isEqualTo(Coins.ofMilliSatoshis(1));
    }

    @Test
    void minimum_second() {
        assertThat(Coins.ofMilliSatoshis(100).minimum(Coins.ofMilliSatoshis(50))).isEqualTo(Coins.ofMilliSatoshis(50));
    }

    @Test
    void minimum_same() {
        assertThat(Coins.ofMilliSatoshis(1).minimum(Coins.ofMilliSatoshis(1))).isEqualTo(Coins.ofMilliSatoshis(1));
    }

    @Test
    void minimum_same_prefers_this() {
        Coins first = Coins.ofMilliSatoshis(1);
        Coins second = Coins.ofMilliSatoshis(1);
        assertThat(first.minimum(second)).isSameAs(first);
    }

    @Test
    void minimum_null() {
        assertThat(Coins.ofMilliSatoshis(1).minimum(null)).isEqualTo(Coins.ofMilliSatoshis(1));
    }

    @Test
    void maximum_first() {
        assertThat(Coins.ofMilliSatoshis(2).maximum(Coins.ofMilliSatoshis(1))).isEqualTo(Coins.ofMilliSatoshis(2));
    }

    @Test
    void maximum_second() {
        assertThat(Coins.ofMilliSatoshis(50).maximum(Coins.ofMilliSatoshis(100))).isEqualTo(Coins.ofMilliSatoshis(100));
    }

    @Test
    void maximum_same() {
        assertThat(Coins.ofMilliSatoshis(1).maximum(Coins.ofMilliSatoshis(1))).isEqualTo(Coins.ofMilliSatoshis(1));
    }

    @Test
    void maximum_same_prefers_this() {
        Coins first = Coins.ofMilliSatoshis(1);
        Coins second = Coins.ofMilliSatoshis(1);
        assertThat(first.maximum(second)).isSameAs(first);
    }

    @Test
    void maximum_null() {
        assertThat(Coins.ofMilliSatoshis(1).maximum(null)).isEqualTo(Coins.ofMilliSatoshis(1));
    }

    @Test
    void negate_zero() {
        assertThat(Coins.NONE.negate()).isEqualTo(Coins.NONE);
    }

    @Test
    void negate_positive() {
        assertThat(Coins.ofMilliSatoshis(123).negate()).isEqualTo(Coins.ofMilliSatoshis(-123));
    }

    @Test
    void negate_negative() {
        assertThat(Coins.ofSatoshis(-1).negate()).isEqualTo(Coins.ofMilliSatoshis(1_000));
    }
}
