package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.FailureCode.AMOUNT_BELOW_MINIMUM;
import static de.cotto.lndmanagej.model.FailureCode.CHANNEL_DISABLED;
import static de.cotto.lndmanagej.model.FailureCode.EXPIRY_TOO_FAR;
import static de.cotto.lndmanagej.model.FailureCode.EXPIRY_TOO_SOON;
import static de.cotto.lndmanagej.model.FailureCode.FEE_INSUFFICIENT;
import static de.cotto.lndmanagej.model.FailureCode.FINAL_EXPIRY_TOO_SOON;
import static de.cotto.lndmanagej.model.FailureCode.FINAL_INCORRECT_CLTV_EXPIRY;
import static de.cotto.lndmanagej.model.FailureCode.FINAL_INCORRECT_HTLC_AMOUNT;
import static de.cotto.lndmanagej.model.FailureCode.INCORRECT_CLTV_EXPIRY;
import static de.cotto.lndmanagej.model.FailureCode.INCORRECT_OR_UNKNOWN_PAYMENT_DETAILS;
import static de.cotto.lndmanagej.model.FailureCode.INCORRECT_PAYMENT_AMOUNT;
import static de.cotto.lndmanagej.model.FailureCode.INVALID_ONION_HMAC;
import static de.cotto.lndmanagej.model.FailureCode.INVALID_ONION_KEY;
import static de.cotto.lndmanagej.model.FailureCode.INVALID_ONION_PAYLOAD;
import static de.cotto.lndmanagej.model.FailureCode.INVALID_ONION_VERSION;
import static de.cotto.lndmanagej.model.FailureCode.INVALID_REALM;
import static de.cotto.lndmanagej.model.FailureCode.MPP_TIMEOUT;
import static de.cotto.lndmanagej.model.FailureCode.PERMANENT_CHANNEL_FAILURE;
import static de.cotto.lndmanagej.model.FailureCode.PERMANENT_NODE_FAILURE;
import static de.cotto.lndmanagej.model.FailureCode.REQUIRED_CHANNEL_FEATURE_MISSING;
import static de.cotto.lndmanagej.model.FailureCode.REQUIRED_NODE_FEATURE_MISSING;
import static de.cotto.lndmanagej.model.FailureCode.TEMPORARY_CHANNEL_FAILURE;
import static de.cotto.lndmanagej.model.FailureCode.TEMPORARY_NODE_FAILURE;
import static de.cotto.lndmanagej.model.FailureCode.UNKNOWN_FAILURE;
import static de.cotto.lndmanagej.model.FailureCode.UNKNOWN_NEXT_PEER;
import static org.assertj.core.api.Assertions.assertThat;

class FailureCodeTest {
    @Test
    void getFor_unknown() {
        assertThat(FailureCode.getFor(99)).isEqualTo(UNKNOWN_FAILURE);
        assertThat(UNKNOWN_FAILURE.isErrorFromFinalNode()).isFalse();
    }

    @Test
    void incorrectOrUnknownPaymentDetails() {
        assertThat(FailureCode.getFor(1)).isEqualTo(INCORRECT_OR_UNKNOWN_PAYMENT_DETAILS);
        assertThat(INCORRECT_OR_UNKNOWN_PAYMENT_DETAILS.isErrorFromFinalNode()).isTrue();
    }

    @Test
    void incorrectPaymentAmount() {
        assertThat(FailureCode.getFor(2)).isEqualTo(INCORRECT_PAYMENT_AMOUNT);
        assertThat(INCORRECT_PAYMENT_AMOUNT.isErrorFromFinalNode()).isFalse();
    }

    @Test
    void finalIncorrectCltvExpiry() {
        assertThat(FailureCode.getFor(3)).isEqualTo(FINAL_INCORRECT_CLTV_EXPIRY);
        assertThat(FINAL_INCORRECT_CLTV_EXPIRY.isErrorFromFinalNode()).isTrue();
    }

    @Test
    void finalIncorrectHtlcAmount() {
        assertThat(FailureCode.getFor(4)).isEqualTo(FINAL_INCORRECT_HTLC_AMOUNT);
        assertThat(FINAL_INCORRECT_HTLC_AMOUNT.isErrorFromFinalNode()).isTrue();
    }

    @Test
    void finalExpiryTooSoon() {
        assertThat(FailureCode.getFor(5)).isEqualTo(FINAL_EXPIRY_TOO_SOON);
        assertThat(FINAL_EXPIRY_TOO_SOON.isErrorFromFinalNode()).isTrue();
    }

    @Test
    void invalidRealm() {
        assertThat(FailureCode.getFor(6)).isEqualTo(INVALID_REALM);
        assertThat(INVALID_REALM.isErrorFromFinalNode()).isFalse();
    }

