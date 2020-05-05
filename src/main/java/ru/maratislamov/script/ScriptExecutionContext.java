package ru.maratislamov.script;

import java.io.IOException;

/**
 * Контекст выполнения скрипта
 */
public abstract class ScriptContextExecution {

    public abstract void onExec(String fname, Object[] args, ScriptSession scriptSession) throws IOException;

}
