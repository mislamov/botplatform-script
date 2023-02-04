package ru.maratislamov.script;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.maratislamov.script.context.ScriptRunnerContext;
import ru.maratislamov.script.expressions.Expression;
import ru.maratislamov.script.expressions.ListExpressions;
import ru.maratislamov.script.statements.AssignStatement;
import ru.maratislamov.script.statements.Statement;
import ru.maratislamov.script.values.ListValue;
import ru.maratislamov.script.values.MethodCallValue;
import ru.maratislamov.script.values.NumberValue;
import ru.maratislamov.script.values.Value;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

        assert ((MethodCallValue) statement0).getName().equals("file");
        assert ((MethodCallValue) statement1).getName().equals("file");

        assert ((MethodCallValue) statement0).getArgs().size() == 3;
        assert ((MethodCallValue) statement1).getArgs().size() == 1;
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

        assert ((MethodCallValue) statements.get(0)).getName().equals("keyboard");
        assert args.size() == 1;
        assert ((ListExpressions) args.get(0)).get(0).toString().equals("a");
        assert ((ListExpressions) args.get(0)).get(1).toString().equals("b");
    }



    @Test
    public void testConcatArrays(){
        String code= "base_menu = [1,2]\n tail_menu=[3,4]\n current_menu = base_menu + [tail_menu]";

        ScriptEngine scriptEngine = new ScriptEngine();
        scriptEngine.load(new ByteArrayInputStream((code).getBytes(StandardCharsets.UTF_8)));
        final ScriptSession sess = scriptEngine.interpret(new ScriptSession(ScriptRunnerContext.empty) {{
            setCurrentStatement(0);
        }});

        System.out.println(sess.getSessionScope());
    }


    @Test
    public void testLn(){
        assertEval("\"head\" + \"tail\"", "headtail");
        assertEval("\"head\"\n + \"tail\"", "headtail");
        assertEval("\"head\" + \n\"tail\"", "headtail");
        assertEval("\n[\n\t1,\n\t2\n]", ListValue.of(new NumberValue(1), new NumberValue(2)));
    }


    public void assertEval(String exp, String result){
        ScriptEngine scriptEngine = new ScriptEngine();
        scriptEngine.load(new ByteArrayInputStream(("E=" + exp).getBytes(StandardCharsets.UTF_8)));
        final ScriptSession sess = scriptEngine.interpret(new ScriptSession(ScriptRunnerContext.empty) {{
            setCurrentStatement(0);
        }});

        Assertions.assertEquals(result, String.valueOf(sess.getSessionScope().get("E")), "Ошибка арифметического вычисления: " + exp);
    }

    public void assertEval(String exp, Value result){
        ScriptEngine scriptEngine = new ScriptEngine();
        scriptEngine.load(new ByteArrayInputStream(("E=" + exp).getBytes(StandardCharsets.UTF_8)));
        final ScriptSession sess = scriptEngine.interpret(new ScriptSession(ScriptRunnerContext.empty) {{
            setCurrentStatement(0);
        }});

        Assertions.assertEquals(result, sess.getSessionScope().get("E"), "Ошибка арифметического вычисления: " + exp);
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

    @Test
    public void testCollection(){
        ScriptEngine scriptEngine = new ScriptEngine();
        scriptEngine.load(new ByteArrayInputStream(("E=[];E.1=1; E.2=2; E.3=3").getBytes(StandardCharsets.UTF_8)));
        final ScriptSession sess = scriptEngine.interpret(new ScriptSession(ScriptRunnerContext.empty) {{
            setCurrentStatement(0);
        }});

        ListValue listValue = (ListValue) sess.getSessionScope().get("E");

        System.out.println(listValue);

        AtomicInteger i = new AtomicInteger();
        listValue.forEach(l -> {
            System.out.println(l);
            i.incrementAndGet();
        });
        assert i.get() == 4;  // [null, 1, 2, 3]

        AtomicInteger ii = new AtomicInteger();
        listValue.getIterator().forEachRemaining(l -> {
            System.out.println(l);
            ii.incrementAndGet();
        });
        assert ii.get() == 4;  // [null, 1, 2, 3]
    }



}