    @Test
    void expiryTooSoon() {
        assertThat(FailureCode.getFor(7)).isEqualTo(EXPIRY_TOO_SOON);
        assertThat(EXPIRY_TOO_SOON.isErrorFromFinalNode()).isFalse();
    }

    @Test
    void invalidOnionVersion() {
        assertThat(FailureCode.getFor(8)).isEqualTo(INVALID_ONION_VERSION);
        assertThat(INVALID_ONION_VERSION.isErrorFromFinalNode()).isFalse();
    }

    @Test
    void invalidOnionHmac() {
        assertThat(FailureCode.getFor(9)).isEqualTo(INVALID_ONION_HMAC);
        assertThat(INVALID_ONION_HMAC.isErrorFromFinalNode()).isFalse();
    }

    @Test
    void invalidOnionKey() {
        assertThat(FailureCode.getFor(10)).isEqualTo(INVALID_ONION_KEY);
        assertThat(INVALID_ONION_KEY.isErrorFromFinalNode()).isFalse();
    }

    @Test
    void amountBelowMinimum() {
        assertThat(FailureCode.getFor(11)).isEqualTo(AMOUNT_BELOW_MINIMUM);
        assertThat(AMOUNT_BELOW_MINIMUM.isErrorFromFinalNode()).isFalse();
    }

    @Test
    void feeInsufficient() {
        assertThat(FailureCode.getFor(12)).isEqualTo(FEE_INSUFFICIENT);
        assertThat(FEE_INSUFFICIENT.isErrorFromFinalNode()).isFalse();
    }

    @Test
    void incorrectCltvExpiry() {
        assertThat(FailureCode.getFor(13)).isEqualTo(INCORRECT_CLTV_EXPIRY);
        assertThat(INCORRECT_CLTV_EXPIRY.isErrorFromFinalNode()).isFalse();
    }

    @Test
    void channelDisabled() {
        assertThat(FailureCode.getFor(14)).isEqualTo(CHANNEL_DISABLED);
        assertThat(CHANNEL_DISABLED.isErrorFromFinalNode()).isFalse();
    }

    @Test
    void temporaryChannelFailure() {
        assertThat(FailureCode.getFor(15)).isEqualTo(TEMPORARY_CHANNEL_FAILURE);
        assertThat(TEMPORARY_CHANNEL_FAILURE.isErrorFromFinalNode()).isFalse();
    }

    @Test
    void requiredNodeFeatureMissing() {
        assertThat(FailureCode.getFor(16)).isEqualTo(REQUIRED_NODE_FEATURE_MISSING);
        assertThat(REQUIRED_NODE_FEATURE_MISSING.isErrorFromFinalNode()).isFalse();
    }

    @Test
    void requiredChannelFeatureMissing() {
        assertThat(FailureCode.getFor(17)).isEqualTo(REQUIRED_CHANNEL_FEATURE_MISSING);
        assertThat(REQUIRED_CHANNEL_FEATURE_MISSING.isErrorFromFinalNode()).isFalse();
    }

    @Test
    void unknownNextPeer() {
        assertThat(FailureCode.getFor(18)).isEqualTo(UNKNOWN_NEXT_PEER);
        assertThat(UNKNOWN_NEXT_PEER.isErrorFromFinalNode()).isFalse();
    }

    @Test
    void temporaryNodeFailure() {
        assertThat(FailureCode.getFor(19)).isEqualTo(TEMPORARY_NODE_FAILURE);
        assertThat(TEMPORARY_NODE_FAILURE.isErrorFromFinalNode()).isFalse();
    }

    @Test
    void permanentNodeFailure() {
        assertThat(FailureCode.getFor(20)).isEqualTo(PERMANENT_NODE_FAILURE);
        assertThat(PERMANENT_NODE_FAILURE.isErrorFromFinalNode()).isFalse();
    }

    @Test
    void permanentChannelFailure() {
        assertThat(FailureCode.getFor(21)).isEqualTo(PERMANENT_CHANNEL_FAILURE);
        assertThat(PERMANENT_CHANNEL_FAILURE.isErrorFromFinalNode()).isFalse();
    }

    @Test
    void expiryTooFar() {
        assertThat(FailureCode.getFor(22)).isEqualTo(EXPIRY_TOO_FAR);
        assertThat(EXPIRY_TOO_FAR.isErrorFromFinalNode()).isFalse();
    }

    @Test
    void mppTimeout() {
        assertThat(FailureCode.getFor(23)).isEqualTo(MPP_TIMEOUT);
        assertThat(MPP_TIMEOUT.isErrorFromFinalNode()).isTrue();
    }

    @Test
    void invalidOnionPayload() {
        assertThat(FailureCode.getFor(24)).isEqualTo(INVALID_ONION_PAYLOAD);
        assertThat(INVALID_ONION_PAYLOAD.isErrorFromFinalNode()).isFalse();
    }
}
