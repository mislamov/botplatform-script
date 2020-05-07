package ru.maratislamov.script.statements;

import ru.maratislamov.script.BotScript;
import ru.maratislamov.script.ScriptFunctionsImplemntator;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.values.Value;

/**
 * A "goto" statement jumps execution to another place in the program.
 */
public class GotoStatement implements Statement {
    private final BotScript botScript;

    private final String label;


    public GotoStatement(BotScript botScript, String label) {
        this.botScript = botScript;
        this.label = label;
    }

    public Value execute(ScriptSession session, ScriptFunctionsImplemntator functionsImplemntator) {
        if (botScript.labels.containsKey(label)) {
            session.setCurrentStatement(botScript.labels.get(label).intValue());
            return null;
        }
        throw new Error("Label " + label + " not found");
    }
}
