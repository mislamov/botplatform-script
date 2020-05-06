package ru.maratislamov.script;

import ru.maratislamov.script.values.MapValue;

import java.util.HashMap;

public class ScriptSession<T> {

    private T uid;

    private MapValue variables = new MapValue(new HashMap<>());

    private int currentStatement;

    public ScriptSession(T uid) {
        this.uid = uid;
        this.currentStatement = 0;
    }

    public ScriptSession(T uid, MapValue variables, int currentStatement) {
        this.uid = uid;
        this.variables = variables;
        this.currentStatement = currentStatement;
    }

    public int getCurrentStatement() {
        return currentStatement;
    }

    public void setCurrentStatement(int currentStatement) {
        this.currentStatement = currentStatement;
    }

    public MapValue getVariables() {
        return variables;
    }

    public void setVariables(MapValue variables) {
        this.variables = variables;
    }

    public void incCurrentStatement() {
        ++currentStatement;
    }

    public void decCurrentStatement() {
        --currentStatement;
    }

    public T getUid() {
        return uid;
    }

    public void setUid(T uid) {
        this.uid = uid;
    }
}
