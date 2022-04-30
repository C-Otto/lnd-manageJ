package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.BasicRoute;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Edge;
import de.cotto.lndmanagej.model.Pubkey;

import java.util.ArrayList;
import java.util.List;

public final class BasicRoutes {
    private BasicRoutes() {
        // do not instantiate me
    }

    public static List<BasicRoute> fromFlows(Pubkey source, Pubkey target, Flows flows) {
        Flows flowsCopy = flows.getCopy();
        List<BasicRoute> result = new ArrayList<>();
        List<Edge> path = flowsCopy.getShortestPath(source, target);
        while (!path.isEmpty()) {
            Coins minimum = path.stream().map(flowsCopy::getFlow).reduce(Coins::minimum).orElseThrow();
            for (Edge edge : path) {
                flowsCopy.add(edge, minimum.negate());
            }
            BasicRoute basicRoute = new BasicRoute(path, minimum);
            result.add(basicRoute);
            path = flowsCopy.getShortestPath(source, target);
        }
        return result;
    }
}
