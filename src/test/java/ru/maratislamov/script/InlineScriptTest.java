package ru.maratislamov.script;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.maratislamov.script.debug.DebugExecutor;
import ru.maratislamov.script.statements.Statement;
import ru.maratislamov.script.values.Value;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InlineScriptTest {

    @Test
    public void test(){
        ScriptFunctionsService.register(new DebugExecutor());

        ScriptEngine scriptEngine = new ScriptEngine();
        List<Statement> statements = scriptEngine.scriptToStatements(new ByteArrayInputStream("i=\"hello, world\"\nprint i".getBytes(StandardCharsets.UTF_8)));
        ScriptSession session = scriptEngine.interpret(new ScriptSession().activate(), statements);
        System.out.println(session);
        Map<String, Value> varMap = session.getSessionScope().getBody();
        assert varMap.size() == 1;
        assert varMap.get("i").equals("hello, world");

    }

    @Test
    public void test2(){
        ScriptFunctionsService.register(new DebugExecutor());

        ScriptEngine scriptEngine = new ScriptEngine();
        List<Statement> statements = scriptEngine.scriptToStatements(new ByteArrayInputStream("i=\"hello, world\"".getBytes(StandardCharsets.UTF_8)));
        ScriptSession session = scriptEngine.interpret(new ScriptSession().activate(), statements);
        System.out.println(session);
        Map<String, Value> varMap = session.getSessionScope().getBody();

        Assertions.assertEquals(1, varMap.size());
        Assertions.assertEquals("hello, world", varMap.get("i").toString());
    }

    @Test
    public void test3(){
        ScriptFunctionsService.register(new DebugExecutor());

        ScriptEngine scriptEngine = new ScriptEngine();
        List<Statement> statements = scriptEngine.scriptToStatements(new ByteArrayInputStream("print \"2\"\ni=\"hello, world\"".getBytes(StandardCharsets.UTF_8)));
        ScriptSession session = scriptEngine.interpret(new ScriptSession().activate(), statements);
        System.out.println(session);
        Map<String, Value> varMap = session.getSessionScope().getBody();

        Assertions.assertEquals(1, varMap.size());
        Assertions.assertEquals("hello, world", varMap.get("i").toString());
    }

}
