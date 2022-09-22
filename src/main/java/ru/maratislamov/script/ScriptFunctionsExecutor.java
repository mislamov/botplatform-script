package ru.maratislamov.script;

import ru.maratislamov.script.values.Value;

import java.util.List;

/**
 * Обработчик функций и методов скрипта
 */
public abstract class ScriptFunctionsExecutor {

    public abstract Value onExec(String fname, List<Value> args, ScriptSession scriptSession) throws Exception;

    public ScriptFunctionsExecutor() {
    }


}
