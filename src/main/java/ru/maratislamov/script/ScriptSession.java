package ru.maratislamov.script;

import ru.maratislamov.script.values.MapValue;

import java.util.HashMap;

public class ScriptSession<T> {

    private T user;

    private MapValue variables = new MapValue(new HashMap<>());

    private int currentStatement;

    private boolean active = false; // активировать сессию нужно явно. Изначально неактивна

    public ScriptSession(T user) {
        this.user = user;
        this.currentStatement = 0;
    }

    public ScriptSession(T user, MapValue variables, int currentStatement) {
        this.user = user;
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

    public T getUser() {
        return user;
    }

    public void setUser(T user) {
        this.user = user;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
