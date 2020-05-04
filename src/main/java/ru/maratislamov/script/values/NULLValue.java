package ru.maratislamov.script.values;

import ru.maratislamov.script.ScriptSession;

/**
 * A string value.
 */
public class NULLValue implements Value {
    public NULLValue() {
    }

    @Override
    public String toString() {
        return null;
    }

    public double toNumber() {
        return Double.NaN;
    }

    public Value evaluate(ScriptSession session) {
        return this;
    }

}
