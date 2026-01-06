package de.cotto.lndmanagej.grpc.middleware;

import com.google.protobuf.ByteString;

public abstract class RequestResponseListener<REQUEST, RESPONSE>
        extends AbstractResponseListener<RESPONSE>
        implements RequestListener<REQUEST> {
    private final String requestType;
    private final Parser<REQUEST> requestParser;

    public RequestResponseListener(
            String requestType,
            Parser<REQUEST> requestParser,
            String responseType,
            Parser<RESPONSE> responseParser
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
