package ru.maratislamov.script.expressions;

import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.values.MapValueInterface;
import ru.maratislamov.script.values.Value;

/**
 * A variable expression evaluates to the current value stored in that
 * variable.
 */
public class VariableExpression implements Expression {

    private final String name;

    public VariableExpression(String name) {
        this.name = name;
    }

    public Value evaluate(ScriptSession session) {
        MapValueInterface variables = session.getSessionScope();

        return findByName(variables, name, session);
    }

    private Value findByName(MapValueInterface variables, String nameSearch, ScriptSession session) {
        // переменная присутствует точь-в-точь по имени
        if (variables.containsKey(nameSearch)) {
            return variables.get(nameSearch, session);
        }

        // задан путь до переменной
        if (nameSearch.contains(".")) {
            String[] path = nameSearch.split("\\.");
            Value value = null;
            for (String vr : path) {
                value = variables.get(vr, session);

                if (value == null) return Value.NULL;

                if (value instanceof MapValueInterface) {
                    variables = (MapValueInterface) value;
                }
            }
            return value == null ? Value.NULL : value;
        }

        return Value.NULL;
    }

    @Override
    public String toString() {
        return name;
    }
}
