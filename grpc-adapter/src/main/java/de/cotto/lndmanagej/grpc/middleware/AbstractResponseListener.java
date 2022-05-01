package de.cotto.lndmanagej.grpc.middleware;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public abstract class AbstractResponseListener<ResponseType> implements ResponseListener<ResponseType> {
    private final String responseType;
    private final Parser<ResponseType> responseParser;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public AbstractResponseListener(String responseType, Parser<ResponseType> responseParser) {
        this.responseType = responseType;
        this.responseParser = responseParser;
    }

    @Override
    public String getResponseType() {
        return responseType;
    }

    @Override
    public void acceptResponse(ByteString response, long requestId) {
        parse(response, responseParser).ifPresent(parsed -> acceptResponse(parsed, requestId));
    }

    <T> Optional<T> parse(ByteString message, Parser<T> parser) {
        try {
            return Optional.of(parser.parse(message));
        } catch (InvalidProtocolBufferException e) {
            logger.warn("Unable to parse: ", e);
            return Optional.empty();
        }
    }
}
