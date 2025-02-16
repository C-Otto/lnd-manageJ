package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.ReactiveStreamReader;
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
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import static de.cotto.lndmanagej.ReactiveStreamReader.readAll;
import static de.cotto.lndmanagej.ReactiveStreamReader.readMessages;
import static de.cotto.lndmanagej.model.EdgeFixtures.EDGE;
import static de.cotto.lndmanagej.model.RouteFixtures.ROUTE;
import static de.cotto.lndmanagej.model.RouteFixtures.ROUTE_2;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentStatusTest {
    private static final HexString PAYMENT_HASH = new HexString("AABBCC001122");
    private static final String ROUTE_PREFIX = "Sending to route #1: 100: ";
    private final PaymentStatus paymentStatus = PaymentStatus.createFor(PAYMENT_HASH);

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
        void messages() {
            assertThat(readMessages(paymentStatus, 1))
                    .map(InstantWithString::string)
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
            assertThat(readMessages(paymentStatus, 2))
                    .map(InstantWithString::string)
                    .contains(ROUTE_PREFIX +
                            "[712345x123x1 (cap 21,000,000), " +
                            "799999x456x3 (cap 21,000,000), " +
                            "799999x456x5 (cap 21,000,000)], " +
                            "400ppm, 600ppm with first hop");
        }

        @Test
        void adds_message_with_min() {
            sendSingleEdge(EdgeWithLiquidityInformation.forLowerBound(EDGE, Coins.ofSatoshis(10)));
            assertThat(readMessages(paymentStatus, 2))
                    .map(InstantWithString::string)
                    .contains(ROUTE_PREFIX + "[712345x123x1 (min 10, cap 21,000,000)], " +
                            "0ppm, 200ppm with first hop");
        }

        @Test
        void adds_message_with_max() {
            sendSingleEdge(EdgeWithLiquidityInformation.forUpperBound(EDGE, Coins.ofSatoshis(11)));
            assertThat(readMessages(paymentStatus, 2))
                    .map(InstantWithString::string)
                    .contains(ROUTE_PREFIX + "[712345x123x1 (max 11, cap 21,000,000)], " +
                            "0ppm, 200ppm with first hop");
        }

        @Test
        void adds_message_with_known() {
            sendSingleEdge(EdgeWithLiquidityInformation.forKnownLiquidity(EDGE, Coins.ofSatoshis(12)));
            assertThat(readMessages(paymentStatus, 2))
                    .map(InstantWithString::string)
                    .contains(ROUTE_PREFIX + "[712345x123x1 (known 12)], 0ppm, 200ppm with first hop");
        }

        @Test
        void adds_message_for_second_route() {
            paymentStatus.sending(ROUTE);
            paymentStatus.sending(ROUTE_2);
            assertThat(readMessages(paymentStatus, 3))
                    .map(InstantWithString::string)
                    .contains("Sending to route #2: 200: " +
                            "[799999x456x2 (cap 21,000,000), " +
                            "799999x456x3 (cap 21,000,000)], " +
                            "200ppm, 400ppm with first hop");
        }

        private void sendSingleEdge(EdgeWithLiquidityInformation edge) {
            paymentStatus.sending(new Route(List.of(edge), Coins.ofSatoshis(100)));
        }
    }

    @Test
    void info() {
        paymentStatus.info("hallo!");
        assertThat(readMessages(paymentStatus, 2))
                .map(InstantWithString::string)
                .contains("hallo!");
    }

    @Test
    void last_message_requested_after_completion_triggered() {
        PaymentStatus paymentStatus = PaymentStatus.createFor(PAYMENT_HASH);
        paymentStatus.info("message");
        ReactiveStreamReader<InstantWithString> instance = new ReactiveStreamReader<>(3, 2);
        paymentStatus.subscribe(instance);
        paymentStatus.failed("failed");
        instance.requestAnotherMessage();
        assertThat(instance.getMessages())
                .map(InstantWithString::string)
                .contains("failed");
    }

    @Nested
    class Failed {
        @Test
        void adds_message() {
            paymentStatus.failed("hallo :(");
            assertThat(readMessages(paymentStatus, 2))
                    .map(InstantWithString::string)
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
            assertThat(readMessages(paymentStatus, 2))
                    .map(InstantWithString::string)
                    .contains("Failed with PERMANENT_CHANNEL_FAILURE");
        }

        @Test
        void message_from_failure_code() {
            paymentStatus.failed(FailureCode.PERMANENT_CHANNEL_FAILURE);
            assertFailure();
        }

        @Test
        @SuppressWarnings("FutureReturnValueIgnored")
        void completes_stream_from_string() {
            Executors.newFixedThreadPool(1).submit(() -> paymentStatus.failed("failure"));
            assertThat(readAll(paymentStatus)).hasSize(2);
        }

        @Test
        @SuppressWarnings("FutureReturnValueIgnored")
        void completes_stream_from_code() {
            Executors.newFixedThreadPool(1).submit(() -> paymentStatus.failed(FailureCode.PERMANENT_CHANNEL_FAILURE));
            assertThat(readAll(paymentStatus)).hasSize(2);
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
            assertThat(readMessages(paymentStatus, 2))
                    .map(InstantWithString::string)
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

        @Test
        @SuppressWarnings("FutureReturnValueIgnored")
        void completes_stream() {
            Executors.newFixedThreadPool(1).submit(paymentStatus::settled);
            assertThat(readAll(paymentStatus)).hasSize(2);
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    class InstantWithStringTest {
        @Test
        void string() {
            InstantWithString instantWithString = new InstantWithString("x");
            assertThat(instantWithString.string()).isEqualTo("x");
        }

        @Test
        void just_string_adds_timestamp() {
            InstantWithString instantWithString = new InstantWithString("x");
            assertThat(instantWithString.instant())
                    .isBetween(Instant.now().minusSeconds(1), Instant.now());
        }
    }

    @Test
    void testEquals() {
        EqualsVerifier.simple()
                .forClass(PaymentStatus.class)
                .withPrefabValues(ReentrantLock.class, new ReentrantLock(), new ReentrantLock())
                .withIgnoredFields("lock")
                .verify();
    }
}
