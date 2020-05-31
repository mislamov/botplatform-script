package ru.maratislamov.script.values;

import ru.maratislamov.script.ScriptFunctionsImplemntator;
import ru.maratislamov.script.ScriptSession;

import java.math.BigDecimal;

public class NULLValue implements Value {

    public static final NULLValue NULL = new NULLValue();

    public NULLValue() {
    }

    @Override
    public BigDecimal toNumber() {
        return null;
    }

    @Override
    public Value copy() {
        return this;
    }

    @Override
    public Value evaluate(ScriptSession session, ScriptFunctionsImplemntator funcImpl) {
        return this;
    }

    @Override
    public String toString() {
        return "NULL";
    }
}
