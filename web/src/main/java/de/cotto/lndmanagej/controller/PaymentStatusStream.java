package de.cotto.lndmanagej.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentStatus;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentStatus.InstantWithString;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class PaymentStatusStream {
    private final ObjectMapper objectMapper;

    public PaymentStatusStream() {
        this.objectMapper = new ObjectMapper();
    }

    public StreamingResponseBody getFor(PaymentStatus paymentStatus) {
        return response -> {
            int seenMessages = 0;
            do {
                List<InstantWithString> messages = new ArrayList<>(paymentStatus.getMessages());
                int oldMessages = seenMessages;
                seenMessages = messages.size();
                for (int i = oldMessages; i < messages.size(); i++) {
                    InstantWithString instantWithString = messages.get(i);
                    Object messageToWrite = new Message(
                            instantWithString.instant().toString(),
                            instantWithString.string()
                    );
                    String json = objectMapper.writeValueAsString(messageToWrite);
                    response.write(json.getBytes(StandardCharsets.UTF_8));
                    response.write('\n');
                    response.flush();
                }
            } while (notDone(paymentStatus, seenMessages));
        };
    }

    private boolean notDone(PaymentStatus paymentStatus, int seenMessages) {
        sleep();
        return paymentStatus.getMessages().size() > seenMessages || paymentStatus.isPending();
    }

    private void sleep() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
            // ignored
        }
    }

    @SuppressWarnings("UnusedVariable")
    private record Message(String timestamp, String message) {
    }
}
