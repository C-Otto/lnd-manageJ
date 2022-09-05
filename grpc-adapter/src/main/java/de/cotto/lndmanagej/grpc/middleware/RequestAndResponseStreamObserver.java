package de.cotto.lndmanagej.grpc.middleware;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.grpc.stub.StreamObserver;
import lnrpc.InterceptFeedback;
import lnrpc.MiddlewareRegistration;
import lnrpc.RPCMessage;
import lnrpc.RPCMiddlewareRequest;
import lnrpc.RPCMiddlewareResponse;

import javax.annotation.CheckForNull;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class RequestAndResponseStreamObserver implements StreamObserver<RPCMiddlewareRequest> {
    private static final String MIDDLEWARE_NAME = "lnd-manageJ";

    private static final MiddlewareRegistration REGISTRATION = MiddlewareRegistration.newBuilder()
            .setReadOnlyMode(true)
            .setMiddlewareName(MIDDLEWARE_NAME)
            .build();
    private static final InterceptFeedback DO_NOT_REPLACE_FEEDBACK = InterceptFeedback.newBuilder()
            .setReplaceResponse(false)
            .build();

    @CheckForNull
    private StreamObserver<RPCMiddlewareResponse> responseObserver;
    @CheckForNull
    private ObserverIsDoneListener observerIsDoneListener;
    private final Multimap<String, RequestListener<?>> requestListeners = ArrayListMultimap.create();
    private final Multimap<String, ResponseListener<?>> responseListeners = ArrayListMultimap.create();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private boolean isDone;

    public RequestAndResponseStreamObserver() {
        // default constructor
    }

    public void initialize(
            StreamObserver<RPCMiddlewareResponse> responseObserver,
            ObserverIsDoneListener observerIsDoneListener
    ) {
        this.responseObserver = responseObserver;
        this.observerIsDoneListener = observerIsDoneListener;
        if (isDone) {
            observerIsDoneListener.onIsDone();
        } else {
            RPCMiddlewareResponse registrationMessage =
                    RPCMiddlewareResponse.newBuilder().setRegister(REGISTRATION).build();
            responseObserver.onNext(registrationMessage);
        }
    }

    @Override
    @SuppressWarnings("FutureReturnValueIgnored")
    public void onNext(RPCMiddlewareRequest value) {
        respondWithDoNotReplace(value.getMsgId());
        executorService.submit(() -> {
            handleRequest(value);
            handleResponse(value);
        });
    }

    @Override
    public void onError(Throwable throwable) {
        if (observerIsDoneListener != null) {
            observerIsDoneListener.onIsDone();
        } else {
            isDone = true;
        }
    }

    @Override
    public void onCompleted() {
        Objects.requireNonNull(observerIsDoneListener).onIsDone();
    }

    private void handleRequest(RPCMiddlewareRequest value) {
        if (!value.hasRequest()) {
            return;
        }
        RPCMessage request = value.getRequest();
        String typeName = request.getTypeName();
        long requestId = value.getRequestId();
        requestListeners.get(typeName)
                .forEach(listener -> listener.acceptRequest(request.getSerialized(), requestId));
    }

    private void handleResponse(RPCMiddlewareRequest value) {
        if (!value.hasResponse()) {
            return;
        }
        RPCMessage response = value.getResponse();
        String typeName = response.getTypeName();
        long requestId = value.getRequestId();
        responseListeners.get(typeName)
                .forEach(listener -> listener.acceptResponse(response.getSerialized(), requestId));
    }

    private void respondWithDoNotReplace(long messageId) {
        Objects.requireNonNull(responseObserver).onNext(createDoNotReplaceMessage(messageId));
    }

    public void addRequestListener(RequestListener<?> listener) {
        requestListeners.put(listener.getRequestType(), listener);
    }

    public void addResponseListener(ResponseListener<?> listener) {
        responseListeners.put(listener.getResponseType(), listener);
    }

    private RPCMiddlewareResponse createDoNotReplaceMessage(long messageId) {
        return RPCMiddlewareResponse.newBuilder()
                .setRefMsgId(messageId)
                .setFeedback(DO_NOT_REPLACE_FEEDBACK)
                .build();
    }
}
