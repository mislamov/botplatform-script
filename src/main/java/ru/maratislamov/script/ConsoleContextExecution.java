package ru.maratislamov.script;

/**
 * Контекст выполнения скрипта
 */
public abstract class ScriptContextExecution {

    public abstract void onExec(String fname, Object[] args, ScriptSession scriptSession);

    public abstract void onPrint(Object[] args, ScriptSession scriptSession);

    public abstract ScriptSession onInput(Object[] args);

}
