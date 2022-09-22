package ru.maratislamov.script.values;

import ru.maratislamov.script.ScriptSession;



public class NULLValue implements Value {

    public static final NULLValue NULL = new NULLValue();

    public NULLValue() {
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
        return "NULL";
    }
}
