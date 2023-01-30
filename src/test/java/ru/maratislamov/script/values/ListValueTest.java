package ru.maratislamov.script.values;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

class ListValueTest {

    @Test
    public void test2(){
        final List<Value> values = Arrays.asList(Value.from("1"), Value.from("2"), Value.from("3"));
        ListValue listValue = new ListValue(values);

        Assertions.assertEquals(values.size(), listValue.size());

        final Iterator<Value> iterator1 = listValue.getIterator();
        final Iterator<Value> iterator2 = values.stream().toList().iterator();

        Assertions.assertEquals(iterator2.next(), iterator1.next());
        Assertions.assertEquals(iterator2.next(), iterator1.next());
        Assertions.assertEquals(iterator2.next(), iterator1.next());

        assert !iterator1.hasNext();
        assert !iterator2.hasNext();
    }

}