package ru.maratislamov.script.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.maratislamov.script.ScriptEngine;
import ru.maratislamov.script.expressions.Expression;
import ru.maratislamov.script.expressions.VariableExpression;
import ru.maratislamov.script.expressions.WrapStatement;
import ru.maratislamov.script.statements.Statement;
import ru.maratislamov.script.values.NumberValue;
import ru.maratislamov.script.values.StringValue;

import java.util.ArrayList;
import java.util.List;

public class TextFrameTest {


    @Test
    public void testVarNameInFrame1(){
        ArrayList<Token> tokens = new ArrayList<>();
        ParserSession parserSession = new ParserSession(new ScriptEngine(), tokens);

        final List<Expression> expressions = parserSession.frameTextToArgList("hello, $user.name, bye");
        assert expressions.get(0).toString().equals("hello, ");
        assert expressions.get(1) instanceof VariableExpression;
        assert ((VariableExpression) expressions.get(1)).getName().equals("user");
        assert ((VariableExpression) expressions.get(1)).getNextInPath().getName().equals("name");
        assert ((VariableExpression) expressions.get(1)).getNextInPath().getNextInPath() == null;
        assert expressions.get(2).toString().equals(", bye");

        assert expressions.get(0) instanceof StringValue;
        assert expressions.get(1) instanceof VariableExpression;
        assert expressions.get(2) instanceof StringValue;

        assert expressions.size() == 3;
    }

    @Test
    public void testVarNameInFrame2(){
        ArrayList<Token> tokens = new ArrayList<>();
        ParserSession parserSession = new ParserSession(new ScriptEngine(), tokens);

        final List<Expression> expressions = parserSession.frameTextToArgList("hello, $user.name");
        assert expressions.get(0).toString().equals("hello, ");
        assert expressions.get(1) instanceof VariableExpression;
        assert ((VariableExpression) expressions.get(1)).getName().equals("user");
        assert ((VariableExpression) expressions.get(1)).getNextInPath().getName().equals("name");
        assert ((VariableExpression) expressions.get(1)).getNextInPath().getNextInPath() == null;

        assert expressions.get(0) instanceof StringValue;
        assert expressions.get(1) instanceof VariableExpression;

        assert expressions.size() == 2;
    }

    @Test
    public void testVarNameInFrame2_1(){
        ArrayList<Token> tokens = new ArrayList<>();
        ParserSession parserSession = new ParserSession(new ScriptEngine(), tokens);

        final List<Expression> expressions = parserSession.frameTextToArgList("$user.name, hello");
        assert expressions.get(0).toString().equals("${user.name}");
        assert expressions.get(1).toString().equals(", hello");

        assert expressions.get(0) instanceof VariableExpression;
        assert expressions.get(1) instanceof StringValue;

        assert expressions.size() == 2;
    }


    @Test
    public void testVarNameInFrame2_2(){
        ArrayList<Token> tokens = new ArrayList<>();
        ParserSession parserSession = new ParserSession(new ScriptEngine(), tokens);

        final List<Expression> expressions = parserSession.frameTextToArgList("mmm $user.name, hello");
        assert expressions.get(0).toString().equals("mmm ");
        assert expressions.get(1).toString().equals("${user.name}");
        assert expressions.get(2).toString().equals(", hello");

        assert expressions.get(0) instanceof StringValue;
        assert expressions.get(1) instanceof VariableExpression;
        assert expressions.get(2) instanceof StringValue;

        assert expressions.size() == 3;
    }

    @Test
    public void testVarNameInFrame3(){
        ArrayList<Token> tokens = new ArrayList<>();
        ParserSession parserSession = new ParserSession(new ScriptEngine(), tokens);

        final List<Expression> expressions = parserSession.frameTextToArgList("hello, $$user.name");
        assert expressions.get(0).toString().equals("hello, ");
        assert expressions.get(1).toString().equals("$");
        assert expressions.get(2).toString().equals("user.name");

        assert expressions.get(0) instanceof StringValue;
        assert expressions.get(1) instanceof StringValue;
        assert expressions.get(2) instanceof StringValue;

        assert expressions.size() == 3;

        System.out.println(expressions);
    }
    @Test
    public void testVarNameInFrame3_1(){
        ArrayList<Token> tokens = new ArrayList<>();
        ParserSession parserSession = new ParserSession(new ScriptEngine(), tokens);

        final List<Expression> expressions = parserSession.frameTextToArgList("$$hello, user.name");

        assert expressions.get(0).toString().equals("$");
        assert expressions.get(1).toString().equals("hello, user.name");

        assert expressions.get(0) instanceof StringValue;
        assert expressions.get(1) instanceof StringValue;

        assert expressions.size() == 2;

        System.out.println(expressions);
    }

