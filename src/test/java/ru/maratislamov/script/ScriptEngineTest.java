package ru.maratislamov.script;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.maratislamov.script.expressions.Expression;
import ru.maratislamov.script.expressions.ListExpressions;
import ru.maratislamov.script.parser.Parser;
import ru.maratislamov.script.statements.AssignStatement;
import ru.maratislamov.script.statements.Statement;
import ru.maratislamov.script.values.ListValue;
import ru.maratislamov.script.values.MethodCallValue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

class ScriptEngineTest {

    @Test
    public void test(){
        ScriptEngine scriptEngine = new ScriptEngine();
        List<Statement> statements = scriptEngine.scriptToStatements(new ByteArrayInputStream("\nfile \"file1\" \"caption\" \"wronstr\"\nfile \"file2\"\n\n".getBytes(StandardCharsets.UTF_8)));
        System.out.println(statements);
        assert statements.size() == 2; // print(file()), print(file())

        final Statement statement0 = statements.get(0);
        final Statement statement1 = statements.get(1);

        assert statement0 instanceof MethodCallValue;
        assert statement1 instanceof MethodCallValue;

        assert ((MethodCallValue) statement0).getName().equals("print");
        assert ((MethodCallValue) statement1).getName().equals("print");

        assert ((MethodCallValue) statement0).getArgs().size() == 1;
        assert ((MethodCallValue) statement0).getArgs().get(0) instanceof MethodCallValue;
        assert ((MethodCallValue) statement0).getArgs().get(0).getName().equals("file");
        assert ((MethodCallValue) ((MethodCallValue) statement0).getArgs().get(0)).getArgs().size() == 3;

        assert ((MethodCallValue) statement1).getArgs().size() == 1;
        assert ((MethodCallValue) statement1).getArgs().get(0) instanceof MethodCallValue;
        assert ((MethodCallValue) statement1).getArgs().get(0).getName().equals("file");
        assert ((MethodCallValue) ((MethodCallValue) statement1).getArgs().get(0)).getArgs().size() == 1;

    }

    @Test
    public void testKeyboard(){
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
    public void testSignNumbers(){
        ScriptEngine scriptEngine = new ScriptEngine();
        List<Statement> statements = scriptEngine.scriptToStatements(new ByteArrayInputStream("x = -111.12; y = +123.456".getBytes(StandardCharsets.UTF_8)));
        System.out.println(statements);
        assert statements.size() == 2;
        assert statements.get(0) instanceof AssignStatement;
        assert statements.get(1) instanceof AssignStatement;

        assert ((AssignStatement) statements.get(0)).getVarExpression().getName().equals("x");
        assert ((AssignStatement) statements.get(0)).getValue().toString().equals("-111,12");
        assert ((AssignStatement) statements.get(1)).getVarExpression().getName().equals("y");
        assert ((AssignStatement) statements.get(1)).getValue().toString().equals("123,456");
    }

    // keyb []  -->  print(keyb([..]))
    @Test
    public void testKeyboard2(){
        ScriptEngine scriptEngine = new ScriptEngine();
        //Parser.COMMANDS.add("KEYBOARD");
        List<Statement> statements = scriptEngine.scriptToStatements(new ByteArrayInputStream("\nkeyboard [\"a\", \"b\"]\n".getBytes(StandardCharsets.UTF_8)));
        System.out.println(statements);
        assert statements.size() == 1;
        assert statements.get(0) instanceof MethodCallValue;
        List<Expression> args = ((MethodCallValue) statements.get(0)).getArgs();

        assert ((MethodCallValue) statements.get(0)).getName().equals("print");
        assert args.size() == 1;
        assert args.get(0) instanceof MethodCallValue;

        args = ((MethodCallValue) args.get(0)).getArgs();
        assert ((ListExpressions) args.get(0)).get(0).toString().equals("a");
        assert ((ListExpressions) args.get(0)).get(1).toString().equals("b");
    }



    @Test
    public void testExec(){
        String code= "base_menu = [1,2]\n tail_menu=[3,4]\n current_menu = base_menu + [tail_menu]";

        ScriptEngine scriptEngine = new ScriptEngine();
        scriptEngine.load(new ByteArrayInputStream((code).getBytes(StandardCharsets.UTF_8)));
        final ScriptSession sess = scriptEngine.interpret(new ScriptSession() {{
            setCurrentStatement(0);
        }});

        System.out.println(sess.getSessionScope());
    }


    public void assertEval(String exp, String result){
        ScriptEngine scriptEngine = new ScriptEngine();
        scriptEngine.load(new ByteArrayInputStream(("E=" + exp).getBytes(StandardCharsets.UTF_8)));
        final ScriptSession sess = scriptEngine.interpret(new ScriptSession() {{
            setCurrentStatement(0);
        }});

        Assertions.assertEquals(result, String.valueOf(sess.getSessionScope().get("E")), "Ошибка арифметического вычисления: " + exp);
    }

    @Test
    public void testEval(){
        assertEval("6 - (5 + 5)", "-4");
        assertEval("(6 - 5) + 5", "6");
        assertEval("6 - 5 + 5", "6");
        assertEval("5 - 6 + 6", "5");
        assertEval("-55", "-55");
        assertEval("-55.1", "-55,1");
    }


}