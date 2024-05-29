package ru.maratislamov.script.values;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.utils.ValueUtils;
import ru.maratislamov.script.utils.VarLocalMemoryManager;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Хеш-таблица для оперирования со сложными структурными объектами (включая json)
 */
public class MapValue implements Value, MapOrListValueInterface {

    private static final Logger logger = LoggerFactory.getLogger(MapValue.class);

    public static final int MAP_EQUAL_CALC_DEEP = 10; // максимальная глубина сравнения двух ValueMap

    private Map<String, Value> body = Collections.synchronizedMap(new LinkedHashMap<>());  //new TreeMap<>(/*String.CASE_INSENSITIVE_ORDER*/);

    public static Map<String, Function<MapValue, Value>> methods;

    static {
        methods = new HashMap<>();
        methods.put("keys", v -> new ListValue(v.getBody().keySet().stream().map(Value::from).collect(Collectors.toList())));
        methods.put("size", v -> Value.from(v.getBody().size()));
        methods.put("length", v -> Value.from(v.getBody().size()));
        methods.put("regexp", null);
    }

    public Map<String, Value> getBody() {
        return body;
    }

    public void setBody(Map<String, Value> body) {
        this.body = body;
    }

    @Override
    public Object nativeObject() {
        return body.entrySet().stream()
                .collect(LinkedHashMap::new, (m, v) -> m.put(v.getKey(), v.getValue().nativeObject()), LinkedHashMap::putAll);
    }

    public MapValue() {
    }

    public MapValue(Map map) {
        map.forEach((k, v) -> {
            body.put(String.valueOf(k), Value.from(v));
        });
    }

    public MapValue(MapValue mv) {
        //mv.body.forEach(body::put);
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
        return (double) body.size();
    }

    @JsonIgnore
    @Override
    public Value copy() {
        Map<String, Value> map = new LinkedHashMap<>(body);
        return new MapValue(map);
    }

    @JsonIgnore
    @Override
    public String toString() {
        //return "{" + StringUtils.join(body.entrySet(), ",") + "}";
        return ValueUtils.mapToDebugString(this);
    }


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

        if (name == null) throw new RuntimeException("parameter name is NULL");

        /* с тех пор как ключём может быть любая строка (например дата), эта проверка некорректна
        if (name.contains(".")) {
            throw new RuntimeException("Don't use DOTs in keys for MapValues. Use VarMapUtils.getValueSetterByPath for deep. Found: " + name);
        }*/

        if (methods.containsKey(name))
            return methods.get(name).apply(this);

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
     * @deprecated присваивать в логике только через {@link VarLocalMemoryManager#getValueSetterByPath}
     */
    @Deprecated
    @JsonIgnore
    public Value put(String key, Value value) {
        if (key == null) throw new RuntimeException("NULL key");

        /*
        с тех пор как ключём может быть любая строка (например дата), эта проверка некорректна
        assert !key.contains(".");
        if (key.contains(".")) throw new RuntimeException("Unexpected key: " + key);
        */

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

    @Override
    public boolean isEmpty() {
        return body.isEmpty();
    }

    @JsonIgnore
    public boolean containsKey(String name) {
        return methods.containsKey(name) || body.containsKey(name);
    }

    @Override
    public boolean containsMethod(String name) {
        return methods.containsKey(name);
    }

    @Override
    public Function<MapValue, Value> getMethod(String name) {
        return methods.getOrDefault(name, null);
    }

    @JsonIgnore
    public Value computeIfAbsent(String name, Function<String, Value> mappingFunction) {
        return body.computeIfAbsent(name, mappingFunction);
    }

    public static final <K, V> boolean equals(Map<K, V> m1, Map<K, V> m2, int deep) {
        if (m1 == m2) return true;
        if (m1 == null || m1.getClass() != m2.getClass()) return false;
        if (m1.hashCode() != m2.hashCode()) return false;
        if (m1.size() != m2.size()) return false;

        for (Iterator<Map.Entry<K, V>> iterator = m1.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<K, V> en = iterator.next();
            K key = en.getKey();
            V val1 = en.getValue();
            V val2 = m2.get(key);
            if (val1 == val2) continue;
            if (val1 == null || val1.getClass() != val2.getClass()) return false;
            if (val1 instanceof MapValue && val2 instanceof MapValue) {
                if (deep == 0)
                    continue;
                if (!equals((Map) ((MapValue) val1).body, (Map) ((MapValue) val2).body, deep - 1)) {
                    return false;
                }
                continue;
            }
            if (!val1.equals(val2)) return false;
        }
        return true;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapValue mapValue = (MapValue) o;
        return equals(body, mapValue.body, MAP_EQUAL_CALC_DEEP);
    }

    @Override
    public int hashCode() {
        return body == null ? 0 : body.size(); // иначе может быть зацикленность
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof MapValue) {
            return Integer.compare(getBody().size(), ((MapValue) o).getBody().size());
        }
        throw new RuntimeException("Unexpected comparation ListValue with " + o.getClass().getSimpleName());
    }
}
