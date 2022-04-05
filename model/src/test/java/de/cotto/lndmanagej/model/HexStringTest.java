package de.cotto.lndmanagej.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class HexStringTest {
    @Test
    void empty() {
        assertThat(HexString.EMPTY).isEqualTo(new HexString(""));
    }

    @Test
    void testToString() {
        assertThat(new HexString("10")).hasToString("10");
    }

    @Test
    void create_from_byte() {
        assertThat(new HexString((byte) 255)).isEqualTo(new HexString("FF"));
    }

    @Test
    void create_from_byte_array() {
        assertThat(new HexString((byte) 255, (byte) 0)).isEqualTo(new HexString("FF00"));
    }

    @Test
    void create_from_byte_array_uses_copy() {
        byte[] byteArray = {(byte) 255, (byte) 0};
        HexString hexString = new HexString(byteArray);
        byteArray[0] = 0;
        assertThat(hexString.getByteArray()).containsExactly(255, 0);
    }

    @Test
    void create_from_byte_list() {
        assertThat(new HexString(List.of((byte) 255, (byte) 0))).isEqualTo(new HexString("FF00"));
    }

    @Test
    void create_from_byte_list_uses_copy() {
        List<Byte> list = new ArrayList<>(List.of((byte) 255, (byte) 0));
        HexString hexString = new HexString(list);
        list.set(0, (byte) 0);
        assertThat(hexString).isEqualTo(new HexString("FF00"));
    }

    @Test
    void create_from_string_odd_length() {
        assertThatIllegalArgumentException().isThrownBy(() -> new HexString("F"));
    }

    @Test
    void create_from_string_many_bytes() {
        assertThat(new HexString("FF".repeat(255)).getNumberOfBytes()).isEqualTo(255);
    }

    @Test
    void create_from_string_very_many_bytes() {
        assertThat(new HexString("FF".repeat(256)).getNumberOfBytes()).isEqualTo(256);
    }

    @Test
    void create_from_string_invalid_character() {
        assertThatIllegalArgumentException().isThrownBy(() -> new HexString("FFXF"));
    }

    @Test
    void getNumberOfBytes_empty() {
        assertThat(new HexString("").getNumberOfBytes()).isZero();
    }

    @Test
    void getNumberOfBytes_even_string_length() {
        assertThat(new HexString("FF").getNumberOfBytes()).isEqualTo(1);
    }

    @Test
    void getByteArray_one_byte() {
        assertThat(new HexString("FF").getByteArray()).isEqualTo(new byte[]{(byte) 255});
    }

    @Test
    void getByteArray_three_bytes() {
        assertThat(new HexString("FFAA11").getByteArray()).isEqualTo(new byte[]{(byte) 255, (byte) 170, (byte) 17});
    }

    @Test
    void getByteArray_is_copy() {
        HexString hexString = new HexString("FF");
        byte[] byteArray = hexString.getByteArray();
        byteArray[0] = 0;
        assertThat(hexString.getByteArray()).isEqualTo(new byte[]{(byte) 255});
    }

    @Test
    void getStringLength_one_byte() {
        assertThat(new HexString("FF").getStringLength()).isEqualTo(2);
    }

    @Test
    void getStringLength_three_bytes() {
        assertThat(new HexString("112233").getStringLength()).isEqualTo(6);
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(HexString.class).usingGetClass().verify();
    }
}
