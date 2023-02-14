package ru.maratislamov.script.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.maratislamov.script.ScriptEngine;
import ru.maratislamov.script.expressions.VariableExpression;
import ru.maratislamov.script.statements.AssignStatement;
import ru.maratislamov.script.statements.Statement;
import ru.maratislamov.script.values.MethodCallValue;
import ru.maratislamov.script.values.StringFrameValue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

public class ParserVarTest {

    @Test
    public void test0() {
        List<Token> tokens = Tokenizer.tokenize(new ByteArrayInputStream("x = 1".getBytes(StandardCharsets.UTF_8)));
        Parser parser = new Parser(new ScriptEngine(), tokens);
        final List<Statement> statements = parser.parseCommands(new HashMap<>());

        assert statements.get(0) instanceof AssignStatement;
        Assertions.assertEquals("x", ((AssignStatement) statements.get(0)).getVarExpression().getName());
        Assertions.assertEquals("1", ((AssignStatement) statements.get(0)).getValue().toString());

    }

    @Test
    public void test1() {
        List<Token> tokens = Tokenizer.tokenize(new ByteArrayInputStream("x.1.2.3 = y.1".getBytes(StandardCharsets.UTF_8)));
        int i = 0;
        Assertions.assertEquals(TokenType.WORD, tokens.get(i++).type);
        Assertions.assertEquals(TokenType.DOT, tokens.get(i++).type);
        Assertions.assertEquals(TokenType.DIGITS, tokens.get(i++).type);
        Assertions.assertEquals(TokenType.DOT, tokens.get(i++).type);
        Assertions.assertEquals(TokenType.DIGITS, tokens.get(i++).type);
        Assertions.assertEquals(TokenType.DOT, tokens.get(i++).type);
        Assertions.assertEquals(TokenType.DIGITS, tokens.get(i++).type);
        Assertions.assertEquals(TokenType.OPERATOR, tokens.get(i++).type);
        Assertions.assertEquals(TokenType.WORD, tokens.get(i++).type);
        Assertions.assertEquals(TokenType.DOT, tokens.get(i++).type);
        Assertions.assertEquals(TokenType.DIGITS, tokens.get(i++).type);

        Parser parser = new Parser(new ScriptEngine(), tokens);
        final List<Statement> statements = parser.parseCommands(new HashMap<>());

        assert statements.get(0) instanceof AssignStatement;
        assert ((AssignStatement) statements.get(0)).getValue() instanceof VariableExpression;

        Assertions.assertEquals("x", ((AssignStatement) statements.get(0)).getVarExpression().getName());
        Assertions.assertEquals("1", ((AssignStatement) statements.get(0)).getVarExpression().getNextInPath().getName());
        Assertions.assertEquals("2", ((AssignStatement) statements.get(0)).getVarExpression().getNextInPath().getNextInPath().getName());
        Assertions.assertEquals("3", ((AssignStatement) statements.get(0)).getVarExpression().getNextInPath().getNextInPath().getNextInPath().getName());

        Assertions.assertEquals("${y.1}", ((AssignStatement) statements.get(0)).getValue().toString());

    }

    @Test
    public void test2() {
        List<Token> tokens = Tokenizer.tokenize(new ByteArrayInputStream("x.1[2].3 = y.1".getBytes(StandardCharsets.UTF_8)));

        Parser parser = new Parser(new ScriptEngine(), tokens);
        final List<Statement> statements = parser.parseCommands(new HashMap<>());

        assert statements.get(0) instanceof AssignStatement;
        assert ((AssignStatement) statements.get(0)).getValue() instanceof VariableExpression;

        Assertions.assertEquals("x", ((AssignStatement) statements.get(0)).getVarExpression().getName());
        Assertions.assertEquals("1", ((AssignStatement) statements.get(0)).getVarExpression().getNextInPath().getName());
        Assertions.assertEquals("2", ((AssignStatement) statements.get(0)).getVarExpression().getNextInPath().getNextInPath().getName());
        Assertions.assertEquals("3", ((AssignStatement) statements.get(0)).getVarExpression().getNextInPath().getNextInPath().getNextInPath().getName());

        Assertions.assertEquals("${y.1}", ((AssignStatement) statements.get(0)).getValue().toString());

    }


