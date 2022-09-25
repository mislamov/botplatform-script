package ru.maratislamov.script;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class ScriptSessionTest {

    @Test
    public void serializableTest() throws JsonProcessingException {

        ScriptSession session = new ScriptSession("sessid");

        ObjectMapper objectMapper = new ObjectMapper();
        String str = objectMapper.writeValueAsString(session);
        System.out.println(str);

        ScriptSession newSession = objectMapper.readValue(str, ScriptSession.class);
        String str2 = objectMapper.writeValueAsString(newSession);

        assert str.equals(str2);

    }

}