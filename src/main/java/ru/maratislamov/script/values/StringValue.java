package ru.maratislamov.script.values;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.text.StringEscapeUtils;
import ru.maratislamov.script.ScriptSession;

import java.util.Objects;


/**
 * A string value.
 */
public class StringValue implements Value {

    public static Integer MAX_LENGTH = null;

    private String value;

    public StringValue() {
        super();
    }

    public StringValue(String value) {
        setValue(StringEscapeUtils.unescapeJava(value));
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        if (value != null && MAX_LENGTH != null && value.length() > MAX_LENGTH)
            throw new RuntimeException("content to big: > " + MAX_LENGTH + " for StringValue: " + value.substring(0, Math.min(30, MAX_LENGTH)) + "...");
    }

    @Override
    public Object nativeObject() {
        return value;
    }

    @JsonIgnore
    @Override
    public String toString() {
        return getValue();
    }


    @Override
    public String getName() {
        return toString();
    }

    @JsonIgnore
    public Double toNumber() {
        try {
            return Double.parseDouble(getValue());
        } catch (NumberFormatException ex) {
            return null; // NYR. Должна ли строка всегда преобразовываться в null если не число?
        }
    }

    @JsonIgnore
    @Override
    public Value copy() {
        return new StringValue(value);
    }

    @JsonIgnore
    public Value evaluate(ScriptSession session) {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringValue that = (StringValue) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
