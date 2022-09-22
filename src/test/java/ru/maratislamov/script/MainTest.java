package ru.maratislamov.script;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
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

    public void runScript(String fname) throws IOException {
        System.out.println("ok");

        ScriptFunctionsService.register(new ScriptFunctionDemoExecutor());

        //try (InputStream inputStream = new ClassPathResource("demo.bas").getInputStream()) {
        try (InputStream inputStream = MainTest.class.getClassLoader().getResourceAsStream(fname)) {

            ScriptEngine botScript = new ScriptEngine();

            botScript.load(inputStream);
            ScriptSession session = new ScriptSession(UUID.randomUUID().toString());
            session.setActive(true);
            botScript.interpret(session);
        }

    }
}