    @Test
    public void test3() {

        String code = "print \"\"\"\n" +
                "О        $O\n" +
                "К        $C\n" +
                "Э        $E\n" +
                "А        $D\n" +
                "Н        $N\n" +
                "\"\"\"";


        List<Token> tokens = Tokenizer.tokenize(new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8)));

        Parser parser = new Parser(new ScriptEngine(), tokens);
        final List<Statement> statements = parser.parseCommands(new HashMap<>());

        final MethodCallValue methodCallValue = (MethodCallValue) statements.get(0);
        Assertions.assertEquals("print", methodCallValue.getName());

        assert methodCallValue.getArgs().size() == 1;
        assert methodCallValue.getArgs().get(0) instanceof StringFrameValue;
    }



    @Test
    public void test4() {

        String code = "var = NULL\n input var.x\n print var";


        List<Token> tokens = Tokenizer.tokenize(new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8)));
        Parser parser = new Parser(new ScriptEngine(), tokens);
        final List<Statement> statements = parser.parseCommands(new HashMap<>());

        assert statements.size() == 3;
        Assertions.assertTrue( statements.get(0) instanceof AssignStatement);
        Assertions.assertTrue( statements.get(1) instanceof AssignStatement); // var.x = input()
        Assertions.assertTrue( statements.get(2) instanceof MethodCallValue);


        AssignStatement assignStatement = (AssignStatement) statements.get(1);
        Assertions.assertEquals("${var.x}", assignStatement.getVarExpression().toString());
        Assertions.assertEquals("{CALL input []}", assignStatement.getValue().toString());

        MethodCallValue methodCallValue = (MethodCallValue) statements.get(2);
        Assertions.assertEquals("print", methodCallValue.getName());
    }


    @Test
    public void test5() {

        String code = "m = create_message(\"\")";


        List<Token> tokens = Tokenizer.tokenize(new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8)));
        Parser parser = new Parser(new ScriptEngine(), tokens);
        final List<Statement> statements = parser.parseCommands(new HashMap<>());

        assert statements.size() == 1;
        Assertions.assertTrue( statements.get(0) instanceof AssignStatement);

        AssignStatement assignStatement = (AssignStatement) statements.get(0);
        Assertions.assertEquals("m", assignStatement.getVarExpression().getName());
        Assertions.assertTrue(assignStatement.getValue() instanceof MethodCallValue);
        Assertions.assertEquals("create_message", ((MethodCallValue) assignStatement.getValue()).getName());
        Assertions.assertEquals("", ((MethodCallValue) assignStatement.getValue()).getArgs().get(0).toString());

    }

    @Test
    public void test6() {

        String code = "create_message(\"\")";

        List<Token> tokens = Tokenizer.tokenize(new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8)));
        Parser parser = new Parser(new ScriptEngine(), tokens);
        final List<Statement> statements = parser.parseCommands(new HashMap<>());

        assert statements.size() == 1;
        Assertions.assertTrue( statements.get(0) instanceof MethodCallValue);

        MethodCallValue methodCallValue = (MethodCallValue) statements.get(0);
        Assertions.assertEquals("create_message", methodCallValue.getName());
        Assertions.assertEquals("", methodCallValue.getArgs().get(0).toString());

    }

    @Test
    public void test7() {

        String code = "i = 1\ncreate_message(\"\")\ni=2";

        List<Token> tokens = Tokenizer.tokenize(new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8)));
        Parser parser = new Parser(new ScriptEngine(), tokens);
        final List<Statement> statements = parser.parseCommands(new HashMap<>());

        assert statements.size() == 3;
        Assertions.assertTrue( statements.get(0) instanceof AssignStatement);
        Assertions.assertTrue( statements.get(1) instanceof MethodCallValue);
        Assertions.assertTrue( statements.get(2) instanceof AssignStatement);

        MethodCallValue methodCallValue = (MethodCallValue) statements.get(1);
        Assertions.assertEquals("create_message", methodCallValue.getName());
        Assertions.assertEquals("", methodCallValue.getArgs().get(0).toString());

        AssignStatement assignStatement = (AssignStatement) statements.get(2);
        assert assignStatement.getVarExpression().getName().equals("i");
        assert assignStatement.getValue().toString().equals("2");

    }


}
