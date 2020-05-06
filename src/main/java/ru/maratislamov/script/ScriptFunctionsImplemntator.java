package ru.maratislamov.script;

import ru.maratislamov.script.values.Value;

import java.util.List;

/**
 * Контекст выполнения скрипта
 */
public abstract class ScriptExecutionContext {

    public abstract Value onExec(String fname, List<Value> args, ScriptSession scriptSession) throws Exception;

}
