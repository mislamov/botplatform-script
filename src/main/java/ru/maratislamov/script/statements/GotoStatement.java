package ru.maratislamov.script.statements;

import ru.maratislamov.script.BotScript;
import ru.maratislamov.script.ScriptSession;

/**
 * A "goto" statement jumps execution to another place in the program.
 */
public class GotoStatement implements Statement {
    private final BotScript botScript;

    public GotoStatement(BotScript botScript, String label) {
        this.botScript = botScript;
        this.label = label;
    }

    public void execute(ScriptSession session) {
        if (botScript.labels.containsKey(label)) {
            session.setCurrentStatement(botScript.labels.get(label).intValue());
        }
    }

    private final String label;
}
