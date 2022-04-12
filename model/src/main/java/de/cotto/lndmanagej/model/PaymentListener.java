package de.cotto.lndmanagej.model;

import java.util.List;

public interface PaymentListener {
    void success(HexString preimage, List<PaymentAttemptHop> paymentAttemptHops);

    void failure(List<PaymentAttemptHop> paymentAttemptHops, FailureCode failureCode, int failureSourceIndex);
}
