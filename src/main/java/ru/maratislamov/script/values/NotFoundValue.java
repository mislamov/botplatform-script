package ru.maratislamov.script.values;

import ru.maratislamov.script.ScriptSession;



public class NotFoundValue implements Value {

    public static final NotFoundValue NOT_FOUND_VALUE = new NotFoundValue();

    public NotFoundValue() {
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
        return "$NotFound";
    }
}
