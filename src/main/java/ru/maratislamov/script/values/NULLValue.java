package ru.maratislamov.script.values;

import ru.maratislamov.script.ScriptFunctionsImplemntator;
import ru.maratislamov.script.ScriptSession;

import java.math.BigDecimal;

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

    public BigDecimal toNumber() {
        return null;
    }

    public Value evaluate(ScriptSession session, ScriptFunctionsImplemntator executionContext) {
        return this;
    }

}
