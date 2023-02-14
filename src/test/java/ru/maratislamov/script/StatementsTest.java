package ru.maratislamov.script;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.maratislamov.script.context.ScriptRunnerContext;
import ru.maratislamov.script.expressions.BinaryOperatorExpression;
import ru.maratislamov.script.expressions.Expression;
import ru.maratislamov.script.expressions.ListExpressions;
import ru.maratislamov.script.parser.Token;
import ru.maratislamov.script.parser.TokenType;
import ru.maratislamov.script.parser.Tokenizer;
import ru.maratislamov.script.statements.AssignStatement;
import ru.maratislamov.script.statements.GotoStatement;
import ru.maratislamov.script.statements.IfThenStatement;
import ru.maratislamov.script.statements.Statement;
import ru.maratislamov.script.values.ListValue;
import ru.maratislamov.script.values.MethodCallValue;
import ru.maratislamov.script.values.NumberValue;
import ru.maratislamov.script.values.Value;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class StatementsTest {

    @Test
    public void test() {
        ScriptEngine scriptEngine = new ScriptEngine();
        List<Statement> statements = scriptEngine.scriptToStatements(new ByteArrayInputStream("\nfile \"file1\" \"caption\" \"wronstr\"\nfile \"file2\"\n\n".getBytes(StandardCharsets.UTF_8)));
        System.out.println(statements);
        assert statements.size() == 2; // print(file()), print(file())

        final Statement statement0 = statements.get(0);
        final Statement statement1 = statements.get(1);

        assert statement0 instanceof MethodCallValue;
        assert statement1 instanceof MethodCallValue;

        assert ((MethodCallValue) statement0).getName().equals("file");
        assert ((MethodCallValue) statement1).getName().equals("file");

        assert ((MethodCallValue) statement0).getArgs().size() == 3;
        assert ((MethodCallValue) statement1).getArgs().size() == 1;
    }

    @Test
    public void testIfDiv() {
        ScriptEngine scriptEngine = new ScriptEngine();
        List<Statement> statements = scriptEngine.scriptToStatements(new ByteArrayInputStream("if x < (3 / 4) then goto xloop".getBytes(StandardCharsets.UTF_8)));
        System.out.println(statements);
        assert statements.size() == 2;
        assert statements.get(0) instanceof IfThenStatement;
        assert statements.get(1) instanceof GotoStatement;
    }

    @Test
    public void testKeyboard() {
        ScriptEngine scriptEngine = new ScriptEngine();
        List<Statement> statements = scriptEngine.scriptToStatements(new ByteArrayInputStream("\nkeyboard([\"a\", \"b\"])\n".getBytes(StandardCharsets.UTF_8)));
        System.out.println(statements);
        assert statements.size() == 1;
        assert statements.get(0) instanceof MethodCallValue;
        final List<Expression> args = ((MethodCallValue) statements.get(0)).getArgs();
        assert args.size() == 1;
        assert args.get(0) instanceof ListExpressions;
        assert ((ListExpressions) args.get(0)).get(0).toString().equals("a");
        assert ((ListExpressions) args.get(0)).get(1).toString().equals("b");
    }


    @Test
    public void testSignNumbers() {
        ScriptEngine scriptEngine = new ScriptEngine();
        List<Statement> statements = scriptEngine.scriptToStatements(new ByteArrayInputStream("x = -111.12; y = +123.456".getBytes(StandardCharsets.UTF_8)));
        System.out.println(statements);
        assert statements.size() == 2;
        assert statements.get(0) instanceof AssignStatement;
        assert statements.get(1) instanceof AssignStatement;

        Assertions.assertEquals("x", ((AssignStatement) statements.get(0)).getVarExpression().getName());
        Assertions.assertEquals("0 - 111,12", ((AssignStatement) statements.get(0)).getValue().toString());
        Assertions.assertEquals("y", ((AssignStatement) statements.get(1)).getVarExpression().getName());
        Assertions.assertEquals("0 + 123,456", ((AssignStatement) statements.get(1)).getValue().toString());
    }

    // keyb []  -->  print(keyb([..]))
    @Test
    public void testKeyboard2() {
        ScriptEngine scriptEngine = new ScriptEngine();
        //Parser.COMMANDS.add("KEYBOARD");
        List<Statement> statements = scriptEngine.scriptToStatements(new ByteArrayInputStream("\nkeyboard [\"a\", \"b\"]\n".getBytes(StandardCharsets.UTF_8)));
        System.out.println(statements);
        assert statements.size() == 1;
        assert statements.get(0) instanceof MethodCallValue;
        List<Expression> args = ((MethodCallValue) statements.get(0)).getArgs();

        assert ((MethodCallValue) statements.get(0)).getName().equals("keyboard");
        assert args.size() == 1;
        assert ((ListExpressions) args.get(0)).get(0).toString().equals("a");
        assert ((ListExpressions) args.get(0)).get(1).toString().equals("b");
    }

    @Test
    public void testBigOperators() {
        testBigOperators("!=");
        testBigOperators("<");
        testBigOperators(">");
        testBigOperators("<=");
        testBigOperators(">=");
        testBigOperators("&&");
        testBigOperators("||");

    }

    public void testBigOperators(String operCode) {
        ScriptEngine scriptEngine = new ScriptEngine();

        List<Statement> statements = scriptEngine.scriptToStatements(new ByteArrayInputStream(("\nprint x " + operCode + " y\n").getBytes(StandardCharsets.UTF_8)));
        System.out.println(statements);
        assert statements.size() == 1;
        assert statements.get(0) instanceof MethodCallValue;
        List<Expression> args = ((MethodCallValue) statements.get(0)).getArgs();

        assert ((MethodCallValue) statements.get(0)).getName().equals("print");
        assert args.size() == 1;
        assert args.get(0) instanceof BinaryOperatorExpression;
        Assertions.assertEquals(operCode, ((BinaryOperatorExpression) args.get(0)).getOperator());


    }


}