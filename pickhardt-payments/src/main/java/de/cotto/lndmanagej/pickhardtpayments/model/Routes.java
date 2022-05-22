package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.EdgeWithLiquidityInformation;
import de.cotto.lndmanagej.model.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class Routes {
    private static final Logger LOGGER = LoggerFactory.getLogger(Routes.class);

    private Routes() {
        // do not instantiate me
    }

    public static List<Route> getFixedWithTotalAmount(Collection<Route> routes, Coins amount) {
        List<Route> routesCopy = new ArrayList<>(routes);
        Route highProbabilityRoute = routesCopy.stream().max(Comparator.comparing(Route::getProbability)).orElseThrow();
        Coins routesAmount = routesCopy.stream().map(Route::getAmount).reduce(Coins.NONE, Coins::add);
        Coins remainder = amount.subtract(routesAmount);
        routesCopy.remove(highProbabilityRoute);
        Route fixedRoute = highProbabilityRoute.getForAmount(highProbabilityRoute.getAmount().add(remainder));
        routesCopy.add(fixedRoute);
        if (notEnoughLiquidityOnFirstHop(routesCopy)) {
            return List.of();
        }
        return routesCopy;
    }

    private static boolean notEnoughLiquidityOnFirstHop(List<Route> routes) {
        for (Route route : routes) {
            List<EdgeWithLiquidityInformation> edgesWithLiquidityInformation = route.getEdgesWithLiquidityInformation();
            EdgeWithLiquidityInformation firstEdge = edgesWithLiquidityInformation.get(0);
            Coins amountForFirstHop = route.getForwardAmountForHop(0);
            Coins availableAmountUpperBound = firstEdge.availableLiquidityUpperBound();
            if (availableAmountUpperBound.compareTo(amountForFirstHop) < 0) {
                LOGGER.warn(
                        "Above liquidity: {} < {} at first hop in {} (in {})",
                        availableAmountUpperBound,
                        amountForFirstHop,
                        route,
                        routes
                );
                return true;
            }
        }
        return false;
    }
}
