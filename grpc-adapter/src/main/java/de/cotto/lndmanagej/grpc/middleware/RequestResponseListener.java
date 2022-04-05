package de.cotto.lndmanagej.grpc.middleware;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public abstract class RequestResponseListener<RequestType, ResponseType>
        implements RequestListener<RequestType>, ResponseListener<ResponseType> {
    private final String requestType;
    private final Parser<RequestType> requestParser;
    private final String responseType;
    private final Parser<ResponseType> responseParser;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public RequestResponseListener(
            String requestType,
            Parser<RequestType> requestParser,
            String responseType,
            Parser<ResponseType> responseParser
    ) {
        this.requestType = requestType;
        this.requestParser = requestParser;
        this.responseType = responseType;
        this.responseParser = responseParser;
    }

    @Override
    public String getRequestType() {
        return requestType;
    }

    @Override
    public void acceptRequest(ByteString request, long requestId) {
        parse(request, requestParser).ifPresent(parsed -> acceptRequest(parsed, requestId));
    }

    @Override
    public String getResponseType() {
        return responseType;
    }

    @Override
    public void acceptResponse(ByteString response, long requestId) {
        parse(response, responseParser).ifPresent(parsed -> acceptResponse(parsed, requestId));
    }

    private <T> Optional<T> parse(ByteString message, Parser<T> parser) {
        try {
            return Optional.of(parser.parse(message));
        } catch (InvalidProtocolBufferException e) {
            logger.warn("Unable to parse: ", e);
            return Optional.empty();
        }
    }
}
