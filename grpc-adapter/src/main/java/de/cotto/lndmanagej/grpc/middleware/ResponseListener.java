package de.cotto.lndmanagej.grpc.middleware;

import com.google.protobuf.ByteString;

public interface ResponseListener<T> {
    String getResponseType();

    void acceptResponse(ByteString response, long requestId);

    void acceptResponse(T response, long requestId);
}
