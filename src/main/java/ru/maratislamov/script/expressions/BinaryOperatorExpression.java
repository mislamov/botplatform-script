package ru.maratislamov.script.expressions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.values.ListValue;
import ru.maratislamov.script.values.NumberValue;
import ru.maratislamov.script.values.StringValue;
import ru.maratislamov.script.values.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static javax.swing.UIManager.put;

/**
 * An operator expression evaluates two expressions and then performs some
 * arithmetic operation on the results.
 */
public class BinaryOperatorExpression implements Expression {

    private static final Logger logger = LoggerFactory.getLogger(BinaryOperatorExpression.class);

    private Expression left;
    private String operator;
    private Expression right;

    public BinaryOperatorExpression() {
    }

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


    public Expression getLeft() {
        return left;
    }

    public void setLeft(Expression left) {
        this.left = left;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Expression getRight() {
        return right;
    }

    public void setRight(Expression right) {
        this.right = right;
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
        if (Value.NULL.equals(leftVal) || Value.NULL.equals(rightVal)) {

            if (Objects.equals(operator, "=") || operator.equals("=="))
                return new NumberValue(leftVal.equals(rightVal) ? 1 : 0);

            if (Objects.equals(operator, "!=")) return new NumberValue(leftVal.equals(rightVal) ? 0 : 1);

            Value secondValue = leftVal.equals(Value.NULL) ? rightVal : leftVal;
            if (secondValue instanceof NumberValue)
                throw new Error(new StringBuilder().append("Unsupported operation ").append(operator).append(" for NULL and Number: ")
                        .append(left.toString())
                        .append(" (").append(leftVal).append(")")
                        .append(" vs ")
                        .append(right.toString())
                        .append(" (").append(rightVal).append(")")
                        .append("\nexpression: ")
                        .append(this).toString());
        }


        Double leftNumber = leftVal.toNumber();
        Double rightNumber = rightVal.toNumber();

        switch (operator) {
            case "=":
            case "==":
            case "!=":
                // Coerce to the left argument's type, then compare.
                boolean isEqual;

                if (leftVal instanceof NumberValue && rightVal instanceof NumberValue) {
                    isEqual = Objects.equals(leftNumber, rightNumber);

                } else if (Value.NULL.equals(leftVal)) {
                    isEqual = Value.NULL.equals(rightVal);

                } else {
                    isEqual = Objects.equals(leftVal, rightVal);
                }
                return Value.from(isEqual == !Objects.equals(operator, "!="));

            case "+":
                // Addition if the left argument is a number, otherwise do
                // string concatenation.
                if (rightNumber != null && leftVal instanceof NumberValue) {
                    return new NumberValue(leftNumber + rightNumber);
                } else if (leftVal instanceof ListValue) {
                    ListValue lv = new ListValue((List<Value>) leftVal);
                    if (rightVal instanceof ListValue) {
                        lv.addAll((ListValue) rightVal);
                    } else {
                        lv.push(rightVal);
                    }
                    /*return lv;
                } else if (rightVal instanceof ListValue rv) {
                    ListValue lv = leftVal == Value.NULL ? new ListValue() : ListValue.of(leftVal);
                    lv.addAll(rv);*/
                    return lv;
                } else {
                    if (leftNumber == null) leftNumber = 0.0;
                    return new StringValue(leftVal.toString() + rightVal);
                }
            case "-":
                if (leftNumber == null) leftNumber = 0.0;
                return new NumberValue(leftNumber - rightNumber);
            case "*":
                return new NumberValue(leftNumber * rightNumber);
            case "%":
                return new NumberValue(leftNumber % rightNumber);
            case "/":
                return new NumberValue(leftNumber / rightNumber);
            case "<":
                // Coerce to the left argument's type, then compare.
                if (leftVal instanceof NumberValue) {
                    return new NumberValue((leftNumber < rightNumber) ? 1 : 0);
                } else {
                    String left = leftVal.toString();
                    String rigth = rightVal.toString();
                    return new NumberValue((compareTo(left, rigth) < 0) ? 1 : 0);
                }
            case "<=":
                // Coerce to the left argument's type, then compare.
                if (leftVal instanceof NumberValue) {
                    return new NumberValue((leftNumber <= rightNumber) ? 1 : 0);
                } else {
                    String left = leftVal.toString();
                    String rigth = rightVal.toString();
                    return new NumberValue((compareTo(left, rigth) <= 0) ? 1 : 0);
                }
            case ">":
                // Coerce to the left argument's type, then compare.
                if (leftVal instanceof NumberValue) {
                    return new NumberValue((leftNumber > rightNumber) ? 1 : 0);
                } else {
                    String left = leftVal.toString();
                    String rigth = rightVal.toString();
                    return new NumberValue((compareTo(left, rigth) > 0) ? 1 : 0);
                }
            case ">=":
                // Coerce to the left argument's type, then compare.
                if (leftVal instanceof NumberValue) {
                    return new NumberValue((leftNumber >= rightNumber) ? 1 : 0);
                } else {
                    String left = leftVal.toString();
                    String rigth = rightVal.toString();
                    return new NumberValue((compareTo(left, rigth) >= 0) ? 1 : 0);
                }

            case "&&":
                if (leftNumber == null || rightNumber == null) return Value.from(false);
                return new NumberValue(rightNumber != 0 && leftNumber != 0 ? 1 : 0);
            case "||":
                if (leftNumber == null) {
                    return rightNumber == null ? Value.from(false) : new NumberValue(rightNumber != 0 ? 1 : 0);
                }
                return rightNumber == null ? new NumberValue(leftNumber != 0 ? 1 : 0) : new NumberValue(rightNumber != 0 || leftNumber != 0 ? 1 : 0);

        }
        throw new Error("Unknown operator: " + operator);

        /*} catch (Throwable tr) {
            logger.error("Error evalution for operator " + this + ": " + tr);
            return null;
        }*/
    }
}
