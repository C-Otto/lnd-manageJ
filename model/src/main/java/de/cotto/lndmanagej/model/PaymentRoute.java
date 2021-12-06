package de.cotto.lndmanagej.model;

import java.util.List;

public record PaymentRoute(List<PaymentHop> hops) {
}
