package ru.maratislamov.script.expressions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.values.ListValue;
import ru.maratislamov.script.values.NumberValue;
import ru.maratislamov.script.values.StringValue;
import ru.maratislamov.script.values.Value;

import java.util.List;
import java.util.Objects;

/**
 * An operator expression evaluates two expressions and then performs some
 * arithmetic operation on the results.
 */
public class BinaryOperatorExpression implements Expression {

    Logger logger = LoggerFactory.getLogger(BinaryOperatorExpression.class);

    private final Expression left;
    private final String operator;
    private final Expression right;

    public BinaryOperatorExpression(Expression left, String operator,
                                    Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public String toString() {
        return left + " " + operator + " " + right;
    }

    @Override
    public String getName() {
        return toString();
    }

    int compareTo(String a, String b) {
        if (a == null || b == null) return 0;
        return a.compareTo(b);
    }

    public Value evaluate(ScriptSession session) {
        //try {
        Value leftVal = left.evaluate(session);
        Value rightVal = right.evaluate(session);

        // операции с NULL
        if (leftVal == Value.NULL || rightVal == Value.NULL) {
            if (Objects.equals(operator, "=") || operator.equals("=="))
                return new NumberValue(leftVal == rightVal ? 1 : 0);
            if (Objects.equals(operator, "!=")) return new NumberValue(leftVal == rightVal ? 0 : 1);
            Value secondValue = leftVal == Value.NULL ? rightVal : leftVal;
            if (secondValue instanceof NumberValue)
                throw new Error("Unsupported operation " + operator + " for NULL and Number: " + left.toString() + " vs " + right.toString());
        }


        final Double rightNumber = rightVal.toNumber();

        switch (operator) {
            case "=":
            case "==":
            case "!=":
                // Coerce to the left argument's type, then compare.
                if (leftVal instanceof NumberValue && rightVal instanceof NumberValue) {
                    return new NumberValue((Objects.equals(leftVal.toNumber(), rightNumber)) == (!Objects.equals(operator, "!=")) ? 1 : 0);

                } else if (leftVal == Value.NULL) {
                    return new NumberValue(rightVal == Value.NULL && !Objects.equals(operator, "!=") ? 1 : 0);

                } else {
                    return new NumberValue(leftVal.toString().equals(
                            rightVal.toString()) == (!Objects.equals(operator, "!=")) ? 1 : 0);
                }

            case "+":
                // Addition if the left argument is a number, otherwise do
                // string concatenation.
                if (rightNumber != null && leftVal instanceof NumberValue) {
                    return new NumberValue(leftVal.toNumber() + rightNumber);
                } else if (leftVal instanceof ListValue) {
                    ListValue lv = new ListValue((List<Value>) leftVal);
                    if (rightVal instanceof ListValue) {
                        lv.addAll((ListValue) rightVal);
                    } else {
                        lv.push(rightVal);
                    }
                    return lv;
                } else {
                    return new StringValue(leftVal.toString() + rightVal);
                }
            case "-":
                return new NumberValue(leftVal.toNumber() - rightNumber);
            case "*":
                return new NumberValue(leftVal.toNumber() * rightNumber);
            case "/":
                return new NumberValue(leftVal.toNumber() / rightNumber);
            case "<":
                // Coerce to the left argument's type, then compare.
                if (leftVal instanceof NumberValue) {
                    return new NumberValue((leftVal.toNumber() < rightNumber) ? 1 : 0);
                } else {
                    String left = leftVal.toString();
                    String rigth = rightVal.toString();
                    return new NumberValue((compareTo(left, rigth) < 0) ? 1 : 0);
                }
            case ">":
                // Coerce to the left argument's type, then compare.
                if (leftVal instanceof NumberValue) {
                    return new NumberValue((leftVal.toNumber() > rightNumber) ? 1 : 0);
                } else {
                    String left = leftVal.toString();
                    String rigth = rightVal.toString();
                    return new NumberValue((compareTo(left, rigth) > 0) ? 1 : 0);
                }
        }
        throw new Error("Unknown operator: " + operator);

        /*} catch (Throwable tr) {
            logger.error("Error evalution for operator " + this + ": " + tr);
            return null;
        }*/
    }
}
