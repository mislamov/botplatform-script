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
    private final String labelIfNot;

    public IfThenStatement(ScriptEngine botScript, Expression condition, String label) {
        this.botScript = botScript;
        this.condition = condition;
        this.label = label;
        this.labelIfNot = null;
    }

    public IfThenStatement(ScriptEngine botScript, Expression condition, String label, String labelIfNot) {
        this.botScript = botScript;
        this.condition = condition;
        this.label = label;
        this.labelIfNot = labelIfNot;
    }

    public Value execute(ScriptSession session) {

        if (label != null && !botScript.labels.containsKey(label)) {
            throw new RuntimeException("Unexpected label: '" + label + "'");
        }
        if (labelIfNot != null && !botScript.labels.containsKey(labelIfNot)) {
            throw new RuntimeException("Unexpected label: '" + labelIfNot + "'");
        }

        Double value = Expression.evaluate(condition, session).toNumber();

        // true
        if (value != 0) {
            if (label != null) {
                session.setCurrentStatement(botScript.labels.get(label));
            }
            return null;
        }

        // false
        if (labelIfNot != null) {
            session.setCurrentStatement(botScript.labels.get(labelIfNot));
        }
        return null;
    }

    @Override
    public String toString() {
        return "IfThenStatement{" + condition + "->" + label + " $else$ " + labelIfNot + '}';
    }
}
