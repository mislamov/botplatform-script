package ru.maratislamov.script.statements;

import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.expressions.Expression;
import ru.maratislamov.script.expressions.OperatorExpression;
import ru.maratislamov.script.values.Value;

/**
 * An assignment statement evaluates an expression and stores the result in
 * a variable.
 */
public class AssignStatement implements Statement {

    public AssignStatement(String name, Expression value) {
        this.name = name;
        this.modifyOperator = null;
        this.value = value;
    }

    public AssignStatement(String name, String operator, Expression value) {
        this.name = name;
        this.modifyOperator = operator;
        this.value = value;
    }

    public Value execute(ScriptSession session) {
        String[] names = name.split(".");
        for (String name : names){
            Value var = session.getSessionScope().get(name);
            // init by touch
        }

        Expression val = value;
        if (modifyOperator != null){
            val = new OperatorExpression(session.getSessionScope().getOrDefault(name, Value.NULL), modifyOperator, value);
        }
        return session.getSessionScope().put(name, val.evaluate(session));
    }

    private final String name;
    private final Expression value;
    private final String modifyOperator;

    @Override
    public String toString() {
        return "AssignStatement{" +
                name + " " + (modifyOperator==null?"":modifyOperator) + "= " + value +
                '}';
    }
}