    @Test
    public void testVarNameInFrame3_2(){
        ArrayList<Token> tokens = new ArrayList<>();
        ParserSession parserSession = new ParserSession(new ScriptEngine(), tokens);

        final List<Expression> expressions = parserSession.frameTextToArgList("hello, user.name$$");

        assert expressions.get(0).toString().equals("hello, user.name");
        assert expressions.get(1).toString().equals("$");

        assert expressions.get(0) instanceof StringValue;
        assert expressions.get(1) instanceof StringValue;

        assert expressions.size() == 2;

        System.out.println(expressions);
    }

    @Test
    public void testVarNameInFrame4(){
        ArrayList<Token> tokens = new ArrayList<>();
        ParserSession parserSession = new ParserSession(new ScriptEngine(), tokens);

        final List<Expression> expressions = parserSession.frameTextToArgList("hello, $ user.name");
        assert expressions.get(0).toString().equals("hello, ");
        assert expressions.get(1).toString().equals("$ user.name");

        assert expressions.get(0) instanceof StringValue;
        assert expressions.get(1) instanceof StringValue;

        assert expressions.size() == 2;
    }

    @Test
    public void testVarNameInFrame4_1(){
        ArrayList<Token> tokens = new ArrayList<>();
        ParserSession parserSession = new ParserSession(new ScriptEngine(), tokens);

        final List<Expression> expressions = parserSession.frameTextToArgList("$ hello, user.name");

        assert expressions.size() == 1;

        assert expressions.get(0).toString().equals("$ hello, user.name");

        assert expressions.get(0) instanceof StringValue;
    }

    @Test
    public void testVarNameInFrame4_2(){
        ArrayList<Token> tokens = new ArrayList<>();
        ParserSession parserSession = new ParserSession(new ScriptEngine(), tokens);

        final List<Expression> expressions = parserSession.frameTextToArgList("hello, user.name$");
        assert expressions.get(0).toString().equals("hello, user.name");
        assert expressions.get(1).toString().equals("$");

        assert expressions.get(0) instanceof StringValue;
        assert expressions.get(1) instanceof StringValue;
    }

    @Test
    public void testVarNameInFrame4_3(){
        ArrayList<Token> tokens = new ArrayList<>();
        final ScriptEngine scriptEngine = new ScriptEngine();
        ParserSession parserSession = new ParserSession(scriptEngine, tokens);

        final List<Statement> statements = scriptEngine.scriptToStatements("name.subname[2]");
        assert statements.size() == 1;
        assert statements.get(0) instanceof WrapStatement;
        final VariableExpression var = (VariableExpression) ((WrapStatement) statements.get(0)).getExpression();
        assert var.getName().equals("name");
        assert var.getNextInPath().getName().equals("subname");
        assert var.getNextInPath().getNextInPath().getName().equals("2");


        List<Expression> expressions = parserSession.frameTextToArgList("hello, $name.subname[2]!");
        assert expressions.get(0).toString().equals("hello, ");
        assert expressions.get(1) instanceof VariableExpression;
        assert expressions.get(1).getName().equals("name");
        assert ((VariableExpression) expressions.get(1)).getNextInPath().getName().equals("subname");
        assert ((VariableExpression) expressions.get(1)).getNextInPath().getNextInPath().getNameExpression().equals(new NumberValue(2));
        assert expressions.get(2).toString().equals("!");


        expressions = parserSession.frameTextToArgList("hello, $name.subname[1]+$name.subname[2]!");
        assert expressions.get(0).toString().equals("hello, ");
        assert expressions.get(1) instanceof VariableExpression;
        assert expressions.get(1).getName().equals("name");
        assert ((VariableExpression) expressions.get(1)).getNextInPath().getName().equals("subname");
        assert ((VariableExpression) expressions.get(1)).getNextInPath().getNextInPath().getNameExpression().equals(new NumberValue(1));

        assert expressions.get(2).toString().equals("+");

        assert expressions.get(3) instanceof VariableExpression;
        assert expressions.get(3).getName().equals("name");
        assert ((VariableExpression) expressions.get(3)).getNextInPath().getName().equals("subname");
        assert ((VariableExpression) expressions.get(3)).getNextInPath().getNextInPath().getNameExpression().equals(new NumberValue(2));

        assert expressions.get(4).toString().equals("!");

    }

    @Test
    public void test(){
        Assertions.assertThrows(Error.class, () -> {
            final List<Statement> statements = new ScriptEngine().scriptToStatements("print \"\"\"hello,\nmy\nfriend\"\n y=1");
            System.out.println(statements);
        });


    }


}
