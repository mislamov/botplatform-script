package ru.maratislamov.script;

import org.junit.jupiter.api.Test;
import ru.maratislamov.script.context.ScriptRunnerContext;
import ru.maratislamov.script.debug.DebugExecutor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class MainTest {

    @Test
    public void runHello() throws IOException {
        runScript("sample\\hello.jas", null);
    }

    @Test
    public void runHellos() throws IOException {
        runScript("sample\\hellos.jas", null);
    }

    @Test
    public void runMandel() throws IOException {
        runScript("sample\\mandel.jas", null);
    }

    @Test
    public void runDemoMaps() throws IOException {
        runScript("sample\\demomaps.jas", null);
    }

    @Test
    public void runMaps() throws IOException {
        runScript("sample\\maps.bas", null);
    }


    @Test
    public void runPlusEq() throws IOException {
        runScript("x = 1; x += 10; print x".getBytes(StandardCharsets.UTF_8), null);
    }

    @Test
    public void runDummy() throws IOException {
        runScript("print 4>=4".getBytes(StandardCharsets.UTF_8), null);
    }

@Test
    public void runDummy2() throws IOException {
        runScript("if (j >= gap && compare(arr[j - gap], temp) > 0) then goto end_if_3".getBytes(StandardCharsets.UTF_8), null);
    }

    //@Test
    public void runDemo() throws IOException {
        runScript("demo.bas", null);
    }

//    /**
//     * запуск скрипта без внешнего контекста
//     * @param path - путь до скрипта
//     * @throws IOException
//     */
//    public void runScript(String path) throws IOException {
//        runScript(path, null);
//    }

    /**
     * запуск скрипта во внешнем контексте
     * @param path - путь до скрипта
     * @param runnerContext - контекст запуска скрипта
     * @throws IOException
     */
    public void runScript(String path, ScriptRunnerContext runnerContext) throws IOException {

        ScriptFunctionsService.register(new DebugExecutor());

        try (InputStream inputStream = MainTest.class.getClassLoader().getResourceAsStream(path)) {

            ScriptEngine botScript = new ScriptEngine();

            botScript.load(inputStream);

            botScript.interpret(runnerContext);
        }
    }

//    public void runScript(byte[] code) throws IOException {
//        runScript(code, null);
//    }

    public void runScript(byte[] code, ScriptRunnerContext runnerContext) throws IOException {

        ScriptFunctionsService.register(new DebugExecutor());

        try (InputStream inputStream = new ByteArrayInputStream(code)) {

            ScriptEngine botScript = new ScriptEngine();

            botScript.load(inputStream);

            botScript.interpret(runnerContext);
        }

    }
}
