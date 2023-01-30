package ru.maratislamov.script.utils;

import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.expressions.Expression;
import ru.maratislamov.script.expressions.VariableExpression;
import ru.maratislamov.script.values.MapOrListValueInterface;
import ru.maratislamov.script.values.MapValue;
import ru.maratislamov.script.values.Value;
import ru.maratislamov.script.values.VarLinkWrapper;

import java.util.function.Consumer;

import static ru.maratislamov.script.values.Value.NULL;

public class VarMapUtils {

    /**
     * поиск и при необходимости создание ссылки на значение переменной в скоупе. Точка может быть заменена на новое значение
     * в скоупе через метод {@link VarLinkWrapper#replaceByValue(Value)}
     *
     * @param session           -
     * @param pathVarExpression - переменная (возможно, составная) по которой ищется значение в скоупе
     * @return присвоитель значения в скоупе @sessionScope
     */
    public static Consumer<Value> getValueSetterByPath(final ScriptSession session, final VariableExpression pathVarExpression) {
        String name;
        Value value = session.getSessionScope();
        MapOrListValueInterface prevValue;

        VariableExpression var = pathVarExpression;
        VariableExpression nextVar = var.getNextInPath();
        MapOrListValueInterface scope;

        while (true) {
            name = Expression.evaluate(var.getNameExpression(), session).toString();

            prevValue = (MapOrListValueInterface) value;
            scope = (MapOrListValueInterface) value;
            //value = Expression.evaluate(scope.get(name), session);
            value = scope.get(name);

            // путь не закончен - значит value должен быть составным
            if (nextVar != null) {

                // новое значение
                if (value == null || value == NULL) {
                    value = new MapValue(); // Пока по-умолчанию при доступе инициализируемся как мапа
                    prevValue.put(name, value);

                    // уже занято НЕ коллекцией значением
                } else if (!(value instanceof MapOrListValueInterface)) {
                    throw new RuntimeException("can't get attribute from non collection value by name %s: %s[%s]".formatted(name, value, nextVar));

                }

                // все ок - ныряем дальше
                var = var.getNextInPath();
                nextVar = var.getNextInPath();

            } else {
                // обход завершен
                MapOrListValueInterface finalPrevValue = prevValue;
                String finalName = name;
                return newValue -> finalPrevValue.put(finalName, newValue); // todo убедиться, что работает )
            }

        }

    }
}
