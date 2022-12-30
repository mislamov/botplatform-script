package ru.maratislamov.script.values;

public interface MapOrListValueInterface {

    boolean containsKey(String name);

    Value get(String name);

    /**
     *
     * @param key
     * @param value
     * @return @value
     */
    Value put(String key, Value value);
}
