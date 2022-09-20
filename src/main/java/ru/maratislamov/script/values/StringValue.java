package ru.maratislamov.script.values;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.maratislamov.script.ScriptFunctionsImplemntator;
import ru.maratislamov.script.ScriptSession;
import org.apache.commons.text.StringEscapeUtils;

import java.math.BigDecimal;

/**
 * A string value.
 */
public class StringValue implements Value {

    private String value;

    public StringValue() {
    }

    public StringValue(String value) {
        this.value = StringEscapeUtils.unescapeJava(value);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @JsonIgnore
    @Override
    public String toString() {
        return value;
    }

    @JsonIgnore
    public BigDecimal toNumber() {
        try {
            return BigDecimal.valueOf(Double.parseDouble(value));
        } catch (NumberFormatException ex){
            return null; // NYR. Должна ли строка всегда преобразовываться в null если не число?
        }
    }

    @JsonIgnore
    @Override
    public Value copy() {
        return new StringValue(value);
    }

    @JsonIgnore
    public Value evaluate(ScriptSession session, ScriptFunctionsImplemntator funcImpl) {
        return this;
    }
}
