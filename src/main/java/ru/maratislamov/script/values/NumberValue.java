package ru.maratislamov.script.values;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.maratislamov.script.ScriptSession;

import java.text.DecimalFormat;
import java.util.Objects;


/**
 * A numeric value. Jasic uses doubles internally for all numbers.
 */
public class NumberValue implements Value, Comparable<Value> {

    public static DecimalFormat decimalFormat = new DecimalFormat();

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
        if (value == null) return null;
        return value.longValue() - value == 0 ? String.valueOf(value.longValue()) : decimalFormat.format(value);
    }


    @Override
    public String getName() {
        return toString();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NumberValue that = (NumberValue) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public int compareTo(Value o) {
        return Double.compare(value, o.toNumber());
    }
}
