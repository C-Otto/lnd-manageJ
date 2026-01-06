package de.cotto.lndmanagej.controller;

import java.util.function.Consumer;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

public final class AssertUtil {
    private AssertUtil() {
        // do not instantiate me
    }

    public static Consumer<Object> is(Object expectedValue) {
        return v -> assertThat(v).isEqualTo(expectedValue);
    }

    public static Consumer<Object> containsExactlyInAnyOrder(Object... expected) {
        return v -> assertThatJson(v).isArray().containsExactlyInAnyOrder(expected);
    }
}
