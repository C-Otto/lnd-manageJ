package de.cotto.lndmanagej.grpc;

import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ByteStringConverterTest {
    @Test
    void toHexString() {
        ByteString byteString = ByteString.copyFrom(new byte[]{0, (byte) 0xFF, 0});
        assertThat(ByteStringConverter.toHexString(byteString)).isEqualTo("00ff00");
    }

    @Test
    void fromHexString_lowercase() {
        ByteString byteString = ByteString.copyFrom(new byte[]{0, (byte) 0xFF, 0});
        assertThat(ByteStringConverter.fromHexString("00ff00")).isEqualTo(byteString);
    }

    @Test
    void fromHexString_uppercase() {
        ByteString byteString = ByteString.copyFrom(new byte[]{0, (byte) 0xFF, 0});
        assertThat(ByteStringConverter.fromHexString("00FF00")).isEqualTo(byteString);
    }

    @Test
    void fromHexString_mixed_case() {
        ByteString byteString = ByteString.copyFrom(new byte[]{0, (byte) 0xFF, 0, (byte) 0xFF, 0, (byte) 0xFF});
        assertThat(ByteStringConverter.fromHexString("00Ff00FF00ff")).isEqualTo(byteString);
    }
}