package ru.maratislamov.script.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.maratislamov.script.ScriptEngine;

import java.util.ArrayList;
import java.util.Objects;

class ParserTest {

    @Test
    public void test(){
        ArrayList<Token> tokens = new ArrayList<>();
        Parser parser = new Parser(new ScriptEngine(), tokens);

        Assertions.assertEquals(parser.lastTokensAsString(), "");

        tokens.add(new Token("X", TokenType.WORD));
        Assertions.assertEquals(parser.lastTokensAsString(), "X");

        tokens.add(new Token("=", TokenType.OPERATOR));
        Assertions.assertEquals(parser.lastTokensAsString(), "X=");

        tokens.add(new Token("value", TokenType.STRING));
        Assertions.assertEquals(parser.lastTokensAsString(), "=value");
    }

    @Test
    public void testPlusEq(){
        ArrayList<Token> tokens = new ArrayList<>();
        Parser parser = new Parser(new ScriptEngine(), tokens);

        Assertions.assertEquals(parser.lastTokensAsString(), "");

        tokens.add(new Token("X", TokenType.WORD));
        Assertions.assertEquals(parser.lastTokensAsString(), "X");

        tokens.add(new Token("+=", TokenType.OPERATOR));
        Assertions.assertEquals(parser.lastTokensAsString(), "X+=");

        tokens.add(new Token("value", TokenType.STRING));
        Assertions.assertEquals(parser.lastTokensAsString(), "+=value");
    }

    @Test
    public void testList(){

    }


}