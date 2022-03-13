package de.cotto.lndmanagej.grpc;

import com.google.protobuf.ByteString;

import java.util.HexFormat;

public final class ByteStringConverter {
    private ByteStringConverter() {
        // do not instantiate me
    }

    public static String toHexString(ByteString byteString) {
        return HexFormat.of().formatHex(byteString.toByteArray());
    }

    public static ByteString fromHexString(String hexString) {
        byte[] bytes = HexFormat.of().parseHex(hexString);
        return ByteString.copyFrom(bytes);
    }
}
