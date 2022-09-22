package ru.maratislamov.script;

import ru.maratislamov.script.values.MapValue;

import java.io.Serializable;
import java.util.HashMap;

public class ScriptSession implements Serializable {

    private String sessionId;

    private MapValue variables = new MapValue(new HashMap<>());

    private int currentStatement;

    private boolean active = false; // активировать сессию нужно явно. Изначально неактивна

    protected ScriptSession() {
    }

    public ScriptSession(String sessionId) {
        this.sessionId = sessionId;
        this.currentStatement = 0;
    }

    public ScriptSession(String sessionId, MapValue variables, int currentStatement) {
        this.sessionId = sessionId;
        this.variables = variables;
        this.currentStatement = currentStatement;
    }

    public int getCurrentStatement() {
        return currentStatement;
    }

    public void setCurrentStatement(int currentStatement) {
        this.currentStatement = currentStatement;
    }

    public void clear(){
        this.variables.getBody().clear();
        currentStatement = 0;
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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
