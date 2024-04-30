package ru.maratislamov.script.values;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.maratislamov.script.ScriptSession;



public class NotFoundValue implements Value {

    public static final NotFoundValue NOT_FOUND_VALUE = new NotFoundValue();

    @JsonCreator
    public static NotFoundValue getInstance() {
        return NOT_FOUND_VALUE;
    }

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


    public Object nativeObject() {
        throw new RuntimeException("NYR");
    }

    @Override
    public String toString() {
        return "$NotFound";
    }

    @JsonIgnore
    @Override
    public String getName() {
        throw new RuntimeException("Unexpected");
    }
}
