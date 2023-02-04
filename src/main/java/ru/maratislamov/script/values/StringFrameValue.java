package ru.maratislamov.script.values;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.expressions.Expression;
import ru.maratislamov.script.statements.Statement;

import java.util.List;
import java.util.Objects;

public class StringFrameValue extends StringValue implements Statement {

    private List<Expression> content;

    public StringFrameValue() {
        super();
    }

    public StringFrameValue(List<Expression> content) {
        this();
        this.content = content;
    }

    @JsonIgnore
    @Override
    public String getValue() {
        throw new RuntimeException("NYR");
    }

    @JsonIgnore
    @Override
    public void setValue(String value) {
        throw new RuntimeException("NYR");
    }

    @Override
    public Value evaluate(ScriptSession session) {
        String text = content.stream().map(s -> Expression.evaluate(s , session).toString()).reduce((value, value2) -> value + value2).orElse("");
        return new StringValue(text);
    }

    @Override
    public String toString() {
        final String[] result = {""};
        if (content == null) return "";
        content.forEach(ln -> {
            result[0] += ln;
        });
        return result[0];
    }

    public List<Expression> getContent() {
        return content;
    }

    public void setContent(List<Expression> content) {
        this.content = content;
    }

    @Override
    public Value execute(ScriptSession session) {
        return this;
    }

}
