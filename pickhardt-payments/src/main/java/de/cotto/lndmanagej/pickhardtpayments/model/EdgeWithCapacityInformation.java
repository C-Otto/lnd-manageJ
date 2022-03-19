package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;

public record EdgeWithCapacityInformation(Edge edge, Coins availableCapacity) {
}
