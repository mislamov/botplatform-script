package ru.maratislamov.script.values;

import ru.maratislamov.script.ScriptFunctionsImplemntator;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.expressions.Expression;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A numeric value. Jasic uses doubles internally for all numbers.
 */
public class ListValue implements Expression, Value, MapValueInterface {

    private final List<Expression> list;

    public ListValue(List<Expression> value) {
        this.list = value;
    }

    public ListValue(Expression... values) {
        this.list = new ArrayList<>();
        Collections.addAll(this.list, values);
    }

    @Override
    public String toString() {
        return list == null ? "null" : "[" + list.stream().map(Object::toString).collect(Collectors.joining(", ")) + "]";
    }

    public BigDecimal toNumber() {
        throw new RuntimeException("can't cast List to Number for " + toString());
    }

    @Override
    public Value copy() {
        return new ListValue(new ArrayList<>(list));
    }

    public ListValue evaluate(ScriptSession session) {
        return new ListValue(this.list.stream().map(v -> v.evaluate(session)).collect(Collectors.toList()));
    }

    public Value push(Value value) {
        if (list == null) throw new RuntimeException("Unexpected value == null when push in List");
        list.add(value);
        return value;
    }

    public Value popLast(ScriptSession session) {
        if (list == null) throw new RuntimeException("Unexpected value == null when popLast in List");
        if (list.isEmpty()) return NULL;
        Expression expression = list.remove(list.size() - 1);
        return expression.evaluate(session);
    }

    public Value popFirst(ScriptSession session) {
        if (list == null) throw new RuntimeException("Unexpected value == null when popFirst in List");
        if (list.isEmpty()) return NULL;
        Expression expression = list.remove(0);
        return expression.evaluate(session);
    }

    public void forEach(Consumer<Value> action, ScriptSession session) {
        Objects.requireNonNull(action);
        for (Expression t : list) {
            action.accept(t instanceof Value ? (Value) t : t.evaluate(session));
        }
    }

    @Override
    public boolean containsKey(String name) {
        return false;
    }

    @Override
    public Value get(String name, ScriptSession session/*, ScriptFunctionsImplemntator context*/) {
        if (name.equals("size")) return new NumberValue(list.size());
        return null;
    }

    public List<Expression> getList() {
        return list;
    }

}
