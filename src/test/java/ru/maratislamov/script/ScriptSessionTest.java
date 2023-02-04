package ru.maratislamov.script;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import ru.maratislamov.script.values.MapValue;


public class ScriptSessionTest {

    @Test
    public void serializableTest1() throws JsonProcessingException {
        serializableTest(new ScriptSession("sessid"));

        serializableTest(new ScriptSession("sessid", MapValue.error("example"), 11));

    }

    public void serializableTest(ScriptSession session) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        String str = objectMapper.writeValueAsString(session);
        System.out.println(str);

        ScriptSession newSession = objectMapper.readValue(str, ScriptSession.class);
        String str2 = objectMapper.writeValueAsString(newSession);

        assert str.equals(str2);
    }


}