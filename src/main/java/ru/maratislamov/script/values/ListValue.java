package ru.maratislamov.script.values;

import org.apache.commons.lang3.ObjectUtils;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.values.google.SparseArray;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ListValue extends AbstractList<Value> implements Value, MapOrListValueInterface {

    SparseArray<Value> data;

    public static ListValue of(Value val) {
        if (val instanceof ListValue) return (ListValue) val;

        final ListValue lv = new ListValue();
        lv.push(val);
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
        Integer idx = Integer.parseInt(name);
        return idx <= data.maxIndex();
    }

    @Override
    public Value get(String name) {
        Integer idx = Integer.parseInt(name);
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
        return null;
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
};


