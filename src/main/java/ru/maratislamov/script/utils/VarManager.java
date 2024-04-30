package ru.maratislamov.script.utils;

import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.expressions.VariableExpression;
import ru.maratislamov.script.values.MapValue;
import ru.maratislamov.script.values.Value;

import java.util.function.Consumer;

/**
 * управление хранением переменных и их значений
 */
public interface VarManager {
    /**
     * возвращает по коду-наименованию переменной установщик её значения
     * @param session
     * @param pathVarExpression
     * @return
     */
    Consumer<Value> getValueSetterByPath(final ScriptSession session, final VariableExpression pathVarExpression);

}
