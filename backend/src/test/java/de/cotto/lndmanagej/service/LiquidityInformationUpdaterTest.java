package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcChannelPolicy;
import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FailureCode;
import de.cotto.lndmanagej.model.HexString;
import de.cotto.lndmanagej.model.LiquidityChangeListener;
import de.cotto.lndmanagej.model.PaymentAttemptHop;
import de.cotto.lndmanagej.model.PaymentListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.FailureCode.CHANNEL_DISABLED;
import static de.cotto.lndmanagej.model.FailureCode.FINAL_EXPIRY_TOO_SOON;
import static de.cotto.lndmanagej.model.FailureCode.FINAL_INCORRECT_CLTV_EXPIRY;
import static de.cotto.lndmanagej.model.FailureCode.FINAL_INCORRECT_HTLC_AMOUNT;
import static de.cotto.lndmanagej.model.FailureCode.INCORRECT_OR_UNKNOWN_PAYMENT_DETAILS;
import static de.cotto.lndmanagej.model.FailureCode.INCORRECT_PAYMENT_AMOUNT;
import static de.cotto.lndmanagej.model.FailureCode.MPP_TIMEOUT;
import static de.cotto.lndmanagej.model.FailureCode.TEMPORARY_CHANNEL_FAILURE;
import static de.cotto.lndmanagej.model.FailureCode.UNKNOWN_NEXT_PEER;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_4;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class LiquidityInformationUpdaterTest {

    private final List<PaymentAttemptHop> hopsWithChannelIdsAndPubkeys = List.of(
            new PaymentAttemptHop(Optional.of(CHANNEL_ID), Coins.ofSatoshis(100), Optional.of(PUBKEY_2)),
            new PaymentAttemptHop(Optional.of(CHANNEL_ID_2), Coins.ofSatoshis(90), Optional.of(PUBKEY_3)),
            new PaymentAttemptHop(Optional.of(CHANNEL_ID_3), Coins.ofSatoshis(80), Optional.of(PUBKEY_4))
    );

    private final List<PaymentAttemptHop> hopsWithChannelIds = List.of(
            new PaymentAttemptHop(Optional.of(CHANNEL_ID), Coins.ofSatoshis(100), Optional.empty()),
            new PaymentAttemptHop(Optional.of(CHANNEL_ID_2), Coins.ofSatoshis(90), Optional.empty()),
            new PaymentAttemptHop(Optional.of(CHANNEL_ID_3), Coins.ofSatoshis(80), Optional.empty())
    );

    private final List<PaymentAttemptHop> hopsJustWithAmount = List.of(
            new PaymentAttemptHop(Optional.empty(), Coins.ofSatoshis(100), Optional.empty()),
            new PaymentAttemptHop(Optional.empty(), Coins.ofSatoshis(90), Optional.empty()),
            new PaymentAttemptHop(Optional.empty(), Coins.ofSatoshis(80), Optional.empty())
    );

    private LiquidityInformationUpdater liquidityInformationUpdater;

    @Mock
    private GrpcGetInfo grpcGetInfo;

    @Mock
    private GrpcChannelPolicy grpcChannelPolicy;

    @Mock
    private LiquidityBoundsService liquidityBoundsService;

    @Mock
    private LiquidityChangeListener liquidityChangeListener;

    @BeforeEach
    void setUp() {
        liquidityInformationUpdater = new LiquidityInformationUpdater(
                grpcGetInfo,
                grpcChannelPolicy,
                liquidityBoundsService,
                List.of(liquidityChangeListener)
        );
        lenient().when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY);
        lenient().when(grpcChannelPolicy.getOtherPubkey(CHANNEL_ID, PUBKEY)).thenReturn(Optional.of(PUBKEY_2));
        lenient().when(grpcChannelPolicy.getOtherPubkey(CHANNEL_ID_2, PUBKEY_2)).thenReturn(Optional.of(PUBKEY_3));
        lenient().when(grpcChannelPolicy.getOtherPubkey(CHANNEL_ID_3, PUBKEY_3)).thenReturn(Optional.of(PUBKEY_4));
    }

    @Test
    void is_payment_listener() {
        assertThat(liquidityInformationUpdater).isInstanceOf(PaymentListener.class);
    }

    @Test
    void forNewPayment_adds_in_flight() {
        liquidityInformationUpdater.forNewPaymentAttempt(hopsWithChannelIds);
        verify(liquidityBoundsService).markAsInFlight(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
        verify(liquidityBoundsService).markAsInFlight(PUBKEY_2, PUBKEY_3, Coins.ofSatoshis(90));
        verify(liquidityBoundsService).markAsInFlight(PUBKEY_3, PUBKEY_4, Coins.ofSatoshis(80));
    }

    @Nested
    class Success {
        private static final HexString PREIMAGE = new HexString("00");

        // CPD-OFF
        @Test
        void success() {
            liquidityInformationUpdater.success(PREIMAGE, hopsWithChannelIdsAndPubkeys);
            verify(liquidityBoundsService).markAsMoved(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
            verify(liquidityBoundsService).markAsMoved(PUBKEY_2, PUBKEY_3, Coins.ofSatoshis(90));
            verify(liquidityBoundsService).markAsMoved(PUBKEY_3, PUBKEY_4, Coins.ofSatoshis(80));
            verifyRemovesInFlightForAllHops();
        }

        @Test
        void just_channel_id() {
            liquidityInformationUpdater.success(PREIMAGE, hopsWithChannelIds);
            verify(liquidityBoundsService).markAsMoved(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
            verify(liquidityBoundsService).markAsMoved(PUBKEY_2, PUBKEY_3, Coins.ofSatoshis(90));
            verify(liquidityBoundsService).markAsMoved(PUBKEY_3, PUBKEY_4, Coins.ofSatoshis(80));
            verifyRemovesInFlightForAllHops();
        }
        // CPD-ON

        @Test
        void without_channel_or_pubkey_information() {
            liquidityInformationUpdater.success(PREIMAGE, hopsJustWithAmount);
            verifyNoInteractions(liquidityBoundsService);
        }
    }

    // CPD-OFF
    @Nested
    class TemporaryChannelFailure {
        @Test
        void from_sending_node() {
            liquidityInformationUpdater.failure(hopsWithChannelIdsAndPubkeys, TEMPORARY_CHANNEL_FAILURE, 0);
            verify(liquidityBoundsService).markAsUnavailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
            verifyRemovesInFlightForAllHops();
            verifyNoMoreInteractions(liquidityBoundsService);
        }

        @Test
        void after_first_hop() {
            liquidityInformationUpdater.failure(hopsWithChannelIdsAndPubkeys, TEMPORARY_CHANNEL_FAILURE, 1);
            verify(liquidityBoundsService).markAsAvailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
            verify(liquidityBoundsService).markAsUnavailable(PUBKEY_2, PUBKEY_3, Coins.ofSatoshis(90));

            verifyRemovesInFlightForAllHops();
            verifyNoMoreInteractions(liquidityBoundsService);
        }

        @Test
        void after_second_hop() {
            liquidityInformationUpdater.failure(hopsWithChannelIdsAndPubkeys, TEMPORARY_CHANNEL_FAILURE, 2);
            verify(liquidityBoundsService).markAsAvailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
            verify(liquidityBoundsService).markAsAvailable(PUBKEY_2, PUBKEY_3, Coins.ofSatoshis(90));
            verify(liquidityBoundsService).markAsUnavailable(PUBKEY_3, PUBKEY_4, Coins.ofSatoshis(80));
            verifyRemovesInFlightForAllHops();
        }

        @Test
        void after_hop_that_does_not_exist() {
            liquidityInformationUpdater.failure(hopsWithChannelIdsAndPubkeys, TEMPORARY_CHANNEL_FAILURE, 99);
            verify(liquidityBoundsService).markAsAvailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
            verify(liquidityBoundsService).markAsAvailable(PUBKEY_2, PUBKEY_3, Coins.ofSatoshis(90));
            verify(liquidityBoundsService).markAsAvailable(PUBKEY_3, PUBKEY_4, Coins.ofSatoshis(80));
            verifyRemovesInFlightForAllHops();
        }

        @Test
        void just_channel_information() {
            liquidityInformationUpdater.failure(hopsWithChannelIds, TEMPORARY_CHANNEL_FAILURE, 2);
            verify(liquidityBoundsService).markAsAvailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
            verify(liquidityBoundsService).markAsAvailable(PUBKEY_2, PUBKEY_3, Coins.ofSatoshis(90));
            verify(liquidityBoundsService).markAsUnavailable(PUBKEY_3, PUBKEY_4, Coins.ofSatoshis(80));
            verifyRemovesInFlightForAllHops();
        }

        @Test
        void without_channel_or_pubkey_information() {
            liquidityInformationUpdater.failure(hopsJustWithAmount, TEMPORARY_CHANNEL_FAILURE, 2);
            verifyNoInteractions(liquidityBoundsService);
        }
    }

    @Nested
    class UnknownNextPeer {
        private final Coins twoSatoshis = Coins.ofSatoshis(2); // see ChannelDisabled

        @Test
        void from_sending_node() {
            liquidityInformationUpdater.failure(hopsWithChannelIdsAndPubkeys, UNKNOWN_NEXT_PEER, 0);
            verify(liquidityBoundsService).markAsUnavailable(PUBKEY, PUBKEY_2, twoSatoshis);
            verifyRemovesInFlightForAllHops();
            verifyNoMoreInteractions(liquidityBoundsService);
        }

        @Test
        void after_first_hop() {
            liquidityInformationUpdater.failure(hopsWithChannelIdsAndPubkeys, UNKNOWN_NEXT_PEER, 1);
            verify(liquidityBoundsService).markAsAvailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
            verify(liquidityBoundsService).markAsUnavailable(PUBKEY_2, PUBKEY_3, twoSatoshis);
            verifyRemovesInFlightForAllHops();
            verifyNoMoreInteractions(liquidityBoundsService);
        }

        @Test
        void after_second_hop() {
            liquidityInformationUpdater.failure(hopsWithChannelIdsAndPubkeys, UNKNOWN_NEXT_PEER, 2);
            verify(liquidityBoundsService).markAsAvailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
            verify(liquidityBoundsService).markAsAvailable(PUBKEY_2, PUBKEY_3, Coins.ofSatoshis(90));
            verify(liquidityBoundsService).markAsUnavailable(PUBKEY_3, PUBKEY_4, twoSatoshis);
            verifyRemovesInFlightForAllHops();
        }

        @Test
        void after_hop_that_does_not_exist() {
            liquidityInformationUpdater.failure(hopsWithChannelIdsAndPubkeys, UNKNOWN_NEXT_PEER, 99);
            verify(liquidityBoundsService).markAsAvailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
            verify(liquidityBoundsService).markAsAvailable(PUBKEY_2, PUBKEY_3, Coins.ofSatoshis(90));
            verify(liquidityBoundsService).markAsAvailable(PUBKEY_3, PUBKEY_4, Coins.ofSatoshis(80));
            verifyRemovesInFlightForAllHops();
        }

        @Test
        void just_channel_information() {
            liquidityInformationUpdater.failure(hopsWithChannelIds, UNKNOWN_NEXT_PEER, 2);
            verify(liquidityBoundsService).markAsAvailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
            verify(liquidityBoundsService).markAsAvailable(PUBKEY_2, PUBKEY_3, Coins.ofSatoshis(90));
            verify(liquidityBoundsService).markAsUnavailable(PUBKEY_3, PUBKEY_4, twoSatoshis);
            verifyRemovesInFlightForAllHops();
        }

        @Test
        void without_channel_or_pubkey_information() {
            liquidityInformationUpdater.failure(hopsJustWithAmount, UNKNOWN_NEXT_PEER, 2);
            verifyNoInteractions(liquidityBoundsService);
        }
    }

    @Nested
    class ChannelDisabled {
        /*
         * Set upper bound to non-zero amount (2sat unavailable => 1sat upper bound) so that the liquidity information
         * for this channel is kept for a while. If we reset the upper bound to 0, this effectively resets the upper
         * bound to the channel capacity - which might cause the algorithm to attempt to route through the disabled
         * channel again.
         */
        private final Coins twoSatoshis = Coins.ofSatoshis(2);

        @Test
        void from_sending_node() {
            liquidityInformationUpdater.failure(hopsWithChannelIdsAndPubkeys, CHANNEL_DISABLED, 0);
            verify(liquidityBoundsService).markAsUnavailable(PUBKEY, PUBKEY_2, twoSatoshis);
            verifyRemovesInFlightForAllHops();
            verifyNoMoreInteractions(liquidityBoundsService);
        }

        @Test
        void after_first_hop() {
            liquidityInformationUpdater.failure(hopsWithChannelIdsAndPubkeys, CHANNEL_DISABLED, 1);
            verify(liquidityBoundsService).markAsAvailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
            verify(liquidityBoundsService).markAsUnavailable(PUBKEY_2, PUBKEY_3, twoSatoshis);
            verifyRemovesInFlightForAllHops();
            verifyNoMoreInteractions(liquidityBoundsService);
        }

        @Test
        void after_second_hop() {
            liquidityInformationUpdater.failure(hopsWithChannelIdsAndPubkeys, CHANNEL_DISABLED, 2);
            verify(liquidityBoundsService).markAsAvailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
            verify(liquidityBoundsService).markAsAvailable(PUBKEY_2, PUBKEY_3, Coins.ofSatoshis(90));
            verify(liquidityBoundsService).markAsUnavailable(PUBKEY_3, PUBKEY_4, twoSatoshis);
            verifyRemovesInFlightForAllHops();
        }

        @Test
        void after_hop_that_does_not_exist() {
            liquidityInformationUpdater.failure(hopsWithChannelIdsAndPubkeys, CHANNEL_DISABLED, 99);
            verify(liquidityBoundsService).markAsAvailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
            verify(liquidityBoundsService).markAsAvailable(PUBKEY_2, PUBKEY_3, Coins.ofSatoshis(90));
            verify(liquidityBoundsService).markAsAvailable(PUBKEY_3, PUBKEY_4, Coins.ofSatoshis(80));
            verifyRemovesInFlightForAllHops();
        }

        @Test
        void just_channel_information() {
            liquidityInformationUpdater.failure(hopsWithChannelIds, CHANNEL_DISABLED, 2);
            verify(liquidityBoundsService).markAsAvailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
            verify(liquidityBoundsService).markAsAvailable(PUBKEY_2, PUBKEY_3, Coins.ofSatoshis(90));
            verify(liquidityBoundsService).markAsUnavailable(PUBKEY_3, PUBKEY_4, twoSatoshis);
            verifyRemovesInFlightForAllHops();
        }

        @Test
        void without_channel_or_pubkey_information() {
            liquidityInformationUpdater.failure(hopsJustWithAmount, CHANNEL_DISABLED, 2);
            verifyNoInteractions(liquidityBoundsService);
        }
    }

    @Nested
    class MppTimeout {
        @Test
        void from_sending_node() {
            liquidityInformationUpdater.failure(hopsWithChannelIdsAndPubkeys, MPP_TIMEOUT, 0);
            verifyRemovesInFlightForAllHops();
            verifyNoMoreInteractions(liquidityBoundsService);
        }

        @Test
        void after_first_hop() {
            liquidityInformationUpdater.failure(hopsWithChannelIdsAndPubkeys, MPP_TIMEOUT, 1);
            verifyRemovesInFlightForAllHops();
            verifyNoMoreInteractions(liquidityBoundsService);
        }

        @Test
        void after_second_hop() {
            liquidityInformationUpdater.failure(hopsWithChannelIdsAndPubkeys, MPP_TIMEOUT, 2);
            verifyRemovesInFlightForAllHops();
            verifyNoMoreInteractions(liquidityBoundsService);
        }

        @Test
        void after_last_hop_from_receiver() {
            assertAllAvailableForFailureFromFinalNode(MPP_TIMEOUT);
        }

        @Test
        void after_hop_that_does_not_exist() {
            liquidityInformationUpdater.failure(hopsWithChannelIdsAndPubkeys, MPP_TIMEOUT, 99);
            verifyRemovesInFlightForAllHops();
            verifyNoMoreInteractions(liquidityBoundsService);
        }

        @Test
        void after_last_hop_from_receiver_just_channel_information() {
            liquidityInformationUpdater.failure(hopsWithChannelIds, MPP_TIMEOUT, 3);
            verify(liquidityBoundsService).markAsAvailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
            verify(liquidityBoundsService).markAsAvailable(PUBKEY_2, PUBKEY_3, Coins.ofSatoshis(90));
            verify(liquidityBoundsService).markAsAvailable(PUBKEY_3, PUBKEY_4, Coins.ofSatoshis(80));
            verifyRemovesInFlightForAllHops();
        }

        @Test
        void after_last_hop_from_receiver_without_channel_or_pubkey_information() {
            liquidityInformationUpdater.failure(hopsJustWithAmount, MPP_TIMEOUT, 3);
            verifyNoInteractions(liquidityBoundsService);
        }
    }

    @Nested
    class IncorrectOrUnknownPaymentDetails {
        @Test
        void from_sending_node() {
            liquidityInformationUpdater.failure(hopsWithChannelIdsAndPubkeys, INCORRECT_OR_UNKNOWN_PAYMENT_DETAILS, 0);
            verifyRemovesInFlightForAllHops();
            verifyNoMoreInteractions(liquidityBoundsService);
        }

        @Test
        void after_first_hop() {
            liquidityInformationUpdater.failure(hopsWithChannelIdsAndPubkeys, INCORRECT_OR_UNKNOWN_PAYMENT_DETAILS, 1);
            verifyRemovesInFlightForAllHops();
            verifyNoMoreInteractions(liquidityBoundsService);
        }

        @Test
        void after_second_hop() {
            liquidityInformationUpdater.failure(hopsWithChannelIdsAndPubkeys, INCORRECT_OR_UNKNOWN_PAYMENT_DETAILS, 2);
            verifyRemovesInFlightForAllHops();
            verifyNoMoreInteractions(liquidityBoundsService);
        }

        @Test
        void after_last_hop_from_receiver() {
            assertAllAvailableForFailureFromFinalNode(INCORRECT_OR_UNKNOWN_PAYMENT_DETAILS);
        }

        @Test
        void after_hop_that_does_not_exist() {
            liquidityInformationUpdater.failure(hopsWithChannelIdsAndPubkeys, INCORRECT_OR_UNKNOWN_PAYMENT_DETAILS, 99);
            verifyRemovesInFlightForAllHops();
            verifyNoMoreInteractions(liquidityBoundsService);
        }

        @Test
        void from_receiver_just_channel_information() {
            liquidityInformationUpdater.failure(hopsWithChannelIds, INCORRECT_OR_UNKNOWN_PAYMENT_DETAILS, 3);
            verify(liquidityBoundsService).markAsAvailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
            verify(liquidityBoundsService).markAsAvailable(PUBKEY_2, PUBKEY_3, Coins.ofSatoshis(90));
            verify(liquidityBoundsService).markAsAvailable(PUBKEY_3, PUBKEY_4, Coins.ofSatoshis(80));
            verifyRemovesInFlightForAllHops();
        }

        @Test
        void after_last_hop_from_receiver_without_channel_or_pubkey_information() {
            liquidityInformationUpdater.failure(hopsJustWithAmount, INCORRECT_OR_UNKNOWN_PAYMENT_DETAILS, 3);
            verifyNoInteractions(liquidityBoundsService);
        }
    }
    // CPD-ON

    @ParameterizedTest
    @EnumSource(value = FailureCode.class, names = {
        "INVALID_REALM",
        "EXPIRY_TOO_SOON",
        "INVALID_ONION_VERSION",
        "INVALID_ONION_HMAC",
        "INVALID_ONION_KEY",
        "AMOUNT_BELOW_MINIMUM",
        "FEE_INSUFFICIENT",
        "INCORRECT_CLTV_EXPIRY",
        "CHANNEL_DISABLED",
        "REQUIRED_NODE_FEATURE_MISSING",
        "REQUIRED_CHANNEL_FEATURE_MISSING",
        "UNKNOWN_NEXT_PEER",
        "TEMPORARY_NODE_FAILURE",
        "PERMANENT_NODE_FAILURE",
        "PERMANENT_CHANNEL_FAILURE",
        "EXPIRY_TOO_FAR",
        "INVALID_ONION_PAYLOAD",
        "UNKNOWN_FAILURE"
    })
    void channel_should_be_treated_as_unavailable(FailureCode failureCode) {
        liquidityInformationUpdater.failure(hopsWithChannelIds, failureCode, 2);
        verifyRemovesInFlightForAllHops();
        verify(liquidityBoundsService).markAsAvailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
        verify(liquidityBoundsService).markAsAvailable(PUBKEY_2, PUBKEY_3, Coins.ofSatoshis(90));

        // see ChannelDisabled test class
        verify(liquidityBoundsService).markAsUnavailable(PUBKEY_3, PUBKEY_4, Coins.ofSatoshis(2));
    }

    @Test
    void incorrect_payment_amount_on_final_node_marks_everything_as_available() {
        assertAllAvailableForFailureFromFinalNode(INCORRECT_PAYMENT_AMOUNT);
    }

    @Test
    void final_incorrect_cltv_expiry_on_final_node_marks_everything_as_available() {
        assertAllAvailableForFailureFromFinalNode(FINAL_INCORRECT_CLTV_EXPIRY);
    }

    @Test
    void final_incorrect_htlc_amount_on_final_node_marks_everything_as_available() {
        assertAllAvailableForFailureFromFinalNode(FINAL_INCORRECT_HTLC_AMOUNT);
    }

    @Test
    void final_expiry_too_soon_on_final_node_marks_everything_as_available() {
        assertAllAvailableForFailureFromFinalNode(FINAL_EXPIRY_TOO_SOON);
    }

    @Test
    void unknown_failure_code() {
        liquidityInformationUpdater.failure(hopsWithChannelIdsAndPubkeys, FailureCode.UNKNOWN_FAILURE, 2);
        verifyRemovesInFlightForAllHops();
    }

    @Test
    void removeInFlight() {
        liquidityInformationUpdater.removeInFlight(hopsWithChannelIdsAndPubkeys);
        verifyRemovesInFlightForAllHops();
        verifyNoMoreInteractions(liquidityBoundsService);
    }

    @Test
    void notifies_liquidity_change_listener_on_in_flight_change_addition() {
        liquidityInformationUpdater.forNewPaymentAttempt(hopsWithChannelIdsAndPubkeys);
        verify(liquidityChangeListener).amountChanged(PUBKEY_2);
    }

    @Test
    void notifies_liquidity_change_listener_on_in_flight_change_removal() {
        liquidityInformationUpdater.removeInFlight(hopsWithChannelIdsAndPubkeys);
        verify(liquidityChangeListener).amountChanged(PUBKEY_2);
    }

    private void assertAllAvailableForFailureFromFinalNode(FailureCode failureCode) {
        liquidityInformationUpdater.failure(hopsWithChannelIdsAndPubkeys, failureCode, 3);
        verify(liquidityBoundsService).markAsAvailable(PUBKEY, PUBKEY_2, Coins.ofSatoshis(100));
        verify(liquidityBoundsService).markAsAvailable(PUBKEY_2, PUBKEY_3, Coins.ofSatoshis(90));
        verify(liquidityBoundsService).markAsAvailable(PUBKEY_3, PUBKEY_4, Coins.ofSatoshis(80));
        verifyRemovesInFlightForAllHops();
    }

    private void verifyRemovesInFlightForAllHops() {
        verify(liquidityBoundsService).markAsInFlight(PUBKEY, PUBKEY_2, Coins.ofSatoshis(-100));
        verify(liquidityBoundsService).markAsInFlight(PUBKEY_2, PUBKEY_3, Coins.ofSatoshis(-90));
        verify(liquidityBoundsService).markAsInFlight(PUBKEY_3, PUBKEY_4, Coins.ofSatoshis(-80));
    }
}
