package de.cotto.lndmanagej.model;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.RouteHintFixtures.ROUTE_HINT;
import static de.cotto.lndmanagej.model.RouteHintFixtures.ROUTE_HINT_2;

public class DecodedPaymentRequestFixtures {
    public static final DecodedPaymentRequest DECODED_PAYMENT_REQUEST = new DecodedPaymentRequest(
            "some payment request",
            144,
            "description",
            PUBKEY,
            Coins.ofMilliSatoshis(123_456),
            new HexString("FF00AB"),
            new HexString("0011AABBCC"),
            LocalDateTime.of(2022, 4, 24, 18, 45, 0).toInstant(ZoneOffset.UTC),
            LocalDateTime.of(2023, 5, 25, 19, 46, 0).toInstant(ZoneOffset.UTC),
            Set.of(ROUTE_HINT, ROUTE_HINT_2)
    );
}
