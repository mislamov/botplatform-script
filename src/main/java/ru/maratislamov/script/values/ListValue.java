package ru.maratislamov.script.values;

import ru.maratislamov.script.ScriptSession;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A numeric value. Jasic uses doubles internally for all numbers.
 */
public class ListValue implements Value, MapValueInterface {

    private final List<Value> list;

    public ListValue(List<Value> value) {
        this.list = value;
    }

    @Override
    public String toString() {
        return list == null ? "null" : Arrays.toString(list.toArray());
    }

    public double toNumber() {
        throw new RuntimeException("can't cast List to Number for " + toString());
    }

    public ListValue evaluate(ScriptSession session) {
        return new ListValue(this.list.stream().map(v -> v.evaluate(session)).collect(Collectors.toList()));
    }

    public void push(Value value) {
        if (list == null) throw new RuntimeException("Unexpected value == null when push in List");
        list.add(value);
    }

    @Override
    public boolean containsKey(String name) {
        return false;
    }

    @Override
    public Value get(String name, ScriptSession session) {
        if (name.equals("size")) return new NumberValue(list.size());
        return null;
    }
}
