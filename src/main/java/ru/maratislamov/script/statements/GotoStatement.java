package ru.maratislamov.script.statements;

import ru.maratislamov.script.ScriptEngine;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.values.Value;

/**
 * A "goto" statement jumps execution to another place in the program.
 */
public class GotoStatement implements Statement {
    private final ScriptEngine botScript;

    private final String label;


    public GotoStatement(ScriptEngine botScript, String label) {
        this.botScript = botScript;
        this.label = label;
    }

    public Value execute(ScriptSession session) {
        if (botScript.labels.containsKey(label)) {
            if (session.getParentSession() != null){
                throw new RuntimeException("GOTO deprecated for inline scripts");
            }

            session.setCurrentStatement(botScript.labels.get(label).intValue());
            return null;
        }
        throw new Error("Label " + label + " not found");
    }

    @Override
    public String toString() {
        return "GotoStatement{" +  label + '\'' +
                '}';
    }
}
