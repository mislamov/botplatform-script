package ru.maratislamov.script.expressions;

import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.values.ListValue;
import ru.maratislamov.script.values.Value;

import java.util.List;
import java.util.stream.Collectors;

public class ListExpressions implements Expression {

    private List<Expression> list;

    public ListExpressions(List<Expression> expressionList) {
        this.list = expressionList;
    }

    @Override
    public Value evaluate(ScriptSession session) {
        return new ListValue(list.stream().map(e -> Expression.evaluate(e, session)).collect(Collectors.toList()));
    }

    public int size() {
        return list == null ? 0 : list.size();
    }

    public Expression get(int i) {
        return list.get(i);
    }
}
