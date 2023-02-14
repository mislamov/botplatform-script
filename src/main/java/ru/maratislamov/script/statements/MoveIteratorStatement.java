package ru.maratislamov.script.statements;

import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.values.IteratorValue;
import ru.maratislamov.script.values.Value;

/**
 * инкремент итератора
 */
public class MoveIteratorStatement implements Statement {

    IteratorValue iteratorValue;

    public MoveIteratorStatement(IteratorValue iteratorValue) {
        this.iteratorValue = iteratorValue;
    }

    @Override
    public Value execute(ScriptSession session) {
        return iteratorValue.next(session);
    }
}
