package ru.maratislamov.script.values;

import ru.maratislamov.script.ScriptFunctionsImplemntator;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.expressions.Expression;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

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
public interface Value extends Expression {

    /**
     * Value types override this to convert themselves to a string
     * representation.
     */
    String toString();

    /**
     * Value types override this to convert themselves to a numeric
     * representation.
     */
    BigDecimal toNumber();

    Value copy();

    /**
     * service internal message value for suspend when acynch call
     */
    Value SUSPEND = new Value() {
        @Override
        public BigDecimal toNumber() {
            return null;
        }

        @Override
        public Value copy() {
            return this;
        }

        @Override
        public Value evaluate(ScriptSession session, ScriptFunctionsImplemntator funcImpl) {
            return null;
        }

        @Override
        public String toString() {
            return "$SUSPEND";
        }
    };

    Value NULL = NULLValue.NULL;

    static Value from(Object val) {
        if (val == null) return NULL;
        if (val instanceof Number) return new NumberValue(((Number) val).doubleValue());
        if (val instanceof Map) {
            MapValue mapValue = new MapValue();
            Map mapVal = (Map) val;
            mapVal.forEach((k, v) -> {
                mapValue.put((String) k, from(v));
            });
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
        if (val == null) return null;
        if (val instanceof MapValue) return asMap((MapValue) val);
        if (val instanceof ListValue) return asList((ListValue) val);
        if (val instanceof NumberValue) return ((NumberValue) val).getValue();
        return val.toString();

    }

    static ArrayList asList(ListValue val) {
        ArrayList result = new ArrayList();
        val.getList().forEach(ex -> {
            result.add(ex.evaluate(null, null));
        });
        return result;

    }

    static HashMap<String, Serializable> asMap(MapValue mapValue) {
        HashMap<String, Serializable> result = new HashMap<>();
        mapValue.getAll().forEach(e -> {
            result.put(e.getKey(), asObject(e.getValue()));
        });
        return result;
    }
}
