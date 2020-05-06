package ru.maratislamov.script.values;

import ru.maratislamov.script.ScriptFunctionsImplemntator;
import ru.maratislamov.script.ScriptSession;

import java.math.BigDecimal;

/**
 * A numeric value. Jasic uses doubles internally for all numbers.
 */
public class NumberValue implements Value {

    private final BigDecimal value;

    public NumberValue(BigDecimal value) {
        this.value = value;
    }

    public NumberValue(int val) {
        value = new BigDecimal(val);
    }

    public NumberValue(double val) {
        value = new BigDecimal(val);
    }

    public NumberValue(String val) {
        value = BigDecimal.valueOf(Double.parseDouble(val));
    }

    @Override
    public String toString() {
        return value.remainder(BigDecimal.ONE).doubleValue() == 0.0 ? String.valueOf(value.longValueExact()) : String.valueOf(value);
    }

    public BigDecimal toNumber() {
        return value;
    }

    public Value evaluate(ScriptSession session, ScriptFunctionsImplemntator executionContext) {
        return this;
    }
}
