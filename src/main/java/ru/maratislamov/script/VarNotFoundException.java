package ru.maratislamov.script;

public class VarNotFoundException extends RuntimeException {

    public VarNotFoundException() {
    }

    public VarNotFoundException(String str) {
        super(str);
    }
}
