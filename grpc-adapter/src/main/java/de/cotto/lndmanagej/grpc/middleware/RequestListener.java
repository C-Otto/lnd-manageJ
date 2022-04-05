package de.cotto.lndmanagej.grpc.middleware;

import com.google.protobuf.ByteString;

public interface RequestListener<T> {
    String getRequestType();

    void acceptRequest(ByteString request, long requestId);

    void acceptRequest(T request, long requestId);
}
