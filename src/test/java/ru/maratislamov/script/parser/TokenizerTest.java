package ru.maratislamov.script.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;


class TokenizerTest {
    @Test
    public void testQu(){
        // двойное экранирование кавычек
        List<Token> tokens = Tokenizer.tokenize(new ByteArrayInputStream("\"hello \"\"Monkey\"\" bye\"".getBytes(StandardCharsets.UTF_8)));
        assert tokens.size() == 1;
        assert tokens.get(0).type == TokenType.STRING;
        assert Objects.equals(tokens.get(0).text, "hello \"Monkey\" bye");
    }

    @Test
    public void testEscape(){
        // двойное экранирование кавычек
        List<Token> tokens = Tokenizer.tokenize(new ByteArrayInputStream("\"hello \\\"Monkey\\\" bye\"".getBytes(StandardCharsets.UTF_8)));
        System.out.println(tokens);

        Assertions.assertEquals(1, tokens.size());
        Assertions.assertEquals(TokenType.STRING, tokens.get(0).type);
        Assertions.assertEquals("hello \"Monkey\" bye", tokens.get(0).text);
    }


    @Test
    public void testEscapeN(){
        // двойное экранирование кавычек
        List<Token> tokens = Tokenizer.tokenize(new ByteArrayInputStream("\"line 1\\nline 2\"".getBytes(StandardCharsets.UTF_8)));
        System.out.println(tokens);

        Assertions.assertEquals(1, tokens.size());
        Assertions.assertEquals(TokenType.STRING, tokens.get(0).type);
        Assertions.assertEquals("line 1\nline 2", tokens.get(0).text);
    }

    @Test
    public void testList(){
        // двойное экранирование кавычек
        String textCode = "[1, \"2\", \"3\"]";
        System.out.println(textCode);
        List<Token> tokens = Tokenizer.tokenize(new ByteArrayInputStream(textCode.getBytes(StandardCharsets.UTF_8)));
        System.out.println(tokens);
        assert tokens.get(0).type == TokenType.BEGIN_LIST;
        assert tokens.get(tokens.size()-1).type == TokenType.END_LIST;
        Assertions.assertEquals(7, tokens.size());

        textCode = "keyboard [\"первый::onselect{selected=\"\"a\"\"}\", \"второй:onselect{selected=\"\"b\"\"}\", \"третий:onselect{selected=\"\"c\"\"}\"]";
        System.out.println(textCode);
        assert tokens.get(0).type == TokenType.BEGIN_LIST;
        assert tokens.get(tokens.size()-1).type == TokenType.END_LIST;
    }


}