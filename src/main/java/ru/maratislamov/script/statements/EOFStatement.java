package ru.maratislamov.script.statements;

import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.values.Value;

/**
 * A "goto" statement jumps execution to another place in the program.
 */
public class EOFStatement implements Statement {

    public EOFStatement() {
    }

    public Value execute(ScriptSession session/*, ScriptFunctionsImplemntator functionsImplemntator*/) {
        return null;
    }
}
