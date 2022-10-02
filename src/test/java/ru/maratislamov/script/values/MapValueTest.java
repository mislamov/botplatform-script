package ru.maratislamov.script.values;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MapValueTest {

    @Test
    public void test(){
        MapValue mapValue = new MapValue();
        Assertions.assertNull(mapValue.get("unknown"));

        mapValue.put("var", new NumberValue(100.1));
        Assertions.assertEquals(mapValue.get("var").toNumber(), 100.1);

        Assertions.assertThrows(RuntimeException.class, () -> {
            mapValue.put("var.subvar", new StringValue("the text value"));
        }, "нельзя обращаться к термальной переменной как к мапе");

        Assertions.assertNull(mapValue.get("nonextst"), "несуществующая переменная == NULL");
        Assertions.assertNull(mapValue.get("nonextst"), "обращение к несуществующей переменной не создаёт её");
        Assertions.assertNull(mapValue.get("nonextst.subvar"), "параметры у несуществующей переменной == NULL");
        Assertions.assertNull(mapValue.get("nonextst"), "обращение к параметру несуществующей переменной не создаёт её");

        mapValue.put("var", new MapValue()); // теперь эта переменная - пустая мапа
        String sub_var_text_value = "sub var text value";
        mapValue.put("var.subvar.subsubvar", new StringValue(sub_var_text_value)); // инициализируем вложенное значение
        Assertions.assertTrue(mapValue.get("var") instanceof MapValue);
        Assertions.assertTrue(mapValue.get("var.subvar") instanceof MapValue);
        Assertions.assertTrue(mapValue.get("var.subvar.subsubvar") instanceof StringValue);
        Assertions.assertEquals(mapValue.get("var.subvar.subsubvar").toString(), sub_var_text_value);

        // создаем мапу через инициализацию вложенного значения
        mapValue.put("newvar.newsubvar.newsubsubvar", new StringValue("the value"));
    }


}