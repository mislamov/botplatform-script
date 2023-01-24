package ru.maratislamov.script;

import org.junit.jupiter.api.Test;
import ru.maratislamov.script.debug.DebugExecutor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class MainTest {

    @Test
    public void runHello() throws IOException {
        runScript("sample\\hello.jas");
    }

    @Test
    public void runHellos() throws IOException {
        runScript("sample\\hellos.jas");
    }

    @Test
    public void runMandel() throws IOException {
        runScript("sample\\mandel.jas");
    }

    @Test
    public void runDemoMaps() throws IOException {
        runScript("sample\\demomaps.jas");
    }


    @Test
    public void runPlusEq() throws IOException {
        runScript("x = 1; x += 10; print x".getBytes(StandardCharsets.UTF_8));
    }

    ////@Test
    public void runDummy() throws IOException {
        runScript("x = NULL\ninput x.y[1+1].x".getBytes(StandardCharsets.UTF_8));
    }
    //@Test
    public void runDemo() throws IOException {
        runScript("demo.bas");
    }

    public void runScript(String fname) throws IOException {

        ScriptFunctionsService.register(new DebugExecutor());

        //try (InputStream inputStream = new ClassPathResource("demo.bas").getInputStream()) {
        try (InputStream inputStream = MainTest.class.getClassLoader().getResourceAsStream(fname)) {

            ScriptEngine botScript = new ScriptEngine();

            botScript.load(inputStream);

            botScript.interpret();
        }
    }

    public void runScript(byte[] code) throws IOException {

        ScriptFunctionsService.register(new DebugExecutor());

        //try (InputStream inputStream = new ClassPathResource("demo.bas").getInputStream()) {
        try (InputStream inputStream = new ByteArrayInputStream(code)) {

            ScriptEngine botScript = new ScriptEngine();

            botScript.load(inputStream);

            botScript.interpret();
        }

    }
}
