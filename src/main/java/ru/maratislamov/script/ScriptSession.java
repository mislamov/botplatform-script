package ru.maratislamov.script;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.maratislamov.script.values.MapValue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

/**
 * сеанс выполнения скрипта
 */
public class ScriptSession implements Serializable {

    /**
     * родительский сеанс, внутри которого был создан этот. Либо null
     */
    private ScriptSession parentSession = null;

    private String sessionId;

    // переменные уровня сеанса выполнения скрипта
    private MapValue sessionScope = new MapValue(new HashMap<>());

    private Integer currentStatement;

    public ScriptSession(){
        this(UUID.randomUUID().toString());
    }

    public ScriptSession(MapValue sessionScope) {
        this.sessionScope = sessionScope;
    }

    public ScriptSession(String sessionId) {
        this.sessionId = sessionId;
        this.currentStatement = null;
    }

    public ScriptSession(String sessionId, MapValue sessionScope, int currentStatement) {
        this.sessionId = sessionId;
        this.sessionScope = sessionScope;
        this.currentStatement = currentStatement;
    }

    /**
     * создаем дочернюю сессию для выполнения вложенных скриптов и подпрограмм
     * @return новая сессия
     */
    @JsonIgnore
    public ScriptSession createSubSession(){
        ScriptSession session = new ScriptSession(getSessionScope());
        session.setParentSession(this);
        return session;
    }

    public int getCurrentStatement() {
        return currentStatement;
    }

    public void setCurrentStatement(Integer currentStatement) {
        this.currentStatement = currentStatement;
    }

    public void clear(){
        this.sessionScope.getBody().clear();
        currentStatement = null;
    }

    public boolean isActive(){
        return currentStatement != null;
    }

    public ScriptSession activate(){
        currentStatement = 0;
        return this;
    }

    public ScriptSession getParentSession() {
        return parentSession;
    }

    public void setParentSession(ScriptSession parentSession) {
        this.parentSession = parentSession;
    }

    public MapValue getSessionScope() {
        return sessionScope;
    }

    public void setSessionScope(MapValue sessionScope) {
        this.sessionScope = sessionScope;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScriptSession that = (ScriptSession) o;
        return Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }

    @Override
    public String toString() {
        return "ScriptSession{" +
                "sessionId='" + sessionId + '\'' +
                ", variables=" + sessionScope +
                ", currentStatement=" + currentStatement +
                '}';
    }

    public void close() {
        currentStatement = null;
    }
}
