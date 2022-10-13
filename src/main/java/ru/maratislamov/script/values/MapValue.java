package ru.maratislamov.script.values;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.maratislamov.script.ScriptSession;
import org.apache.commons.lang3.StringUtils;
import ru.maratislamov.script.utils.ValueUtils;


import java.rmi.UnexpectedException;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;

/**
 * Хеш-таблица для оперирования со сложными структурными объектами (включая json)
 */
public class MapValue implements Value, MapValueInterface {

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
        //return "{" + StringUtils.join(body.entrySet(), ",") + "}";
        return ValueUtils.mapToString(this);
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
    public Value get(String name, ScriptSession session/*, ScriptFunctionsImplemntator context*/) {

        if (name.contains(".")) {
            try {
                return find(name, false);

            } catch (ParseException e) {
                logger.error("map parsing by path failed", e);
                //return null;
                throw new RuntimeException("map parsing by path failed", e);
            }
        }

        if (!body.containsKey(name)) {
            if (name.equals(PROPERTY_SIZE)) return new NumberValue(body.size());
        }
        return body.get(name);
    }

    @JsonIgnore
    public Value get(String name) {
        return get(name, null/*, null*/);
    }

    /**
     * Поиск выражение по пути вида Слово.Слово.Слово
     *
     * @param path
     * @return
     */
    public Value find(String path) throws ParseException {
        return find(path, false);
    }

    public Value find(String path, boolean createSubMaps) throws ParseException {
        Value value = this;
        MapValue prevValue = this;
        String[] split = path.split("\\.");

        for (int i = 0; i < split.length; i++) {
            String var = split[i];

            // following
            if (value == null && createSubMaps){
                value = new MapValue();
                prevValue.put(split[i-1], value);
            }

            if (value instanceof MapValue) {
                prevValue = (MapValue) value;
                value = prevValue.get(var);

                // todo: list element by index  ...xxx.yy[2].sd...

            } else if (i == split.length - 1) {
                // last element is not map
                return value;

            } else if (value != null) {
                // not last element and not map
                throw new ParseException("wrong path '" + path + "' for not Map value on step '" + var + "' (type is " + value.getClass().getSimpleName() + ")", i);

            } else {
                // value == null;
                if (!createSubMaps) return null;
                value = new MapValue();
                prevValue.put(var, value);
            }
        }
        return value;
    }


    /**
     * @param key
     * @param value
     * @return @value
     */
    @JsonIgnore
    public Value put(String key, Value value) {
        if (!key.contains(".")){
            body.put(key, value);
            return value;
        }
        // у нас есть путь
        int lastDotIdx = key.lastIndexOf(".");
        String lastPathName = key.substring(lastDotIdx + 1);
        String preLastPath = key.substring(0, lastDotIdx);

        Value old = null;
        try {
            find(key, true); // инициализируем весь preLastPath
            old = find(preLastPath);
            MapValue prev = (MapValue) old;
            prev.put(lastPathName, value);
            return value;

        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("path to wrong map value: " + key + " (" + ClassUtils.getSimpleName(old, null) + ")");
        }


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
