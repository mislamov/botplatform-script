package ru.maratislamov;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import ru.maratislamov.script.BotScript;
import ru.maratislamov.script.ScriptConsoleImplementator;
import ru.maratislamov.script.ScriptSession;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {

        Resource resource = new ClassPathResource("demo.bas");

        try (InputStream inputStream = resource.getInputStream()) {

            BotScript botScript = new BotScript(new ScriptConsoleImplementator());

            botScript.load(inputStream);
            botScript.interpret(new ScriptSession(UUID.randomUUID().toString()));
        }
    }


}
