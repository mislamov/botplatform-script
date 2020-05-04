package ru.maratislamov.script.statements;

import ru.maratislamov.script.BotScript;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.expressions.Expression;

/**
 * An if then statement jumps execution to another place in the program, but
 * only if an expression evaluates to something other than 0.
 */
public class IfThenStatement implements Statement {
    private final BotScript botScript;

    private final Expression condition;
    private final String label;

    public IfThenStatement(BotScript botScript, Expression condition, String label) {
        this.botScript = botScript;
        this.condition = condition;
        this.label = label;
    }

    public void execute(ScriptSession session) {
        if (botScript.labels.containsKey(label)) {
            double value = condition.evaluate(session).toNumber();
            if (value != 0) {
                session.setCurrentStatement(botScript.labels.get(label).intValue());
            }
        } else throw new RuntimeException("Unexpected label: '" + label + "'");
    }

    @Override
    public String toString() {
        return "IfThenStatement{" + condition + "->" + label + '}';
    }
}
