package ru.maratislamov.script.values;

import ru.maratislamov.script.ScriptFunctionsImplemntator;
import ru.maratislamov.script.ScriptSession;

import java.math.BigDecimal;

public class TermValue implements Value {
    private final String word;

    public TermValue(String word) {
        this.word = word;
    }

    @Override
    public String toString() {
        return word;
    }

    @Override
    public BigDecimal toNumber() {
        throw new ClassCastException("Term to Number: \"" + word + "\".toNumber()");
    }

    @Override
    public Value copy() {
        return new TermValue(word);
    }

    @Override
    public Value evaluate(ScriptSession session) {
        return this;
    }
}
