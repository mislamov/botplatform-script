package ru.maratislamov.script.expressions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.values.MapOrListValueInterface;
import ru.maratislamov.script.values.StringValue;
import ru.maratislamov.script.values.Value;

/**
 * A variable expression evaluates to the current value stored in that
 * variable.
 */
public class VariableExpression implements Expression {

    private Expression nameExpression = null; // вычислимое выражение для получения имени переменной
    VariableExpression nextInPath = null; // вложенная переменная (доступ по полю)

    public VariableExpression() {
    }

    public VariableExpression(String name) {
        String[] split = name.split("\\.", 2);
        this.nameExpression = new StringValue(split[0]);
        if (split.length > 1) {
            this.nextInPath = new VariableExpression(split[1]);
        }
    }

    public VariableExpression(VariableExpression parent, Expression pathName) {
        assert parent.getNextInPath() == null;

        this.nameExpression = pathName;
        parent.setNextInPath(this);
    }

    public void setNameExpression(Expression nameExpression) {
        this.nameExpression = nameExpression;
    }

    public Value evaluate(ScriptSession session) {
        return evaluate(session, session.getSessionScope());
    }

    public Value evaluate(ScriptSession session, MapOrListValueInterface scope) {
        assert nameExpression != null;

        final String evaluatedName = Expression.evaluate(nameExpression, session).toString();

        final Value value = findByPathName(scope, evaluatedName, session);

        // ныряем в переменную
        if (nextInPath != null) {

            // не проинициализированные переменные и их параметры равны NULL
            if (value == null || value == Value.NULL) {
                return Value.NULL;
            }

            if (!(value instanceof MapOrListValueInterface)) {
                throw new RuntimeException(String.format("can't take path from ATOMIC value: %s[%s]", value, nextInPath));
            }
            return nextInPath.evaluate(session, (MapOrListValueInterface) value);
        }

        return value;
    }

    private Value findByPathName(MapOrListValueInterface variables, String nameSearch, ScriptSession session) {
        // переменная присутствует точь-в-точь по имени
        if (variables.containsKey(nameSearch)) {
            return variables.get(nameSearch)/*.evaluate(session)*/;
        }

        // задан путь до переменной (например, внутри TextFrame)

        if (nameSearch.contains("[") && nameSearch.contains("]")) {

        }


        if (nameSearch.contains(".")) {
            String[] path = nameSearch.split("\\.");
            Value value = null;
            for (String vr : path) {
                value = Expression.evaluate(variables.get(vr), session);

                if (value == null) return Value.NULL;

                if (value instanceof MapOrListValueInterface) {
                    variables = (MapOrListValueInterface) value;
                }
            }
            return value == null ? Value.NULL : value;
        }

        if (variables == session.getSessionScope()) {
            // если обращение к неиницаилизированной переменной
            throw new RuntimeException("Var '" + nameSearch + "' is not defined");
        } else {
            // если обращение к несуществующему полю инициализированной переменной
            return Value.NULL;
        }
    }

    public Expression getNameExpression() {
        return nameExpression;
    }

    public VariableExpression getNextInPath() {
        return nextInPath;
    }

    public void setNextInPath(VariableExpression nextInPath) {
        this.nextInPath = nextInPath;
    }


    @Override
    public String getName() {
        return nameExpression == null ? null : nameExpression.toString();
    }

    @Override
    public String toString() {
        if (nextInPath == null) return "$" + nameExpression;
        return "${" + nameExpression + "." + nextInPath.toString().substring(1) + "}";
    }

    // возвращает самую глубокую вложенную переменную
    @JsonIgnore
    public VariableExpression getLastInPath() {
        if (nextInPath == null) return this;
        return getNextInPath().getLastInPath(); // tail recursion
    }

}
