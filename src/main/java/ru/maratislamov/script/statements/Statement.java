package ru.maratislamov.script.statements;

import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.values.Value;

/**
 * Base interface for a Jasic statement. The different supported statement
 * types like "print" and "goto" implement this.
 */
public interface Statement {
    /**
     * Statements implement this to actually perform whatever behavior the
     * statement causes. "print" statements will display text here, "goto"
     * statements will change the current statement, etc.
     */
    Value execute(ScriptSession session);
}
