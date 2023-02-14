package ru.maratislamov.script.expressions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.maratislamov.script.values.NumberValue;
import ru.maratislamov.script.values.Value;

class BinaryOperatorExpressionTest {


    @Test
    public void test0() {
        testOper(null, "!=", 2, true);
        testOper(2, "!=", null, true);
        testOper(null, "==", null, true);
        testOper(null, "<=", null, true);
        testOper(null, ">=", null, true);

        Assertions.assertThrows(Error.class, () -> {
            testOper(null, "<", 1, true);
        });
        Assertions.assertThrows(Error.class, () -> {
            testOper(null, "<=", 1, true);
        });
        Assertions.assertThrows(Error.class, () -> {
            testOper(1, ">", null, true);
        });
        Assertions.assertThrows(Error.class, () -> {
            testOper(1, ">=", null, true);
        });
    }

    @Test
    public void test1() {
        testOper(2, "!=", 2.1, true);
        testOper(2.1, "!=", 2, true);
        testOper("2", "==", 2, false);
        testOper("2", "!=", 2, true);
        testOper(2.1, "==", 2.1, true);
        testOper(1, "==", 2, false);
        testOper(2.1, "==", "2.1", false);
        testOper("2.1", "==", "2.1", true);
        testOper(2.1, "<=", 2.1, true);
        testOper(2.1, ">=", 2.1, true);
        testOper(2.1, "<", 2.1, false);
        testOper(2.1, ">", 2.1, false);
        testOper(2, "==", 2.0, true);
        testOper(2 / 3, "!=", "2/3", true);
        testOper("2/3", "==", "2/3", true);

        testOper("111", "<", "222", true);
        testOper("111", ">", "222", false);

        testOper("111", "<", "2222", true);
        testOper("111", ">", "2222", false);
        testOper("2222", ">", "111", true);
        testOper("2", "<=", "111", false);
        testOper("1112", "<", "111", false);

        testOper("1112", ">", "111", true);
        testOper("1112", "<", "1113", true);
        testOper("1112", ">=", "1113", false);

        testOper(null, "<", "1113", true);
        testOper(null, "!=", null, false);


        testOper("3111", ">", "2222", true);
        testOper("3111", ">=", "2222", true);
    }


    public void testOper(Object left, String oper, Object right, boolean expected) {
        final Value lv = Value.from(left);
        final Value rv = Value.from(right);
        BinaryOperatorExpression boe = new BinaryOperatorExpression(lv, oper, rv);
        Assertions.assertTrue(expected != boe.evaluate(null).equals(new NumberValue(0)), "[" + Value.debug(lv) + " " + oper + " " + Value.debug(rv) + " must be " + expected + "]\n");
    }


}