package ru.maratislamov.script.statements;

import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.expressions.Expression;
import ru.maratislamov.script.expressions.VariableExpression;
import ru.maratislamov.script.utils.VarMapUtils;
import ru.maratislamov.script.values.Value;
import ru.maratislamov.script.values.VarWrapper;

/**
 * An assignment statement evaluates an expression and stores the result in
 * a variable.
 */
public class AssignStatement implements Statement {

    private final VariableExpression varExpression;
    private final Expression value;

    public AssignStatement(VariableExpression name, Expression value) {
        this.varExpression = name;
        this.value = value;
    }

    public Value execute(ScriptSession session) {
        final Value result = Expression.evaluate(this.value, session);

        VarMapUtils.getValueSetterByPath(session, varExpression).accept(result);

        return result;
    }

    @Override
    public String toString() {
        return "AssignStatement{" +
                "varExpression=" + varExpression +
                ", value=" + value +
                '}';
    }
}
