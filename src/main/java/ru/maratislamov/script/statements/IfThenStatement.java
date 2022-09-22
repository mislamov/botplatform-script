package ru.maratislamov.script.statements;

import ru.maratislamov.script.ScriptEngine;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.expressions.Expression;
import ru.maratislamov.script.values.Value;

/**
 * An if then statement jumps execution to another place in the program, but
 * only if an expression evaluates to something other than 0.
 */
public class IfThenStatement implements Statement {
    private final ScriptEngine botScript;

    private final Expression condition;
    private final String label;

    public IfThenStatement(ScriptEngine botScript, Expression condition, String label) {
        this.botScript = botScript;
        this.condition = condition;
        this.label = label;
    }

    public Value execute(ScriptSession session) {
        if (botScript.labels.containsKey(label)) {
            Double value = condition.evaluate(session).toNumber();
            if (value != 0) {
                session.setCurrentStatement(botScript.labels.get(label).intValue());
            }
        } else throw new RuntimeException("Unexpected label: '" + label + "'");
        return null;
    }

    @Override
    public String toString() {
        return "IfThenStatement{" + condition + "->" + label + '}';
    }
}
