package ru.maratislamov.script.values;

import ru.maratislamov.script.ScriptSession;

/**
 * A numeric value. Jasic uses doubles internally for all numbers.
 */
public class NumberValue implements Value {
    public NumberValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }

    public double toNumber() {
        return value;
    }

    public Value evaluate(ScriptSession session) {
        return this;
    }

    private final double value;
}
