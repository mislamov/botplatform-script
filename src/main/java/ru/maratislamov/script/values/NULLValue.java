package ru.maratislamov.script.values;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.maratislamov.script.ScriptSession;

/**
 * пустое значение в скрипте (синглтон)
 */
public final class NULLValue implements Value {

    public static final NULLValue NULL = new NULLValue();

    public NULLValue() {
    }

    @JsonCreator
    public static NULLValue getInstance() {
        return NULL;
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
    public String toString() {
        return "$NULL";
    }


    @Override
    public String getName() {
        return toString();
    }

    @Override
    public Object nativeObject() {
        return null;
    }
    @Override
    public boolean equals(Object obj) {
        return obj == null || obj instanceof NULLValue;
    }
}
