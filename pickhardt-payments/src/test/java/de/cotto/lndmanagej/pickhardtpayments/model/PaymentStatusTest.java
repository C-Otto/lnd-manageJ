package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.EdgeWithLiquidityInformation;
import de.cotto.lndmanagej.model.FailureCode;
import de.cotto.lndmanagej.model.HexString;
import de.cotto.lndmanagej.model.Route;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static de.cotto.lndmanagej.model.EdgeFixtures.EDGE;
import static de.cotto.lndmanagej.model.RouteFixtures.ROUTE;
import static de.cotto.lndmanagej.model.RouteFixtures.ROUTE_2;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentStatusTest {
    private static final HexString PAYMENT_HASH = new HexString("AABBCC001122");
    private static final String ROUTE_PREFIX = "Sending to route #1: 100: ";
    private final PaymentStatus paymentStatus = new PaymentStatus(PAYMENT_HASH);

    @Nested
    class Initial {
        @Test
        void isSuccess() {
            assertThat(paymentStatus.isSuccess()).isFalse();
        }

        @Test
        void isFailure() {
            assertThat(paymentStatus.isFailure()).isFalse();
        }

        @Test
        void isPending() {
            assertThat(paymentStatus.isPending()).isTrue();
        }

        @Test
        void getMessages() {
            assertThat(paymentStatus.getMessages().stream().map(PaymentStatus.InstantWithString::string))
                    .containsExactly("Initializing payment " + PAYMENT_HASH);
        }

        @Test
        void getNumberOfAttemptedRoutes() {
            assertThat(paymentStatus.getNumberOfAttemptedRoutes()).isEqualTo(0);
        }
    }

    @Nested
    class Sending {
        @Test
        void increases_route_counter() {
            paymentStatus.sending(ROUTE);
            assertThat(paymentStatus.getNumberOfAttemptedRoutes()).isEqualTo(1);
        }

        @Test
        void increases_route_counter_again() {
            paymentStatus.sending(ROUTE);
            paymentStatus.sending(ROUTE_2);
            assertThat(paymentStatus.getNumberOfAttemptedRoutes()).isEqualTo(2);
        }

        @Test
        void adds_message() {
            paymentStatus.sending(ROUTE);
            assertThat(paymentStatus.getMessages().stream().map(PaymentStatus.InstantWithString::string)).contains(
                    ROUTE_PREFIX +
                            "[712345x123x1 (cap 21,000,000), " +
                            "799999x456x3 (cap 21,000,000), " +
                            "799999x456x5 (cap 21,000,000)], " +
                            "400ppm, 600ppm with first hop, probability 0.9999857143544217"
            );
        }

        @Test
        void adds_message_with_min() {
            sendSingleEdge(EdgeWithLiquidityInformation.forLowerBound(EDGE, Coins.ofSatoshis(10)));
            assertThat(paymentStatus.getMessages().stream().map(PaymentStatus.InstantWithString::string)).contains(
                    ROUTE_PREFIX + "[712345x123x1 (min 10, cap 21,000,000)], " +
                            "0ppm, 200ppm with first hop, probability 0.9999957142838776"
            );
        }

        @Test
        void adds_message_with_max() {
            sendSingleEdge(EdgeWithLiquidityInformation.forUpperBound(EDGE, Coins.ofSatoshis(11)));
            assertThat(paymentStatus.getMessages().stream().map(PaymentStatus.InstantWithString::string)).contains(
                    ROUTE_PREFIX + "[712345x123x1 (max 11, cap 21,000,000)], " +
                            "0ppm, 200ppm with first hop, probability 0.0"
            );
        }

        @Test
        void adds_message_with_known() {
            sendSingleEdge(EdgeWithLiquidityInformation.forKnownLiquidity(EDGE, Coins.ofSatoshis(12)));
            assertThat(paymentStatus.getMessages().stream().map(PaymentStatus.InstantWithString::string)).contains(
                    ROUTE_PREFIX + "[712345x123x1 (known 12)], 0ppm, 200ppm with first hop, probability 0.0"
            );
        }

        @Test
        void adds_message_for_second_route() {
            paymentStatus.sending(ROUTE);
            paymentStatus.sending(ROUTE_2);
            assertThat(paymentStatus.getMessages().stream().map(PaymentStatus.InstantWithString::string))
                    .contains("Sending to route #2: 200: " +
                            "[799999x456x2 (cap 21,000,000), " +
                            "799999x456x3 (cap 21,000,000)], " +
                            "200ppm, 400ppm with first hop, probability 0.9999809524725624");
        }

        private void sendSingleEdge(EdgeWithLiquidityInformation edge) {
            paymentStatus.sending(new Route(List.of(edge), Coins.ofSatoshis(100)));
        }
    }

    @Test
    void info() {
        paymentStatus.info("hallo!");
        assertThat(paymentStatus.getMessages().stream().map(PaymentStatus.InstantWithString::string))
                .contains("hallo!");
    }

    @Nested
    class Failed {
        @Test
        void adds_message() {
            paymentStatus.failed("hallo :(");
            assertThat(paymentStatus.getMessages().stream().map(PaymentStatus.InstantWithString::string))
                    .contains("hallo :(");
        }

        @Test
        void marks_as_failed() {
            paymentStatus.failed("hallo :(");
            assertFailure();
        }

        @Test
        void marks_as_failed_from_failure_code() {
            paymentStatus.failed(FailureCode.PERMANENT_CHANNEL_FAILURE);
            assertThat(paymentStatus.getMessages().stream().map(PaymentStatus.InstantWithString::string))
                    .contains("Failed with PERMANENT_CHANNEL_FAILURE");
        }

        @Test
        void message_from_failure_code() {
            paymentStatus.failed(FailureCode.PERMANENT_CHANNEL_FAILURE);
            assertFailure();
        }

        private void assertFailure() {
            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(paymentStatus.isFailure()).isTrue();
            softly.assertThat(paymentStatus.isSuccess()).isFalse();
            softly.assertThat(paymentStatus.isPending()).isFalse();
            softly.assertAll();
        }
    }

    @Nested
    class Settled {
        @Test
        void adds_message() {
            paymentStatus.settled();
            assertThat(paymentStatus.getMessages().stream().map(PaymentStatus.InstantWithString::string))
                    .contains("Settled");
        }

        @Test
        void marks_as_settled() {
            paymentStatus.settled();
            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(paymentStatus.isSuccess()).isTrue();
            softly.assertThat(paymentStatus.isFailure()).isFalse();
            softly.assertThat(paymentStatus.isPending()).isFalse();
            softly.assertAll();
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    class InstantWithStringTest {
        @Test
        void string() {
            PaymentStatus.InstantWithString instantWithString = new PaymentStatus.InstantWithString("x");
            assertThat(instantWithString.string()).isEqualTo("x");
        }

        @Test
        void just_string_adds_timestamp() {
            PaymentStatus.InstantWithString instantWithString = new PaymentStatus.InstantWithString("x");
            assertThat(instantWithString.instant())
                    .isBetween(Instant.now().minusSeconds(1), Instant.now());
        }
    }

    @Test
    void testEquals() {
        EqualsVerifier.simple().forClass(PaymentStatus.class).verify();
    }
}
