package ru.maratislamov.script.values;

import ru.maratislamov.script.ScriptFunctionsImplemntator;
import ru.maratislamov.script.ScriptSession;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Function;

public class MapValue implements Value, MapValueInterface {

    final Map<String, Value> body;

    public MapValue(Map<String, Value> body) {
        this.body = body;
    }

    @Override
    public BigDecimal toNumber() {
        throw new ClassCastException("MapValue to Number");
    }

    @Override
    public String toString() {
        return "{" + StringUtils.join(body.keySet(), ",") + "}";
    }

    @Override
    public Value evaluate(ScriptSession session, ScriptFunctionsImplemntator executionContext) {
        return this;
    }

    public Value get(String name, ScriptSession session, ScriptFunctionsImplemntator context) {
        if (!body.containsKey(name)) {
            if (name.equals("size")) return new NumberValue(body.size());
        }
        return body.get(name);
    }

    /**
     *
     * @param key
     * @param value
     * @return @value
     */
    public Value put(String key, Value value) {
        body.put(key, value);
        return value;
    }

    public void remove(String key){
        body.remove(key);
    }

    public boolean containsKey(String name) {
        return body.containsKey(name);
    }

    public Value computeIfAbsent(String name, Function<String, Value> mappingFunction) {
        return body.computeIfAbsent(name, mappingFunction);
    }
}
