package ru.maratislamov.script.values;

import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.expressions.Expression;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

public class StringFrameValue extends StringValue {

    private List<Expression> content;

    public StringFrameValue(List<Expression> content) {
        this.content = content;
    }

    @Override
    public String getValue() {
        throw new RuntimeException("NYR");
    }

    @Override
    public void setValue(String value) {
        throw new RuntimeException("NYR");
    }

    @Override
    public Value evaluate(ScriptSession session) {
        String text = content.stream().map(s -> s.evaluate(session).toString()).reduce((value, value2) -> value + value2).orElse("");
        return new StringValue(text);
    }

    @Override
    public String toString() {
        final String[] result = {""};
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
}
