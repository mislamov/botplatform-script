package ru.maratislamov.script.parser;

import org.junit.jupiter.api.Test;
import ru.maratislamov.script.ScriptEngine;

import java.util.ArrayList;
import java.util.Objects;

class ParserTest {

    @Test
    public void test(){
        ArrayList<Token> tokens = new ArrayList<>();
        Parser parser = new Parser(new ScriptEngine(), tokens);

        assert Objects.equals(parser.lastTokensAsString(), "");

        tokens.add(new Token("X", TokenType.WORD));
        assert Objects.equals(parser.lastTokensAsString(), "X");

        tokens.add(new Token("=", TokenType.OPERATOR));
        assert Objects.equals(parser.lastTokensAsString(), "X=");

        tokens.add(new Token("value", TokenType.STRING));
        assert Objects.equals(parser.lastTokensAsString(), "=value");
    }

    @Test
    public void testList(){

    }


}