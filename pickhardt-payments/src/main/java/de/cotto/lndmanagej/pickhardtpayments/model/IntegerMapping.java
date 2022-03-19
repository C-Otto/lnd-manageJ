package de.cotto.lndmanagej.pickhardtpayments.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class IntegerMapping<K> {
    private final Map<K, Integer> mapping = new LinkedHashMap<>();
    private final Map<Integer, K> reverseMapping = new LinkedHashMap<>();
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
