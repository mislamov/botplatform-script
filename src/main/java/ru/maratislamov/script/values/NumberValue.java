package ru.maratislamov.script.values;

import ru.maratislamov.script.ScriptSession;



/**
 * A numeric value. Jasic uses doubles internally for all numbers.
 */
public class NumberValue implements Value {

    private Double value;

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public NumberValue() {
    }

    public NumberValue(Double value) {
        this.value = value;
    }

    public NumberValue(int val) {
        value = (double) val;
    }

    public NumberValue(double val) {
        value = val;
    }

    public NumberValue(String val) {
        value = Double.parseDouble(val);
    }

    @Override
    public String toString() {
        return value.longValue() - value == 0 ? String.valueOf(value.longValue()) : String.valueOf(value);
    }

    public Double toNumber() {
        return value;
    }

    @Override
    public Value copy() {
        return new NumberValue(value);
    }

    public Value evaluate(ScriptSession session) {
        return this;
    }
}
