package ru.maratislamov.script.values;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.maratislamov.script.ScriptFunctionsImplemntator;
import ru.maratislamov.script.ScriptSession;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class MapValue implements Value, MapValueInterface {

    private Map<String, Value> body;

    public Map<String, Value> getBody() {
        return body;
    }

    public void setBody(Map<String, Value> body) {
        this.body = body;
    }

    public MapValue() {
    }

    public MapValue(Map<String, Value> body) {
        this.body = body;
    }

    public MapValue(MapValue mv){
        body = new ConcurrentHashMap<>();
        mv.body.forEach(body::put);
        body.putAll(mv.body);
    }

    public MapValue(String k, Value v) {
        body = new HashMap<>();
        body.put(k, v);
    }

    public MapValue(String k, String v) {
        body = new HashMap<>();
        body.put(k, new StringValue(v));
    }

    public MapValue(String k1, Value v1, String k2, Value v2) {
        body = new HashMap<>();
        body.put(k1, v1);
        body.put(k2, v2);
    }

    public MapValue(String k1, String v1, String k2, String v2) {
        body = new HashMap<>();
        body.put(k1, new StringValue(v1));
        body.put(k2, new StringValue(v2));
    }

    @JsonIgnore
    @Override
    public BigDecimal toNumber() {
        throw new ClassCastException("MapValue to Number");
    }

    @JsonIgnore
    @Override
    public Value copy() {
        Map<String, Value> map = new HashMap<>(body);
        return new MapValue(map);
    }

    @JsonIgnore
    @Override
    public String toString() {
        return "{" + StringUtils.join(body.entrySet(), ",") + "}";
    }

    @JsonIgnore
    public static MapValue error(String text){
        return new MapValue(Collections.singletonMap("error", new StringValue(text)));
    }

    @JsonIgnore
    @Override
    public Value evaluate(ScriptSession session, ScriptFunctionsImplemntator funcImpl) {
        return this;
    }

    @JsonIgnore
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
    @JsonIgnore
    public Value put(String key, Value value) {
        body.put(key, value);
        return value;
    }

    @JsonIgnore
    public Set<Map.Entry<String, Value>> getAll(){
        return body.entrySet();
    }

    @JsonIgnore
    public void remove(String key){
        body.remove(key);
    }

    @JsonIgnore
    public boolean containsKey(String name) {
        return body.containsKey(name);
    }

    @JsonIgnore
    public Value computeIfAbsent(String name, Function<String, Value> mappingFunction) {
        return body.computeIfAbsent(name, mappingFunction);
    }
}
