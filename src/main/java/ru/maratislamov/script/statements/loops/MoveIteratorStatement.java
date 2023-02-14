package ru.maratislamov.script.statements.loops;

import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.expressions.VariableExpression;
import ru.maratislamov.script.statements.Statement;
import ru.maratislamov.script.utils.VarMapUtils;
import ru.maratislamov.script.values.IteratorValue;
import ru.maratislamov.script.values.Value;

/**
 * инкремент итератора
 */
public class MoveIteratorStatement implements Statement {

    String varIteratorInternal;
    String varIteratorName;

    /**
     *
     * @param varIteratorInternal - ссылка на реализацию итератора
     * @param varIteratorName - имя переменной, через которую пользователь получает итерируемое значение
     */
    public MoveIteratorStatement(String varIteratorInternal, String varIteratorName) {
        this.varIteratorInternal = varIteratorInternal;
        this.varIteratorName = varIteratorName;
    }

    /**
     * двигает итератор @varIteratorInternal и присваивает новое значение переменной @varIteratorName
     * @param session
     * @return
     */
    @Override
    public Value execute(ScriptSession session) {
        IteratorValue iteratorValue = (IteratorValue) session.getSessionScope().get(varIteratorInternal);
        final Value next = iteratorValue.next(session);
        VarMapUtils.getValueSetterByPath(session, new VariableExpression(varIteratorName)).accept(next);
        return next;
    }
}
