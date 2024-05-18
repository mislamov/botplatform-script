package ru.maratislamov.script.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.maratislamov.script.ScriptEngine;

import java.util.ArrayList;

class ParserTest {

    @Test
    public void test0(){
        ArrayList<Token> tokens = new ArrayList<>();
        ParserSession parserSession = new ParserSession(new ScriptEngine(), tokens);

        Assertions.assertEquals(parserSession.lastTokensAsString(), "");

        tokens.add(new Token("X", TokenType.WORD));
        Assertions.assertEquals(parserSession.lastTokensAsString(), "X");

        tokens.add(new Token("=", TokenType.OPERATOR));
        Assertions.assertEquals(parserSession.lastTokensAsString(), "X=");

        tokens.add(new Token("value", TokenType.STRING));
        Assertions.assertEquals(parserSession.lastTokensAsString(), "=value");
    }

    @Test
    public void testPlusEq(){
        ArrayList<Token> tokens = new ArrayList<>();
        ParserSession parserSession = new ParserSession(new ScriptEngine(), tokens);

        Assertions.assertEquals(parserSession.lastTokensAsString(), "");

        tokens.add(new Token("X", TokenType.WORD));
        Assertions.assertEquals(parserSession.lastTokensAsString(), "X");

        tokens.add(new Token("+=", TokenType.OPERATOR));
        Assertions.assertEquals(parserSession.lastTokensAsString(), "X+=");

        tokens.add(new Token("value", TokenType.STRING));
        Assertions.assertEquals(parserSession.lastTokensAsString(), "+=value");
    }



}