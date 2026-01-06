package de.cotto.lndmanagej.grpc.middleware;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

public interface Parser<T> {
    T parse(ByteString byteString) throws InvalidProtocolBufferException;
}
