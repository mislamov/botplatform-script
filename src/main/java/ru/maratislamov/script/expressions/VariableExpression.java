package ru.maratislamov.script.expressions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.VarNotFoundException;
import ru.maratislamov.script.values.*;

import java.util.ArrayList;
import java.util.List;

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

        if (nextInPath!=null && nextInPath instanceof MethodCallValue)
            throw new RuntimeException("do not use () for functions in var-path!: " + nextInPath);

        // ныряем в переменную
        if (nextInPath != null) {

            // не проинициализированные переменные и их параметры равны NULL
            if (value == null || value == Value.NULL) {
                return Value.NULL;
            }

            // вызов метода
            if (value instanceof MethodCallValue mcv) {
                assert mcv.getArgs().get(0) == NULLValue.NULL;
                Value arg;
                //try {
                //arg = nextInPath.evaluate(session); - не работает когда в аргументе точка
                //} catch (VarNotFoundException ex) {
                arg = Expression.evaluate(nextInPath.nameExpression, session);
                //}

                mcv.getArgs().set(0, arg);
                Value evaluated = mcv.evaluate(session);
                if (nextInPath.nextInPath != null) {
                    assert evaluated instanceof ListValue lv;
                    return nextInPath.nextInPath.evaluate(session, (ListValue) evaluated);
                }
                return evaluated;

            } else if (!(value instanceof MapOrListValueInterface)) {
                throw new RuntimeException(String.format("can't take path from ATOMIC value: %s[%s]", value, nextInPath));
            }
            return nextInPath.evaluate(session, (MapOrListValueInterface) value);
        }

        return value;
    }

    private Value findByPathName(MapOrListValueInterface variables, String nameSearch, ScriptSession session) {

        if (variables.containsMethod(nameSearch) && variables.getMethod(nameSearch) == null) {
            ArrayList<Expression> args = new ArrayList<>();
            args.add(NULLValue.NULL);
            args.add(variables instanceof MapValue mv ? new MapValue(mv) : new ListValue((ListValue) variables));
            return new MethodCallValue(nameSearch, args);
        }

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
            throw new VarNotFoundException("Var '" + nameSearch + "' is not defined");
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
