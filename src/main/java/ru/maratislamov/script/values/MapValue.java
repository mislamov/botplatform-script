package ru.maratislamov.script.values;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.utils.ValueUtils;

import java.util.*;
import java.util.function.Function;

/**
 * Хеш-таблица для оперирования со сложными структурными объектами (включая json)
 */
public class MapValue implements Value, MapOrListValueInterface {

    private static final Logger logger = LoggerFactory.getLogger(MapValue.class);

    public static final String PROPERTY_SIZE = "size";

    private Map<String, Value> body = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public Map<String, Value> getBody() {
        return body;
    }

    public void setBody(Map<String, Value> body) {
        this.body = body;
    }

    public MapValue() {
    }

    public MapValue(Map map) {
        map.forEach((k, v) -> {
            body.put(String.valueOf(k), Value.from(v));
        });
    }

    public MapValue(MapValue mv) {
        mv.body.forEach(body::put);
        body.putAll(mv.body);
    }

    public MapValue(String k, Value v) {
        body.put(k, v);
    }

    public MapValue(String k, String v) {
        body.put(k, new StringValue(v));
    }

    public MapValue(String k1, Value v1, String k2, Value v2) {
        body.put(k1, v1);
        body.put(k2, v2);
    }

    public MapValue(String k1, String v1, String k2, String v2) {
        body.put(k1, new StringValue(v1));
        body.put(k2, new StringValue(v2));
    }

    @JsonIgnore
    @Override
    public Double toNumber() {
        return null;
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
        //return "{" + StringUtils.join(body.entrySet(), ",") + "}";
        return ValueUtils.mapToString(this);
    }

    @JsonIgnore
    @Override
    public String getName() {
        return toString();
    }

    @JsonIgnore
    public static MapValue error(String text) {
        return new MapValue(Collections.singletonMap("error", new StringValue(text)));
    }

    @JsonIgnore
    @Override
    public Value evaluate(ScriptSession session) {
        return this;
    }

    @JsonIgnore
    public Value get(String name) {

        if (name.contains(".")) {
            throw new RuntimeException("Don't use DOTs in keys for MapValues: use VarMapUtils.getValueSetterByPath for deep");
        }

        if (!body.containsKey(name)) {
            if (name.equals(PROPERTY_SIZE)) return new NumberValue(body.size());
        }
        return body.get(name);
    }

    @JsonIgnore
    public Value getOrDefault(String name, Value def) {
        final Value value = get(name);
        return value == null ? def : value;
    }


    /**
     * @param key
     * @param value
     * @return @value
     * @deprecated присваивать в логике только через {@link ru.maratislamov.script.utils.VarMapUtils#getValueSetterByPath}
     */
    @Deprecated
    @JsonIgnore
    public Value put(String key, Value value) {
        assert !key.contains(".");
        if (key.contains(".")) throw new RuntimeException("Unexpected key");

        body.put(key, value);
        return value;
    }

    @JsonIgnore
    public Set<Map.Entry<String, Value>> getAll() {
        return body.entrySet();
    }

    @JsonIgnore
    public void remove(String key) {
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
