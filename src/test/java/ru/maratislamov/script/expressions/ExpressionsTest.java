package ru.maratislamov.script.expressions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.support.ReflectionSupport;
import ru.maratislamov.script.values.Value;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Predicate;

class ExpressionsTest {

    public static final String PACKAGE = "ru.maratislamov.script";
    public static final Class<Expression> CLASS = Expression.class;

    /**
     * Проверяем корректность сериализации (в json) выражений и значений
     *
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws JsonProcessingException
     */
    @Test
    public void test() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, JsonProcessingException {

        final Predicate<Class<?>> classPredicate = c -> CLASS.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers());

        ObjectMapper objectMapper = new ObjectMapper();

        final List<Class<?>> allClassesInPackage = ReflectionSupport.findAllClassesInPackage(PACKAGE, classPredicate, s -> true);
        for (Class<?> aClass : allClassesInPackage) {
            System.out.println(aClass);
            String json = objectMapper.writeValueAsString(aClass.getDeclaredConstructor().newInstance());
            System.out.println(json);
            final Expression value = objectMapper.readValue(json, CLASS);
            System.out.println(value);

        }

    }

}