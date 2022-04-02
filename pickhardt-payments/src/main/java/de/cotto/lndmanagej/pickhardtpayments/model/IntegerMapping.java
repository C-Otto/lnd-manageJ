package de.cotto.lndmanagej.pickhardtpayments.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class IntegerMapping<K> {
    private static final int INITIAL_CAPACITY = 10_000;
    private final Map<K, Integer> mapping = new LinkedHashMap<>(INITIAL_CAPACITY);
    private final Map<Integer, K> reverseMapping = new LinkedHashMap<>(INITIAL_CAPACITY);
    private int counter;

    public IntegerMapping() {
        // default constructor
    }

    public int getMappedInteger(K key) {
        Integer integer = mapping.computeIfAbsent(key, k -> counter++);
        reverseMapping.put(integer, key);
        return integer;
    }

    public K getKey(int integer) {
        return Objects.requireNonNull(reverseMapping.get(integer));
    }
}
