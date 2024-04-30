package ru.maratislamov.script.statements.loops;

import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.expressions.VariableExpression;
import ru.maratislamov.script.statements.AssignStatement;
import ru.maratislamov.script.statements.Statement;
import ru.maratislamov.script.utils.VarLocalMemoryManager;
import ru.maratislamov.script.values.IteratorValue;
import ru.maratislamov.script.values.Value;

/**
 * инициализация нового итератора
 */
public class CreateIteratorStatement implements Statement {

    String iteratorName; // име переменной-итератора

    VariableExpression collectionVarExpression; // переменная-ссылка на коллекцию

    public CreateIteratorStatement() {
    }

    public CreateIteratorStatement(String iteratorName, VariableExpression collectionVarExpression) {
        this.iteratorName = iteratorName;
        this.collectionVarExpression = collectionVarExpression;
    }

    @Override
    public Value execute(ScriptSession session) {
        final IteratorValue iteratorValue = new IteratorValue(collectionVarExpression);
        session.getVarManager().getValueSetterByPath(session, new VariableExpression(iteratorName)).accept(iteratorValue);
        return iteratorValue;
    }

}
