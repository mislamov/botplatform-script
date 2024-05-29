package ru.maratislamov.script.values;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Comparator;
import java.util.function.Function;

public interface MapOrListValueInterface extends Comparable {

    boolean containsKey(String name);

    default boolean containsMethod(String name){
        return false;
    }

    default Function<? extends MapOrListValueInterface, Value> getMethod(String name){
        return null;
    }

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

    @JsonIgnore
    boolean isEmpty();
}
