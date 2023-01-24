package ru.maratislamov.script.expressions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.values.ListValue;
import ru.maratislamov.script.values.Value;

import java.util.List;
import java.util.stream.Collectors;

public class ListExpressions implements Expression {

    public static final Logger logger = LoggerFactory.getLogger(ListExpressions.class);

    private List<Expression> list;

    public ListExpressions(List<Expression> expressionList) {
        this.list = expressionList;
    }

    @Override
    public Value evaluate(ScriptSession session) {
        final List<Value> collect = list.stream().map(e -> {

            logger.debug("evalute: {}", e);

            return Expression.evaluate(e, session);

        }).collect(Collectors.toList());
        return new ListValue(collect);
    }

    public int size() {
        return list == null ? 0 : list.size();
    }

    public Expression get(int i) {
        return list.get(i);
    }

    @Override
    public String toString() {
        return String.valueOf(list);
    }

    @Override
    public String getName() {
        return toString();
    }
}
