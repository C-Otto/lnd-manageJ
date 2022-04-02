package de.cotto.lndmanagej.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class PubkeyTest {
    private static final String VALID_PUBKEY_STRING =
            "027abc123abc123abc123abc123123abc123abc123abc123abc123abc123abc121";

    @Test
    void empty() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> Pubkey.create("")
        );
    }

    @Test
    void too_short() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> Pubkey.create("027abc123abc123abc123abc123123abc123abc123abc123abc123abc123abc12")
        );
    }

    @Test
    void too_long() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> Pubkey.create("027abc123abc123abc123abc123123abc123abc123abc123abc123abc123abc123a")
        );
    }

    @Test
    void not_just_hex() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> Pubkey.create("x27abc123abc123abc123abc123123abc123abc123abc123abc123abc123abc123")
        );
    }

    @Test
    void testToString() {
        assertThat(PUBKEY).hasToString(VALID_PUBKEY_STRING);
    }

    @Test
    void testToString_from_uppercase() {
        assertThat(Pubkey.create("027ABC123ABC123ABC123ABC123123ABC123ABC123ABC123ABC123ABC123ABC121"))
                .hasToString(VALID_PUBKEY_STRING);
    }

    @Test
    void testCompareTo_smaller() {
        Pubkey one = Pubkey.create(VALID_PUBKEY_STRING);
        Pubkey two = Pubkey.create("100000000000000000000000000000000000000000000000000000000000000000");
        assertThat(one.compareTo(two)).isLessThan(0);
    }

    @Test
    void testCompareTo_same() {
        Pubkey one = Pubkey.create(VALID_PUBKEY_STRING);
        Pubkey two = Pubkey.create(VALID_PUBKEY_STRING);
        assertThat(one.compareTo(two)).isEqualTo(0);
    }

    @Test
    void testCompareTo_larger() {
        Pubkey one = Pubkey.create(VALID_PUBKEY_STRING);
        Pubkey two = Pubkey.create("100000000000000000000000000000000000000000000000000000000000000000");
        assertThat(two.compareTo(one)).isGreaterThan(0);
    }

    @Test
    void testEquals_from_uppercase() {
        assertThat(Pubkey.create("027ABC123ABC123ABC123ABC123123ABC123ABC123ABC123ABC123ABC123ABC121"))
                .isEqualTo(PUBKEY);
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(Pubkey.class)
                .withIgnoredFields("string")
                .usingGetClass()
                .verify();
    }
}
