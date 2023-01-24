package ru.maratislamov.script.values.google;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

class SparseArrayTest {

    @Test
    public void test(){
        SparseArray<String> strings = new SparseArray<>();
        strings.addAll(Arrays.asList("a", "b", "c"));
        System.out.println(strings);
    }


}