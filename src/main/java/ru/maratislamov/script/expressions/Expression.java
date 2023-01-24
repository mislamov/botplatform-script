package ru.maratislamov.script.expressions;

import org.slf4j.Logger;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.values.Value;

/**
 * Base interface for an expression. An expression is like a statement
 * except that it also returns a value when executed. Expressions do not
 * appear at the top level in Jasic programs, but are used in many
 * statements. For example, the value printed by a "print" statement is an
 * expression. Unlike statements, expressions can nest.
 */
public interface Expression {

    static Value evaluate(Expression value, ScriptSession session) {
        return value == null ? null : value.evaluate(session);
    }

    /**
     * Expression classes implement this to evaluate the expression and
     * return the value.
     *
     * @return The value of the calculated expression.
     *
     * @deprecated use Expression.evaluate()
     */
    @Deprecated
    Value evaluate(ScriptSession session);

    /**
     * code name
     * @return
     */
    String getName();
}
