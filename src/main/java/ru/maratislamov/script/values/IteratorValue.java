package ru.maratislamov.script.values;

import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.expressions.Expression;
import ru.maratislamov.script.expressions.VariableExpression;

import java.util.Iterator;

/**
 * итератор для перебора коллекции в цикле for in
 */
public class IteratorValue implements Value {

    VariableExpression collectionVar;
    Iterator<Value> iterator = null;
    Value currentValue = NotFound;


    public IteratorValue() {
    }

    public IteratorValue(VariableExpression collectionVarExpression) {
        collectionVar = collectionVarExpression;
    }

    @Override
    public Object nativeObject() {
        throw new RuntimeException("NYR");
    }

    @Override
    public Value evaluate(ScriptSession session) {
        return currentValue;
    }

    @Override
    public String getName() {
        throw new RuntimeException("NYR");
    }

    @Override
    public Double toNumber() {
        throw new RuntimeException("NYR");
    }

    @Override
    public Value copy() {
        throw new RuntimeException("NYR");
    }

    public Value next(ScriptSession session) {
        if (iterator == null) {
            final Value evaluatedCollection = collectionVar.evaluate(session);
            if (!(evaluatedCollection instanceof ListValue listValue))
                throw new RuntimeException("ListValue expected for loop, but: " + evaluatedCollection);
            iterator = listValue.getIterator();
        }
        currentValue = iterator.hasNext() ? iterator.next() : NULL;
        return currentValue;
    }
}
