package ru.maratislamov.script.statements;

import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.expressions.Expression;
import ru.maratislamov.script.values.Value;

/**
 * An assignment statement evaluates an expression and stores the result in
 * a variable.
 */
public class AssignStatement implements Statement {

    public AssignStatement(String name, Expression value) {
        this.name = name;
        this.value = value;
    }

    public Value execute(ScriptSession session) {
        String[] names = name.split(".");
        for (String name : names){
            Value var = session.getVariables().get(name);
            //if ()
        }


        return session.getVariables().put(name, value.evaluate(session));
    }

    private final String name;
    private final Expression value;
}
