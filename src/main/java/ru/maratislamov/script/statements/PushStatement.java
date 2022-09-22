package ru.maratislamov.script.statements;

import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.expressions.Expression;
import ru.maratislamov.script.values.ListValue;
import ru.maratislamov.script.values.Value;

import java.util.ArrayList;

/**
 * An "input" statement reads input from the user and stores it in a
 * variable.
 */
public class PushStatement implements Statement {

    private final String name;
    private final Expression value;

    public PushStatement(String name, Expression value) {
        this.name = name;
        this.value = value;
    }

    public Value execute(ScriptSession session) {
        ListValue valCollection = (ListValue) session.getVariables().computeIfAbsent(name, k -> new ListValue(new ArrayList<>()));
        return valCollection.push(value.evaluate(session));
    }

}
