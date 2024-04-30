package ru.maratislamov.script.values;

import java.util.Comparator;

public interface MapOrListValueInterface extends Comparable {

    boolean containsKey(String name);

    Value get(String name);

    /**
     *
     * @param key
     * @param value
     * @return @value
     */
    Value put(String key, Value value);

    default void remove(String key){
        throw new RuntimeException("unexpected");
    }
}
