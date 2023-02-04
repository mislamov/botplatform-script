package ru.maratislamov.script.values;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.maratislamov.script.ScriptSession;

/**
 * синглтон для команды SUSPEND, возвращаемой при остановке выполнения скрипта
 */
public final class SuspendValue implements Value {

    public static final SuspendValue SUSPEND = new SuspendValue();

    public SuspendValue() {
    }

    @Override
    public Double toNumber() {
        return null;
    }

    @Override
    public Value copy() {
        return this;
    }

    @Override
    public Value evaluate(ScriptSession session) {
        return this;
    }


    @Override
    public String getName() {
        return toString();
    }

    @Override
    public String toString() {
        return "$SUSPEND";

    }
}
