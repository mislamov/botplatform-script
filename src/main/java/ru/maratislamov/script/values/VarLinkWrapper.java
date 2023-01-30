package ru.maratislamov.script.values;

import ru.maratislamov.script.ScriptSession;

// обертка для хранения и обновления ссылок на значения при работе с полем переменных
public class VarLinkWrapper extends NULLValue {

    private MapValue parent = null;
    private Value value = null;

    protected VarLinkWrapper() {
    }

    public VarLinkWrapper(MapValue parent, Value value) {
        this.parent = parent;
        this.value = value;
    }

    public MapValue getParent() {
        return parent;
    }

    public void setParent(MapValue parent) {
        this.parent = parent;
    }

    public Value getValue() {
        return value;
    }

    public void replaceByValue(Value value) {
        this.value = value;
    }

    @Override
    public Double toNumber() {
        throw new RuntimeException("NYR");
    }

    @Override
    public Value copy() {
        throw new RuntimeException("NYR");
    }

    @Override
    public Value evaluate(ScriptSession session) {
        throw new RuntimeException("NYR");
    }

    @Override
    public String toString() {
        return "VarWrapper{" +
                "parent=" + parent +
                ", value=" + value +
                '}';
    }
}
