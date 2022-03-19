package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;

import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE;
import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE_2_3;
import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE_3_4;

public class FlowFixtures {
    public static final Flow FLOW = new Flow(EDGE, Coins.ofSatoshis(1));
    public static final Flow FLOW_2 = new Flow(EDGE_2_3, Coins.ofSatoshis(2));
    public static final Flow FLOW_3 = new Flow(EDGE_3_4, Coins.ofSatoshis(3));
}
