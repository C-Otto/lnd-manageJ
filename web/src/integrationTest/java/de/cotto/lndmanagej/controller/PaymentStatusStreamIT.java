package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.HexString;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentStatusStreamIT {
    private static final String NEWLINE = "\n";

    private final PaymentStatusStream paymentStatusStream = new PaymentStatusStream();

    private final Executor executor = Executors.newCachedThreadPool();

    @Test
    void convertsToJsonDelimitedByNewlines() throws IOException {
        PaymentStatus paymentStatus = new PaymentStatus(new HexString("1234567890AABBCC"));
        executor.execute(() -> {
            sleep();
            paymentStatus.info("info1");
            sleep();
            paymentStatus.settled();
        });
        StreamingResponseBody streamingResponseBody = paymentStatusStream.getFor(paymentStatus);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        streamingResponseBody.writeTo(outputStream);
        String line1 = "\\{\"timestamp\":\".*\",\"message\":\"Initializing payment 1234567890aabbcc\"}";
        String line2 = "\\{\"timestamp\":\".*\",\"message\":\"info1\"}";
        String line3 = "\\{\"timestamp\":\".*\",\"message\":\"Settled\"}";
        assertThat(outputStream.toString(StandardCharsets.UTF_8))
                .matches(line1 + NEWLINE + line2 + NEWLINE + line3 + NEWLINE);
    }

    private void sleep() {
        try {
            Thread.sleep(110);
        } catch (InterruptedException ignored) {
            // ignored
        }
    }
}
