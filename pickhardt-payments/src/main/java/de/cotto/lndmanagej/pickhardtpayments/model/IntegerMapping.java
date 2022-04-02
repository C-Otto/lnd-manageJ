package de.cotto.lndmanagej.pickhardtpayments.model;

import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectIntHashMap;

import java.util.Objects;

public class IntegerMapping<K> {
    private static final int INITIAL_CAPACITY = 10_000;
    private final ObjectIntHashMap<K> mapping = new ObjectIntHashMap<>(INITIAL_CAPACITY);
    private final IntObjectHashMap<K> reverseMapping = new IntObjectHashMap<>(INITIAL_CAPACITY);
    private int counter;

    public IntegerMapping() {
        // default constructor
    }

    public int getMappedInteger(K key) {
        return mapping.getIfAbsentPut(key, () -> {
            int value = counter++;
            reverseMapping.put(value, key);
            return value;
        });
    }

    public K getKey(int integer) {
        return Objects.requireNonNull(reverseMapping.get(integer));
    }
}
