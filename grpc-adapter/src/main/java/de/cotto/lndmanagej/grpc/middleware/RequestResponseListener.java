package de.cotto.lndmanagej.grpc.middleware;

import com.google.protobuf.ByteString;

public abstract class RequestResponseListener<RequestType, ResponseType>
        extends AbstractResponseListener<ResponseType>
        implements RequestListener<RequestType> {
    private final String requestType;
    private final Parser<RequestType> requestParser;

    public RequestResponseListener(
            String requestType,
            Parser<RequestType> requestParser,
            String responseType,
            Parser<ResponseType> responseParser
    ) {
        super(responseType, responseParser);
        this.requestType = requestType;
        this.requestParser = requestParser;
    }

    @Override
    public String getRequestType() {
        return requestType;
    }

    @Override
    public void acceptRequest(ByteString request, long requestId) {
        parse(request, requestParser).ifPresent(parsed -> acceptRequest(parsed, requestId));
    }
}
