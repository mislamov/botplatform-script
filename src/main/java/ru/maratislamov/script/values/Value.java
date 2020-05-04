package ru.maratislamov.script.values;

import ru.maratislamov.script.expressions.Expression;

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
    double toNumber();
}
