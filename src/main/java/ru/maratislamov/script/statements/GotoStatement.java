package ru.maratislamov.script.statements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.maratislamov.script.ScriptEngine;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.expressions.Expression;
import ru.maratislamov.script.expressions.VariableExpression;
import ru.maratislamov.script.values.Value;

/**
 * A "goto" statement jumps execution to another place in the program.
 */
public class GotoStatement implements Statement {
    private final static Logger logger = LoggerFactory.getLogger(GotoStatement.class);

    private final ScriptEngine botScript;

    private final Expression label;


    public GotoStatement(ScriptEngine botScript, Expression label) {
        this.botScript = botScript;
        this.label = label;
    }

    public Value execute(ScriptSession session) {
        logger.debug("goto " + label);

        final String key = (botScript.labels.containsKey(label.getName())) ? label.getName() : Expression.evaluate(label, session).getName();

        if (botScript.labels.containsKey(key)) {
            if (session.getParentSession() != null){
                throw new RuntimeException("GOTO deprecated for inline scripts");
            }

            session.setCurrentStatement(botScript.labels.get(key));
            return null;
        }
        throw new Error("Label " + key + " not found");
    }

    @Override
    public String toString() {
        return "GotoStatement{" +  label + '\'' +
                '}';
    }
}
