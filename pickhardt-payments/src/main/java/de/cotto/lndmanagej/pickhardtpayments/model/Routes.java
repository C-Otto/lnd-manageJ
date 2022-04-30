package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class Routes {
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
        return routesCopy;
    }
}
