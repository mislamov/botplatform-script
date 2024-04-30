package ru.maratislamov.script.values;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.expressions.VariableExpression;
import ru.maratislamov.script.statements.AssignStatement;
import ru.maratislamov.script.utils.VarLocalMemoryManager;

import java.util.function.Consumer;

public class MapValueTest {

    public void setVarToScope(String path, Value value, ScriptSession session) {
        final Consumer<Value> setter = session.getVarManager().getValueSetterByPath(session, new VariableExpression(path));
        setter.accept(value);
    }

    @Test
    public void testScope() {
        MapValue scope = new MapValue();
        Assertions.assertNull(scope.get("unknown"));

        final ScriptSession session = new ScriptSession(scope);

        setVarToScope("var", new NumberValue(100.1), session);
        Assertions.assertEquals(scope.get("var").toNumber(), 100.1);

        Assertions.assertThrows(RuntimeException.class, () -> {
            setVarToScope("var.subvar", new StringValue("the text value"), session);
        }, "нельзя обращаться к термальной переменной как к мапе");


        Assertions.assertEquals(Value.NULL, new VariableExpression("nonextst").evaluate(session), "несуществующая переменная == NULL");
        Assertions.assertEquals(Value.NULL, new VariableExpression("nonextst").evaluate(session), "обращение к несуществующей переменной не создаёт её");
        Assertions.assertEquals(Value.NULL, new VariableExpression("nonextst.subvar").evaluate(session), "параметры у несуществующей переменной == NULL");
        Assertions.assertEquals(Value.NULL, new VariableExpression("nonextst").evaluate(session), "обращение к параметру несуществующей переменной не создаёт её");

        setVarToScope("var", new MapValue(), session);  // теперь эта переменная - пустая мапа
        String sub_var_text_value = "sub var text value";

        // инициализируем вложенное значение
        setVarToScope("var.subvar.subsubvar", new StringValue(sub_var_text_value), session);

        Assertions.assertTrue(new VariableExpression("var").evaluate(session) instanceof MapValue);
        Assertions.assertTrue(new VariableExpression("var.subvar").evaluate(session) instanceof MapValue);
        Assertions.assertTrue(new VariableExpression("var.subvar.subsubvar").evaluate(session) instanceof StringValue);
        Assertions.assertEquals(new VariableExpression("var.subvar.subsubvar").evaluate(session).toString(), sub_var_text_value);

        // создаем мапу через инициализацию вложенного значения
        setVarToScope("newvar.newsubvar.newsubsubvar", new StringValue("the value"), session);
        Assertions.assertTrue(new VariableExpression("newvar").evaluate(session) instanceof MapValue);
        Assertions.assertTrue(new VariableExpression("newvar.newsubvar").evaluate(session) instanceof MapValue);
        Assertions.assertTrue(new VariableExpression("newvar.newsubvar.newsubsubvar").evaluate(session) instanceof StringValue);
        Assertions.assertEquals(((StringValue) new VariableExpression("newvar.newsubvar.newsubsubvar").evaluate(session)).getValue(), "the value");

        setVarToScope("aaa.bbb", Value.NULL, session);
        Assertions.assertTrue(new VariableExpression("aaa.bbb").evaluate(session) instanceof NULLValue);
        setVarToScope("aaa.bbb.ccc", new StringValue("str"), session);
        Assertions.assertTrue(new VariableExpression("aaa.bbb").evaluate(session) instanceof MapValue);
        Assertions.assertTrue(new VariableExpression("aaa.bbb.ccc").evaluate(session) instanceof StringValue);
        Assertions.assertEquals(((StringValue) new VariableExpression("aaa.bbb.ccc").evaluate(session)).getValue(), "str");
    }

}
