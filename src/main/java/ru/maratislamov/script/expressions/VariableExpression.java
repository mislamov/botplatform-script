package ru.maratislamov.script.expressions;

import ru.maratislamov.script.ScriptFunctionsImplemntator;
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

    public Value evaluate(ScriptSession session, ScriptFunctionsImplemntator context) {
        MapValueInterface variables = session.getVariables();

        if (variables.containsKey(name)) {
            return variables.get(name, session, context);
        }

        if (name.contains(".")) {
            String[] path = name.split("\\.");
            Value value = null;
            for (String vr : path) {
                value = variables.get(vr, session, context);

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
