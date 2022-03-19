package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Pubkey;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class Routes {
    private Routes() {
        // do not instantiate me
    }

    public static Set<Route> fromFlows(Pubkey source, Pubkey target, Flows flows) {
        Flows flowsCopy = flows.getCopy();
        Set<Route> result = new LinkedHashSet<>();
        List<Edge> path = flowsCopy.getShortestPath(source, target);
        while (!path.isEmpty()) {
            Coins minimum = path.stream().map(flowsCopy::getFlow).reduce(Coins::minimum).orElseThrow();
            for (Edge edge : path) {
                flowsCopy.add(edge, minimum.negate());
            }
            Route route = new Route(path, minimum);
            result.add(route);
            path = flowsCopy.getShortestPath(source, target);
        }
        return result;
    }

    public static void ensureTotalAmount(Collection<Route> routes, Coins amount) {
        Route highProbabilityRoute = routes.stream().max(Comparator.comparing(Route::getProbability)).orElseThrow();
        Coins routesAmount = routes.stream().map(Route::amount).reduce(Coins.NONE, Coins::add);
        Coins remainder = amount.subtract(routesAmount);
        routes.remove(highProbabilityRoute);
        Route fixedRoute = highProbabilityRoute.getForAmount(highProbabilityRoute.amount().add(remainder));
        routes.add(fixedRoute);
    }
}
