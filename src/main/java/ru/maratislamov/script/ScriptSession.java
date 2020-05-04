package ru.maratislamov.script;

import ru.maratislamov.script.values.MapValue;

import java.util.HashMap;

public class ScriptSession {

    private MapValue variables = new MapValue(new HashMap<>());

    private int currentStatement;

    public ScriptSession() {
        currentStatement = 0;
    }

    public ScriptSession(MapValue variables, int currentStatement) {
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
}
