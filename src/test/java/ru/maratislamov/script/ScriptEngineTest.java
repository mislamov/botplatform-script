package ru.maratislamov.script;


import org.junit.jupiter.api.Test;
import ru.maratislamov.script.statements.Statement;
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

}