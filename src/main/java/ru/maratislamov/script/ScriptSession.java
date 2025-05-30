package ru.maratislamov.script;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.maratislamov.script.context.ScriptRunnerContext;
import ru.maratislamov.script.utils.VarLocalMemoryManager;
import ru.maratislamov.script.utils.VarManager;
import ru.maratislamov.script.values.MapValue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

/**
 * сеанс выполнения скрипта
 */
public class ScriptSession implements Serializable {

    Logger LOG  = LoggerFactory.getLogger(ScriptSession.class);

    static final long SerialVersionUID = 1122334455L;

    /**
     * родительский сеанс, внутри которого был создан этот. Либо null
     */
    private ScriptSession parentSession = null;

    /**
     * контекст не должен сохраняться. При чтении сессии важно устанавливать актуальный (читающий) контекст
     */
    @JsonIgnore
    private ScriptRunnerContext runnerContext;

    private String sessionId;

    // переменные уровня сеанса выполнения скрипта
    private MapValue sessionScope = new MapValue();

    @JsonIgnore
    // вечные переменные (глобального уровня)
    public MapValue activationSessionScope = new MapValue();

    private Integer currentStatement;

    private long step = 0;

    private Character suspendType; // W - wait; I - input

    /**
     * менеджер переменных
     */
    private static VarManager _varManager = null;


    private ScriptSession() {
        this(UUID.randomUUID().toString());
    }

    public ScriptSession(ScriptRunnerContext runnerContext) {
        this(UUID.randomUUID().toString());
        this.runnerContext = runnerContext;
    }

    public ScriptSession(MapValue sessionScope) {
        this(UUID.randomUUID().toString());
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
        LOG.trace("new ScriptSession(currentStatement = " + currentStatement + ")");
    }

    /**
     * создаем дочернюю сессию для выполнения вложенных скриптов и подпрограмм
     *
     * @return новая сессия
     */
    @JsonIgnore
    public ScriptSession createSubSession() {
        ScriptSession session = new ScriptSession(getSessionScope());
        session.setParentSession(this);
        return session;
    }

    public Integer getCurrentStatement() {
        return currentStatement;
    }

    public void setCurrentStatement(Integer currentStatement) {
        LOG.trace("setCurrentStatement( " + currentStatement + " )");
        this.currentStatement = currentStatement;
    }

    public long getStep() {
        return step;
    }

    public void setStep(long step) {
        this.step = step;
    }

    // проинициализируйте менеджер переменных для использования кастомного хранения значений

    @JsonIgnore
    public VarManager getVarManager() {
        if (_varManager == null) {
            _varManager = VarLocalMemoryManager.getInstance();
        }
        return _varManager;
    }

    public static void setVarManager(VarManager varManager) {
        _varManager = varManager;
    }

    public void clear() {
        this.sessionScope.getBody().clear();
        currentStatement = null;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean isActive() {
        return currentStatement != null;
    }

    public ScriptSession activate() {
        LOG.trace("ScriptSession.activate(): currentStatement = 0");
        currentStatement = 0;
        init();
        return this;
    }

    /**
     * инициализация переменных
     */
    public void init(){
        activationSessionScope.getBody().forEach((k, v) -> {
            this.sessionScope.getBody().put(k, v);
        });
    }



    public ScriptRunnerContext getRunnerContext() {
        return runnerContext == null ? getParentSession().getRunnerContext() : runnerContext;
    }

    public void setRunnerContext(ScriptRunnerContext runnerContext) {
        this.runnerContext = runnerContext;
    }

    public Character getSuspendType() {
        return suspendType;
    }

    public void setSuspendType(Character suspendType) {
        this.suspendType = suspendType;
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
        ++step;
        LOG.trace("++currentStatement: " + currentStatement);
    }

    public void decCurrentStatement() {
        --currentStatement;
        LOG.trace("--currentStatement: " + currentStatement);
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
