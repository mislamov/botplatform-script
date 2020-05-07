package ru.maratislamov.script.statements;

import ru.maratislamov.script.ScriptFunctionsImplemntator;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.expressions.Expression;
import ru.maratislamov.script.values.Value;

/**
 * A "print" statement evaluates an expression, converts the result to a
 * string, and displays it to the user.
 */
public class PrintStatement implements Statement {

    public static String TYPE_TEXT = "text";
    public static String TYPE_INLINE = "inline";
    public static String TYPE_BUTTON = "button";

    private final Expression expression;

    private String type;


    public PrintStatement(Expression expression, String type) {
        this.type = type;
        this.expression = expression;
    }

    public Value execute(ScriptSession session, ScriptFunctionsImplemntator functionsImplemntator) {
        //todo: use type: text, inline, button
        Value value = expression.evaluate(session, functionsImplemntator);
        System.out.println(type + ": " + value.toString());
        return value;
    }
}
