package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH_3;
import static de.cotto.lndmanagej.model.ResolutionFixtures.ANCHOR_CLAIMED;
import static de.cotto.lndmanagej.model.ResolutionFixtures.COMMIT_CLAIMED;
import static de.cotto.lndmanagej.model.ResolutionFixtures.INCOMING_HTLC_CLAIMED;
import static de.cotto.lndmanagej.model.ResolutionFixtures.INCOMING_HTLC_TIMEOUT;
import static de.cotto.lndmanagej.model.ResolutionFixtures.OUTGOING_HTLC_CLAIMED;
import static de.cotto.lndmanagej.model.ResolutionFixtures.OUTGOING_HTLC_FIRST_STAGE;
import static de.cotto.lndmanagej.model.ResolutionFixtures.OUTGOING_HTLC_TIMEOUT;
import static de.cotto.lndmanagej.model.ResolutionFixtures.RESOLUTION_EMPTY;
import static org.assertj.core.api.Assertions.assertThat;

class ResolutionTest {

    @Test
    void sweepTransaction_empty() {
        assertThat(RESOLUTION_EMPTY.sweepTransaction()).isEmpty();
    }

    @Test
    void sweepTransaction() {
        assertThat(INCOMING_HTLC_CLAIMED.sweepTransaction()).contains(TRANSACTION_HASH_3);
    }

    @Test
    void resolutionType() {
        assertThat(INCOMING_HTLC_CLAIMED.resolutionType()).isEqualTo("INCOMING_HTLC");
    }

    @Test
    void outcome() {
        assertThat(INCOMING_HTLC_CLAIMED.outcome()).isEqualTo("CLAIMED");
    }

    @Test
    void sweepTransactionClaimsFunds_commit_claimed() {
        assertThat(COMMIT_CLAIMED.sweepTransactionClaimsFunds()).isTrue();
    }

    @Test
    void sweepTransactionClaimsFunds_commit_other() {
        assertThat(new Resolution(Optional.of(TRANSACTION_HASH_3), "COMMIT", "foo").sweepTransactionClaimsFunds())
                .isFalse();
    }

    @Test
    void sweepTransactionClaimsFunds_anchor_claimed() {
        assertThat(ANCHOR_CLAIMED.sweepTransactionClaimsFunds()).isTrue();
    }

    @Test
    void sweepTransactionClaimsFunds_anchor_other() {
        Resolution resolution = new Resolution(Optional.of(TRANSACTION_HASH_3), "ANCHOR", "foo");
        assertThat(resolution.sweepTransactionClaimsFunds()).isFalse();
    }

    @Test
    void sweepTransactionClaimsFunds_incoming_htlc_claimed() {
        assertThat(INCOMING_HTLC_CLAIMED.sweepTransactionClaimsFunds()).isTrue();
    }

    @Test
    void sweepTransactionClaimsFunds_incoming_htlc_timeout() {
        assertThat(INCOMING_HTLC_TIMEOUT.sweepTransactionClaimsFunds()).isFalse();
    }

    @Test
    void sweepTransactionClaimsFunds_outgoing_htlc_claimed() {
        assertThat(OUTGOING_HTLC_CLAIMED.sweepTransactionClaimsFunds()).isFalse();
    }

    @Test
    void sweepTransactionClaimsFunds_outgoing_htlc_timeout() {
        assertThat(OUTGOING_HTLC_TIMEOUT.sweepTransactionClaimsFunds()).isTrue();
    }

    @Test
    void sweepTransactionClaimsFunds_outgoing_htlc_first_stage() {
        assertThat(OUTGOING_HTLC_FIRST_STAGE.sweepTransactionClaimsFunds()).isTrue();
    }

    @Test
    void sweepTransactionClaimsFunds_other() {
        Resolution resolution = new Resolution(Optional.of(TRANSACTION_HASH_3), "x", "y");
        assertThat(resolution.sweepTransactionClaimsFunds())
                .isFalse();
    }

    @Test
    void isClaimedAnchor_false() {
        Resolution resolution = new Resolution(Optional.of(TRANSACTION_HASH_3), "ANCHOR", "foo");
        assertThat(resolution.isClaimedAnchor()).isFalse();
        assertThat(OUTGOING_HTLC_CLAIMED.isClaimedAnchor()).isFalse();
    }

    @Test
    void isClaimedAnchor() {
        assertThat(ANCHOR_CLAIMED.isClaimedAnchor()).isTrue();
    }
}