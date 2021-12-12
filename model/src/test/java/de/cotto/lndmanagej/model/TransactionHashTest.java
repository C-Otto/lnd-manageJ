package de.cotto.lndmanagej.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class TransactionHashTest {

    private static final String VALID_HASH = "abc222abc000abc000abc000abc000abc000abc000abc000abc000abc000abc0";

    @Test
    void create_not_hex() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> TransactionHash.create("abc222abc000abc000abc000abc000abc000abc000abc000abc000abc000abcx")
        );
    }

    @Test
    void create_wrong_length() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> TransactionHash.create("abc222abc000abc000abc000abc000abc000abc000abc000abc000abc000abc00")
        );
    }

    @Test
    void create() {
        assertThat(TransactionHash.create(VALID_HASH).getHash()).isEqualTo(VALID_HASH);
    }

    @Test
    void testToString() {
        assertThat(TransactionHash.create(VALID_HASH)).hasToString(VALID_HASH);
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(TransactionHash.class).usingGetClass().verify();
    }
}