package ru.maratislamov.script.expressions;

import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.statements.Statement;
import ru.maratislamov.script.values.MapOrListValueInterface;
import ru.maratislamov.script.values.StringValue;
import ru.maratislamov.script.values.Value;

/**
 * A variable expression evaluates to the current value stored in that
 * variable.
 */
public class WrapStatement implements Statement {

    Expression expression;

    public Expression getExpression() {
        return expression;
    }

    public WrapStatement(Expression expression) {
        this.expression = expression;
    }

    @Override
    public Value execute(ScriptSession session) {
        return Expression.evaluate(expression, session);
    }
}
