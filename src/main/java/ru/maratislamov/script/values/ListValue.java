package ru.maratislamov.script.values;

import org.apache.commons.lang3.ObjectUtils;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.values.google.SparseArray;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ListValue extends AbstractList<Value> implements Value, MapOrListValueInterface {

    SparseArray<Value> data;

    public static Map<String, Function<ListValue, Value>> methods;

    static {
        methods = new HashMap<>();
        methods.put("size", v -> Value.from(v.size()));
        methods.put("length", v -> Value.from(v.size()));
        methods.put("regexp", null);
    }

    public static ListValue of(Value... vals) {
        if (vals.length == 1 && vals[0] instanceof ListValue) return (ListValue) vals[0];

        final ListValue lv = new ListValue();

        for (Value val : vals) {
            lv.push(val);
        }
        return lv;
    }

    public ListValue() {
        data = new SparseArray<>(10);
    }

    public ListValue(List<Value> collect) {
        data = new SparseArray<>();
        data.addAll(collect);
    }

    public ListValue(ListValue collect) {
        data = new SparseArray<>();
        data.addAll(collect);
    }

    @Override
    public Value evaluate(ScriptSession session) {
        return this;
    }

    @Override
    public boolean containsKey(String name) {
        if (methods.containsKey(name)) return true;

        int idx = Integer.parseInt(name);
        if (idx < 0) return -idx <= data.maxIndex();
        return idx <= data.maxIndex();
    }

    @Override
    public boolean containsMethod(String name) {
        return methods.containsKey(name);
    }

    @Override
    public Function<ListValue, Value> getMethod(String name) {
        return methods.getOrDefault(name, null);
    }

    @Override
    public Value get(String name) {
        if (methods.containsKey(name)) return methods.get(name).apply(this);

        int idx = Integer.parseInt(name);
        if (idx < 0) {
            return ObjectUtils.firstNonNull(data.get(data.size() + idx), NULL);
        }
        return ObjectUtils.firstNonNull(data.get(idx), NULL);
    }

    @Override
    public Value put(String key, Value value) {
        Integer idx = Integer.parseInt(key);
        data.put(idx, value);
        return value;
    }

    @Override
    public Double toNumber() {
        return (double) size();
    }

    @Override
    public Object nativeObject() {
        return data.stream().map(Value::nativeObject).collect(Collectors.toList());
    }

    @Override
    public Value copy() {
        throw new RuntimeException("NYR");
    }

    public Value push(Value val) {
        data.push(val);
        return val;
    }

    @Override
    public Value set(int index, Value element) {
        return data.set(index, element);
    }

    @Override
    public boolean add(Value val) {
        push(val);
        return true;
    }

    public Iterator<Value> getIterator() {
        return data.listIterator();
    }

    @Override
    public String toString() {
        return String.valueOf(data);
    }

    @Override
    public String getName() {
        return toString();
    }

    @Override
    public Value get(int index) {
        return ObjectUtils.firstNonNull(data.get(index), NULL);
    }

    @Override
    public int size() {
        return data.maxIndex() + 1;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof ListValue) {
            return Integer.compare(size(), ((ListValue) o).size());
        }
        throw new RuntimeException("Unexpected comparation ListValue with " + o.getClass().getSimpleName());
    }
};


