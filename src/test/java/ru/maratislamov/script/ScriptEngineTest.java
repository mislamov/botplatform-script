package ru.maratislamov.script;


import org.junit.jupiter.api.Test;
import ru.maratislamov.script.expressions.Expression;
import ru.maratislamov.script.expressions.ListExpressions;
import ru.maratislamov.script.parser.Parser;
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
        assert statements.size() == 2;
        assert statements.get(0) instanceof MethodCallValue;
        assert ((MethodCallValue) statements.get(0)).getArgs().size() == 3;
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
    public void testKeyboard2(){
        ScriptEngine scriptEngine = new ScriptEngine();
        Parser.COMMANDS.add("KEYBOARD");
        List<Statement> statements = scriptEngine.scriptToStatements(new ByteArrayInputStream("\nkeyboard [\"a\", \"b\"]\n".getBytes(StandardCharsets.UTF_8)));
        System.out.println(statements);
        assert statements.size() == 1;
        assert statements.get(0) instanceof MethodCallValue;
        final List<Expression> args = ((MethodCallValue) statements.get(0)).getArgs();
        assert args.size() == 1;
        assert args.get(0) instanceof ListExpressions;
        assert ((ListExpressions) args.get(0)).get(0).toString().equals("a");
        assert ((ListExpressions) args.get(0)).get(1).toString().equals("b");
    }

}