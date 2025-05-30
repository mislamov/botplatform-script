package ru.maratislamov.script.values;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.expressions.Expression;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the base interface for a value. Values are the data that the
 * interpreter processes. They are what gets stored in variables, printed,
 * and operated on.
 * <p>
 * There is an implementation of this interface for each of the different
 * primitive types (really just double and string) that Jasic supports.
 * Wrapping them in a single Value interface lets Jasic be dynamically-typed
 * and convert between different representations as needed.
 * <p>
 * Note that Value extends Expression. This is a bit of a hack, but it lets
 * us use values (which are typically only ever seen by the interpreter and
 * not the parser) as both runtime values, and as object representing
 * literals in code.
 */
//@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public interface Value extends Expression, Serializable {

    /**
     * Value types override this to convert themselves to a string
     * representation.
     */
    String toString();

    /**
     * Value types override this to convert themselves to a numeric
     * representation.
     */
    Double toNumber();

    /**
     * @return java-версия значения
     */
    Object nativeObject();

    Value copy();

    Value NULL = NULLValue.NULL;
    Value NotFound = NotFoundValue.NOT_FOUND_VALUE;

    @SuppressWarnings("unchecked")
    static Value from(Object val) {
        if (val == null) return NULL;
        if (val instanceof Boolean) return new NumberValue((Boolean) val ? 1 : 0);
        if (val instanceof Value) return (Value) val;
        if (val instanceof Number) return new NumberValue(((Number) val).doubleValue());
        if (val instanceof Map) {
            MapValue mapValue = new MapValue();
            Map mapVal = (Map) val;
            mapVal.forEach((k, v) -> mapValue.put((String) k, from(v)));
            return mapValue;
        }
        if (val instanceof Collection) {
            ListValue listValue = new ListValue();
            ((Collection) val).forEach(v -> {
                listValue.push(from(v));
            });
            return listValue;
        }

        return new StringValue(String.valueOf(val));
    }

    static Serializable asObject(Value val) {
        if (val == null || val == NULL) return null;
        if (val instanceof MapValue) return asMap((MapValue) val);
        if (val instanceof ListValue) return asList((ListValue) val);
        if (val instanceof NumberValue) return ((NumberValue) val).getValue();
        return val.toString();

    }

    static ArrayList<Serializable> asList(ListValue val) {
        ArrayList<Serializable> result = new ArrayList<>();
        val.getIterator().forEachRemaining(v -> result.add(asObject(Expression.evaluate(v, null))));
        return result;

    }

    static HashMap<String, Serializable> asMap(MapValue mapValue) {
        HashMap<String, Serializable> result = new HashMap<>();
        mapValue.getAll().forEach(e -> {
            result.put(e.getKey(), asObject(e.getValue()));
        });
        return result;
    }

    /**
     * техническое представление значения
     *
     * @return
     */
    public static String debug(Value val) {
        return val.getClass().getSimpleName() + "(" + val.toString() + ")";
    }
}
