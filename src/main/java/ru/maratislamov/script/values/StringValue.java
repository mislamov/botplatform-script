package ru.maratislamov.script.values;

import ru.maratislamov.script.ScriptSession;
import org.apache.commons.text.StringEscapeUtils;

/**
 * A string value.
 */
public class StringValue implements Value {
    public StringValue(String value) {
        this.value = StringEscapeUtils.unescapeJava(value);
    }

    @Override
    public String toString() {
        return value;
    }

    public double toNumber() {
        return Double.parseDouble(value);
    }

    public Value evaluate(ScriptSession session) {
        return this;
    }

    private final String value;
}
