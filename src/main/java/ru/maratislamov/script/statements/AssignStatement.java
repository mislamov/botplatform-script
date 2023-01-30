package ru.maratislamov.script.statements;

import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.expressions.Expression;
import ru.maratislamov.script.expressions.VariableExpression;
import ru.maratislamov.script.utils.VarMapUtils;
import ru.maratislamov.script.values.Value;

import java.util.function.Consumer;

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
        final Consumer<Value> setter = VarMapUtils.getValueSetterByPath(session, varExpression);
        final Value result = Expression.evaluate(this.value, session);
        setter.accept(result);

        return result;
    }

    public VariableExpression getVarExpression() {
        return varExpression;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "AssignStatement{" +
                "varExpression=" + varExpression +
                ", value=" + value +
                '}';
    }
}
