package ru.maratislamov.script.values;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.expressions.Expression;
import ru.maratislamov.script.expressions.VariableExpression;

import java.util.Iterator;

/**
 * итератор для перебора коллекции в цикле for in
 */
public class IteratorValue implements Value {

    VariableExpression collectionVar;
    int index = -1; // индекс для обхода коллекции


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
        return getCurrentValue(session);
    }

    @Override
    public String getName() {
        throw new RuntimeException("NYR");
    }

    public void setCollectionVar(VariableExpression collectionVar) {
        this.collectionVar = collectionVar;
    }

    public VariableExpression getCollectionVar() {
        return collectionVar;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @JsonIgnore
    public Value getCurrentValue(ScriptSession session) {
        ListValue listValue = getListValue(session);
        if (index < 0 || index >= listValue.size()) {
            return NULL;
        }
        return listValue.get(index);
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

        ListValue listValue = getListValue(session);
        if (listValue.size() <= index + 1) {
            return NULL; // EOC
        }

        return listValue.get(++index);
    }

    private ListValue getListValue(ScriptSession session) {
        final Value evaluatedCollection = collectionVar.evaluate(session);

        if (evaluatedCollection == null || evaluatedCollection == NULL) return new ListValue();

        if (!(evaluatedCollection instanceof ListValue listValue))
            throw new RuntimeException("ListValue expected for loop, but: " + evaluatedCollection);
        return listValue;
    }

}
