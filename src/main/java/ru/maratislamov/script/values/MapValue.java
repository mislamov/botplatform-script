package ru.maratislamov.script.values;

import ru.maratislamov.script.ScriptSession;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.function.Function;

public class MapValue implements Value, MapValueInterface {

    final Map<String, Value> body;

    public MapValue(Map<String, Value> body) {
        this.body = body;
    }

    @Override
    public double toNumber() {
        throw new ClassCastException("MapValue to Number");
    }

    @Override
    public String toString() {
        return "{" + StringUtils.join(body.keySet(), ",") + "}";
    }

    @Override
    public Value evaluate(ScriptSession session) {
        return this;
    }

    public Value get(String name, ScriptSession session) {
        if (!body.containsKey(name)) {
            if (name.equals("size")) return new NumberValue(body.size());
        }
        return body.get(name);
    }

    public void put(String key, Value value) {
        body.put(key, value);
    }

    public boolean containsKey(String name) {
        return body.containsKey(name);
    }

    public Value computeIfAbsent(String name, Function<String, Value> mappingFunction) {
        return body.computeIfAbsent(name, mappingFunction);
    }
}
