package ru.maratislamov.script.expressions;

import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.values.NULLValue;
import ru.maratislamov.script.values.StringValue;
import ru.maratislamov.script.values.NumberValue;
import ru.maratislamov.script.values.Value;

/**
 * An operator expression evaluates two expressions and then performs some
 * arithmetic operation on the results.
 */
public class OperatorExpression implements Expression {

    private final Expression left;
    private final char operator;
    private final Expression right;

    public OperatorExpression(Expression left, char operator,
                              Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public String toString() {
        return left + " " + operator + " " + right;
    }

    public Value evaluate(ScriptSession session) {
        try {
            Value leftVal = left.evaluate(session);
            Value rightVal = right.evaluate(session);

            switch (operator) {
                case '=':
                case '!':
                    // Coerce to the left argument's type, then compare.
                    if (leftVal instanceof NumberValue) {
                        return new NumberValue((leftVal.toNumber() ==
                                rightVal.toNumber()) == (operator == '=') ? 1 : 0);

                    } else if (leftVal instanceof NULLValue) {
                        return new NumberValue(rightVal instanceof NULLValue ? 1 : 0);

                    } else {
                        return new NumberValue(leftVal.toString().equals(
                                rightVal.toString()) == (operator == '=') ? 1 : 0);
                    }

                case '+':
                    // Addition if the left argument is a number, otherwise do
                    // string concatenation.
                    if (leftVal instanceof NumberValue) {
                        return new NumberValue(leftVal.toNumber() +
                                rightVal.toNumber());
                    } else {
                        return new StringValue(leftVal.toString() +
                                rightVal.toString());
                    }
                case '-':
                    return new NumberValue(leftVal.toNumber() -
                            rightVal.toNumber());
                case '*':
                    return new NumberValue(leftVal.toNumber() *
                            rightVal.toNumber());
                case '/':
                    return new NumberValue(leftVal.toNumber() /
                            rightVal.toNumber());
                case '<':
                    // Coerce to the left argument's type, then compare.
                    if (leftVal instanceof NumberValue) {
                        return new NumberValue((leftVal.toNumber() <
                                rightVal.toNumber()) ? 1 : 0);
                    } else {
                        return new NumberValue((leftVal.toString().compareTo(
                                rightVal.toString()) < 0) ? 1 : 0);
                    }
                case '>':
                    // Coerce to the left argument's type, then compare.
                    if (leftVal instanceof NumberValue) {
                        return new NumberValue((leftVal.toNumber() >
                                rightVal.toNumber()) ? 1 : 0);
                    } else {
                        return new NumberValue((leftVal.toString().compareTo(
                                rightVal.toString()) > 0) ? 1 : 0);
                    }
            }
            throw new Error("Unknown operator: " + operator);

        } catch (Throwable tr) {
            //todo: log.error
            System.out.println("Error evalution for operator " + toString() + ": " + tr.toString());
            return new NumberValue(0);
        }
    }
}
