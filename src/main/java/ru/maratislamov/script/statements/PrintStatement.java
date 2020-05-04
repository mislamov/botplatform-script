package ru.maratislamov.script.statements;

import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.expressions.Expression;

/**
 * A "print" statement evaluates an expression, converts the result to a
 * string, and displays it to the user.
 */
public class PrintStatement implements Statement {

    private final Expression expression;

    private String type;


    public PrintStatement(Expression expression, String type) {
        this.type = type;
        this.expression = expression;
    }

    public void execute(ScriptSession session) {
        //todo: use type: text, inline, button
        System.out.println(type + ": " + expression.evaluate(session).toString());
    }
}
