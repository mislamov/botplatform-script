package ru.maratislamov.script.values;

import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.values.google.SparseArray;

import java.util.Iterator;
import java.util.List;

public class ListValue implements Value, MapOrListValueInterface {

    SparseArray<Value> data;

    public ListValue() {
        data = new SparseArray<>(10);
    }

    public ListValue(List<Value> collect) {
        data = new SparseArray<>();
        //collect.forEach(c -> data.push(c));
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
        return data.get(idx);
    }

    @Override
    public Value put(String key, Value value) {
        Integer idx = Integer.parseInt(key);
        data.put(idx, value);
        return value;
    }

    @Override
    public Double toNumber() {
        throw new RuntimeException("NYR");
    }

    @Override
    public Value copy() {
        throw new RuntimeException("NYR");
    }

    public Value push(Value val) {
        data.push(val);
        return val;
    }

    public Iterator<Value> getList() {
        return data.listIterator();
    }

    @Override
    public String toString() {
        return String.valueOf(data);
    }
};


