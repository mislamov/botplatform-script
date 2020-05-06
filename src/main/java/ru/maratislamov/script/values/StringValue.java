package ru.maratislamov.script.values;

import ru.maratislamov.script.ScriptFunctionsImplemntator;
import ru.maratislamov.script.ScriptSession;
import org.apache.commons.text.StringEscapeUtils;

import java.math.BigDecimal;

/**
 * A string value.
 */
public class StringValue implements Value {
    private final String value;

    public StringValue(String value) {
        this.value = StringEscapeUtils.unescapeJava(value);
    }

    @Override
    public String toString() {
        return value;
    }

    public BigDecimal toNumber() {
        return BigDecimal.valueOf(Double.parseDouble(value));
    }

    public Value evaluate(ScriptSession session, ScriptFunctionsImplemntator executionContext) {
        return this;
    }
}
