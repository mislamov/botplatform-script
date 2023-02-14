package ru.maratislamov.script.parser;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.maratislamov.script.ScriptEngine;
import ru.maratislamov.script.expressions.*;
import ru.maratislamov.script.statements.*;
import ru.maratislamov.script.values.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This defines the Jasic parser. The parser takes in a sequence of tokens
 * and generates an abstract syntax tree. This is the nested data structure
 * that represents the series of statements, and the expressions (which can
 * nest arbitrarily deeply) that they evaluate. In technical terms, what we
 * have is a recursive descent parser, the simplest kind to hand-write.
 * <p>
 * As a side-effect, this phase also stores off the line numbers for each
 * label in the program. It's a bit gross, but it works.
 */
public class Parser {
    Logger logger = LoggerFactory.getLogger(Parser.class);

    private final ScriptEngine botScript;

    private final List<Token> tokens;
    private int position;
    private int positionLine;

    public Parser(ScriptEngine botScript, List<Token> tokens) {
        this.botScript = botScript;
        this.tokens = tokens;
        position = 0;
        positionLine = 0;
    }

    public String debugCurrentPosition() {
        int i = 0;
        while (get(i).type == TokenType.LINE) {
            --i;
        }

        StringBuilder result = new StringBuilder();
        for (; i < 10; ++i) {
            result.append(get(i).text);
            result.append(" ");
        }
        String rest = StringUtils.defaultIfEmpty(result.toString().trim(), null);
        return rest == null ? lastTokensAsString() : rest;
    }

    // два последних токена
    protected String lastTokensAsString() {
        if (tokens.size() >= 2) {
            return tokens.subList(tokens.size() - 2, tokens.size()).stream().map(token -> token.text).collect(Collectors.joining());
        }
        return tokens.isEmpty() ? "" : tokens.get(0).text;
    }


    /**
     * The top-level function to start parsing. This will keep consuming
     * tokens and routing to the other parse functions for the different
     * grammar syntax until we run out of code to parse.
     *
     * @param labels A map of label names to statement indexes. The
     *               parser will fill this in as it scans the code.
     * @return The list of parsed statements.
     */
    public List<Statement> parseCommands(Map<String, Integer> labels) {
        return parseCommands(labels, false);
    }

    /**
     * @param labels
     * @param parseHolder - если true - принудительно парсим переменную или холдер: ${ _from_here_ }
     * @return
     */
    public List<Statement> parseCommands(Map<String, Integer> labels, boolean parseHolder) {
        List<Statement> statements = new ArrayList<>();

        while (true) {

            skipSpacesAndSeps();

            // label:
            if (match(TokenType.LABEL)) {
                // Mark the index of the statement after the label.
                labels.put(last(1).text, statements.size());

                // fn ...
            } else if (peek(TokenType.WORD)) {
                Expression atomStartsByWord = atomic();

                // FN()
                if (atomStartsByWord instanceof MethodCallValue) {
                    statements.add((MethodCallValue) atomStartsByWord);

                } else {
                    /////// fn | a.b[].c | d[]
                    assert atomStartsByWord instanceof VariableExpression;
                    VariableExpression var = (VariableExpression) atomStartsByWord;
                    final VariableExpression nextInPath = ((VariableExpression) atomStartsByWord).getNextInPath();

                    String operator = operator();

                    if (operator != null) {

                        // var =
                        if (operator.equals("=")) {
                            // присваивание
                            skipSpaces();
                            Expression value = expression();
                            statements.add(new AssignStatement(var, value));

                            // var *=  // var +=  // var -=  // var /=
                        } else if (operator.length() == 2 && operator.charAt(1) == '=') {
                            // присваивание
                            String modify = String.valueOf(operator.charAt(0));
                            Expression value = expression();
                            statements.add(new AssignStatement(var, new BinaryOperatorExpression(var, modify, value)));

                        } else {
                            throw new RuntimeException("ERROR [line " + (positionLine + 1) + "]: unknown operator for command: " + operator);
                        }
                    } else {
                        // нет оператора после атомарного выражения

                        if (nextInPath != null) {
                            // просто сложная переменная (path)
                            statements.add(new WrapStatement(atomStartsByWord));

                        } else {
                            // WORD - просто слово (не составная переменная, ни присваивание, ни вызов функции итп
                            final String atomWord = atomStartsByWord.getName();

                            if (atomWord.equals("goto")) {
                                final Expression label = mathExpression(TokenType.LINE, TokenType.COMMAND_SEP);
                                statements.add(new GotoStatement(botScript, label));

                                // if
                            } else if (atomWord.equals("if")) {
                                Expression condition = expression();
                                consume("then");

                                int gotoIndex = findIndexOfNext(TokenType.LINE, TokenType.EOF); // todo: искать полноценный блок
                                if (gotoIndex < 0) {
                                    gotoIndex = tokens.size(); // end of script
                                } else {
                                    ++gotoIndex;
                                }
                                String label = "____if" + gotoIndex;
                                tokens.add(gotoIndex, new Token(label, TokenType.LABEL));

                                statements.add(new IfThenStatement(botScript, condition, null, label));


                                // input
                            } else if (atomWord.equals("input")) { // аргументы (TermValue) у input не должны вычисляться перед вызовом
                                Expression arg = atomic();
                                assert arg instanceof VariableExpression;

                                statements.add(new AssignStatement((VariableExpression) arg, new MethodCallValue("input", List.of())));

                                consume(TokenType.LINE, TokenType.EOF, TokenType.COMMAND_SEP);

                                // command  arg arg arg \n
                                // or
                                // wait \n
                            } else {
                                List<Expression> argList = new ArrayList<>();
                                while (true) {
                                    final TokenType type = get(0).type;
                                    if (type == TokenType.LINE || type == TokenType.EOF || type == TokenType.COMMAND_SEP)
                                        break;

                                    Expression expression = expression(TokenType.LINE, TokenType.COMMAND_SEP);
                                    argList.add(expression);
                                }
                                position++;

                                if (parseHolder && argList.isEmpty()) {
                                    // одно слово в холдере - это переменная
                                    statements.add(new WrapStatement(atomStartsByWord));
                                } else {
                                    // если есть аргументы или одинокое слово в потоке команд - это команда
                                    statements.add(new MethodCallValue(atomWord, argList));
                                    if (atomWord.equals("wait")) {
                                        statements.add(new MethodCallValue("interrupt wait", List.of())); // удаление таймера
                                    }
                                }
                            }
                        }
                    }
                }

            } else if (match(TokenType.EOF)) {
                break;

            } else {
                throw new RuntimeException("ERROR [line " + (positionLine + 1) + "]: near '" + debugCurrentPosition() + "'");
            }
        }

        return statements;
    }

    private ScriptEngine engine4frame = new ScriptEngine();

    protected List<Expression> frameTextToArgList(String text) {
        List<Expression> result = new ArrayList<>();

        final String[] parts = text.split("\\$");

        if (!"".equals(parts[0])) {
            result.add(new StringValue(parts[0]));
        }

        for (int i = 1; i < parts.length; i++) {
            String ln = parts[i];
            if (ln.equals("")) {  // $$
                result.add(new StringValue("$"));
                if (!"".equals(parts[i + 1])) {
                    result.add(new StringValue(parts[i + 1]));
                    i += 1;
                }
                continue;

            } else {
                int idx = endOfVarName(ln);

                if (idx == 0) { // _$_
                    result.add(new StringValue("$" + ln));
                    continue;
                }

                final String varCode = ln.substring(0, idx);
                final Statement varStatement = engine4frame.scriptToStatements(varCode, true).get(0);
                assert varStatement instanceof WrapStatement;
                result.add(((WrapStatement) varStatement).getExpression());

                if (idx < ln.length()) {
                    result.add(new StringValue(ln.substring(idx)));
                }
            }
        } //  fff$$ddd ; fff$$$ddd

        if (text.endsWith("$")) {
            result.add(new StringValue("$"));
        }

        return result;
    }

    private int endOfVarName(String code) {
        for (int i = 0; i < code.length(); ++i) {
            if (List.of('.', '_', '[', ']').contains(code.charAt(i))) continue;
            if (Character.isLetterOrDigit(code.charAt(i))) continue;
            return i;
        }
        return code.length();
    }

    // пропуск разделителей строк команд
    private void skipSpacesAndSeps() {
        while (peek(TokenType.LINE) || peek(TokenType.COMMAND_SEP)) {
            ++position;

            if (peek(TokenType.LINE)) {
                ++positionLine;
            }
        }
    }

    // пропуск разделителей строк
    private void skipSpaces() {
        while (peek(TokenType.LINE)) {
            ++positionLine;
            ++position;
        }
    }

    // The following functions each represent one grammatical part of the
    // language. If this parsed English, these functions would be named like
    // noun() and verb().

    /**
     * Parses a single expression. Recursive descent parsers start with the
     * lowest-precedent term and moves towards higher precedence. For Jasic,
     * binary operators (+, -, etc.) are the lowest.
     *
     * @return The parsed expression.
     */
    private Expression expression(TokenType... stoppers) {

        // список
        if (match(TokenType.BEGIN_LIST)) {
            List<Expression> expressionList = new ArrayList<>();

            while (!match(TokenType.END_LIST)) {
                skipSpaces(); // [\n  ,\n

                expressionList.add(expression());

                skipSpaces(); // xxx\n]

                if (!match(TokenType.COMMA) && !peek(TokenType.END_LIST)) {
                    throw new Error("List parse error here: " + debugCurrentPosition());
                }
            }
            return new ListExpressions(expressionList);
        }

        // мапа
        if (match(TokenType.BEGIN_MAP)) {
            Map<String, Expression> map = new LinkedHashMap<>();

            while (!match(TokenType.END_MAP)) {
                skipSpaces(); // {\n  ,\n

                consume(TokenType.STRING, TokenType.LABEL);
                String key = get(-1).text;

                if (get(-1).type != TokenType.LABEL) {
                    consume(TokenType.MAP_SEP); // :
                }
                Expression val = expression();

                map.put(key, val);
                skipSpaces(); // xxx\n}

                if (!match(TokenType.COMMA) && !peek(TokenType.END_MAP)) {
                    throw new Error("List parse error here: " + debugCurrentPosition());
                }
            }
            return new MapExpressions(map);

        }

        // оператор
        return mathExpression(stoppers);
    }


    protected String operator() {
        if (!peek(TokenType.OPERATOR)) return null;

        Set<String> operators = new HashSet<>(Arrays.asList(
                "+", "-", "*", "/", "%", "=",
                "===", "==", "!=", "+=", "-=", "/=", "*=", "%=", "->",
                "<", ">", "<=", ">=", "&&", "||"));

        String operator = "";
        String lastOperator = null;
        int lastOperatorPosition = position; // todo: считать positionLine

        while (match(TokenType.OPERATOR)) {
            final String ch = last(1).text;
            operator += ch;

            if (operators.contains(operator)){
                lastOperator = operator;
                lastOperatorPosition = position;
            }
        }

        position = lastOperatorPosition;
        return lastOperator;
    }


    private String stackEvaluteCurrentBracketLevel(Stack<Expression> stack, Stack<String> operators) {
        while (!operators.isEmpty() && !operators.peek().equals("(")) {
            String operator = operators.pop();

            switch (operator) {

                case "+", "-" -> {
                    final Expression right = stack.pop();
                    final String br = stackEvaluteCurrentBracketLevel(stack, operators);
                    if (br != null) {
                        operators.push("(");
                    }

                    stack.push(new BinaryOperatorExpression(stack.isEmpty() ? new NumberValue(0) : stack.pop(), operator, right));
                }

                case "===", "==", "!=", "=", "/", "*", ">", "<", ">=", "<=", "&&", "||" -> {
                    final Expression right = stack.pop();
                    stack.push(new BinaryOperatorExpression(stack.pop(), operator, right));
                }

                default -> {
                    throw new RuntimeException("NYR for operator: " + operator);
                }
            }
        }
        return operators.isEmpty() ? null : operators.pop();
    }


    /**
     * Parses a series of binary operator expressions into a single
     * expression.
     *
     * @param stoppers
     * @return The parsed expression.
     */
    private Expression mathExpression(TokenType... stoppers) {
        Stack<Expression> stack = new Stack<>();
        Stack<String> operators = new Stack<>();

        parsingMath:
        while (true) {

            if (stoppers != null && Arrays.asList(stoppers).contains(get(0).type)) break;

            skipSpaces();

            Token token = get(0);

            switch (token.type) {
                case LEFT_PAREN ->  // "("
                        operators.push(token.text);

                case OPERATOR -> {
                    String currentOperatorCode = operator();
                    switch (currentOperatorCode) {
                        case "+", "-" -> {
                            while (!operators.isEmpty() && (operators.peek().equals("+") || operators.peek().equals("-"))) {
                                final Expression right = stack.pop();
                                stack.push(new BinaryOperatorExpression(stack.pop(), operators.pop(), right));
                            }
                            operators.push(currentOperatorCode);
                        }

                        case "*", "/" -> {
                            while (!operators.isEmpty() && (operators.peek().equals("*") || operators.peek().equals("/"))) {
                                Expression right = stack.pop();
                                stack.push(new BinaryOperatorExpression(stack.pop(), operators.pop(), right));
                            }
                            operators.push(currentOperatorCode);
                        }

                        case "===", "==", "=", "<", ">", "<=", ">=", "!=", "&&", "||" -> {
                            String bracket = stackEvaluteCurrentBracketLevel(stack, operators);
                            if (bracket != null) { // EOF
                                operators.push(bracket); // возвращаем скобку обратно
                            }
                            operators.push(currentOperatorCode);
                        }

                        default -> throw new RuntimeException("NYR operator: " + currentOperatorCode + "  here: " + debugCurrentPosition());
                    }
                    continue;
                }

                case RIGHT_PAREN -> {
                    String bracket = stackEvaluteCurrentBracketLevel(stack, operators);

                    if (!Objects.equals(bracket, "(")) {
                        //throw new RuntimeException("Unexpected closed bracket");
                        //завершен парсинг замыкающего выражения в списке:  (... ,..., <math>) <-
                        assert stack.size() == 1;
                        assert operators.isEmpty();
                        break parsingMath;
                    }

                    ++position;
                    skipSpaces();

                    // если дальше после АТОМА арифметическое выражение продолжается
                    if (!peek_one_of(0, TokenType.OPERATOR, TokenType.RIGHT_PAREN)) {
                        break parsingMath;
                    }

                    continue parsingMath;
                }

                case EOF -> {
                    break parsingMath;
                }

                default -> {
                    final Expression atomic = atomic();
                    stack.push(atomic);

                    if (stoppers != null && Arrays.asList(stoppers).contains(get(0).type)) break parsingMath;
                    skipSpaces();

                    if (!peek_one_of(0, TokenType.OPERATOR, TokenType.RIGHT_PAREN)) {
                        // арифм выражение завершено
                        break parsingMath;
                    }
                    // если дальше после АТОМА арифметическое выражение продолжается
                    continue parsingMath;

                }
            }

            position++;
        }

        /*while (!operators.isEmpty()) {
            String operator = operators.pop();

            switch (operator) {
                case "+", "*" -> stack.push(new BinaryOperatorExpression(stack.pop(), operator, stack.pop()));

                case "-", "/" -> {
                    final Expression right = stack.pop();
                    stack.push(new BinaryOperatorExpression(stack.pop(), operator, right));
                }
                default -> {
                    throw new RuntimeException("NYR for operator: " + operator);
                }
            }
        }*/
        final String br = stackEvaluteCurrentBracketLevel(stack, operators);
        if (br != null) {
            throw new RuntimeException("Unbalanced brackets here: " + debugCurrentPosition());
        }

        assert stack.size() == 1;
        return stack.pop();

    }

    /**
     * Parses an "atomic" expression. This is the highest level of
     * precedence and contains single literal tokens like 123 and "foo", as
     * well as parenthesized expressions.
     *
     * @return The parsed expression.
     */
    private Expression atomic() {
        return atomic(false);
    }

    private Expression atomic(boolean readVariableForce) {

        skipSpaces();

        if (match(TokenType.WORD) || (readVariableForce && match(TokenType.DIGITS))) {
            // A word is a reference to a variable.
            Token word = last(1);
            String prev = word.text;
            String prevUpper = prev.toUpperCase();

            /*if (KEYWORDS.contains(prevUpper) || COMMANDS.contains(prevUpper)) {
                return new TermValue(prev);
            }*/

            if ("NULL".equals(prevUpper)) {
                return Value.NULL;
            }

            if (word.isSeparatedWord()) {
                return new VariableExpression(word.text);
            }

            // fn(a,b,c)
            if (peek(TokenType.LEFT_PAREN)) {
                String fname = prev;
                List<Expression> argList = new ArrayList<>();
                match(TokenType.LEFT_PAREN);

                // fn()
                if (peek(TokenType.RIGHT_PAREN)) {
                    consume(TokenType.RIGHT_PAREN);
                    return new MethodCallValue(fname, argList);
                }

                //fn(a
                Expression arg = expression();
                argList.add(arg);

                // fn(a,b,c
                while (peek(TokenType.COMMA)) {
                    match(TokenType.COMMA);
                    argList.add(expression());
                }
                consume(TokenType.RIGHT_PAREN);
                return new MethodCallValue(fname, argList);
            }


            // ------------ for VARIABLES ONLY :
            VariableExpression theVariable = null;
            VariableExpression lastSubVariable = null;

            // fn[index]
            if (peek(TokenType.BEGIN_LIST)) {
                Expression arg = expression();
                assert get(-1).type == TokenType.END_LIST;

                assert arg instanceof ListExpressions && ((ListExpressions) arg).size() == 1;

                final VariableExpression variableExpression = new VariableExpression(prev);
                lastSubVariable = new VariableExpression(variableExpression, ((ListExpressions) arg).get(0));
                theVariable = variableExpression;

            } else {
                // fn
                theVariable = new VariableExpression(prev);
                lastSubVariable = theVariable;
            }

            assert theVariable.getLastInPath() == lastSubVariable;

            // fn.
            while (match(TokenType.DOT)) {
                // subvar
                Expression atomic = atomic(true);

                // fn.fn | fn.4
                if (/*atomic instanceof VariableExpression || */atomic instanceof NumberValue) {
                    assert lastSubVariable.getNextInPath() == null;
                    final VariableExpression variableExpression = new VariableExpression(atomic.toString());
                    lastSubVariable.setNextInPath(variableExpression);
                    lastSubVariable = variableExpression.getLastInPath(); // нужно, поскольку Number может быть вида 123.456

                    // fn.var[...]
                } else if (atomic instanceof VariableExpression) {
                    assert lastSubVariable.getNextInPath() == null;
                    lastSubVariable.setNextInPath((VariableExpression) atomic);
                    lastSubVariable = ((VariableExpression) atomic).getLastInPath();

                } else {
                    throw new RuntimeException(String.format("Unexpected sub-variable syntax: %s for %s", atomic.toString(), theVariable));
                }
            }

            if (readVariableForce) return theVariable; // заранее знаем, что это переменная

            return theVariable;
            // ------------ END VARIABLES ONLY


        } else if (match(TokenType.DIGITS)) {
            final String textNumber = last(1).text;
            String suffix = "";

            if (match(TokenType.DOT, TokenType.DIGITS)) {
                suffix = "." + last(1).text;
            }
            return readVariableForce ? new VariableExpression(textNumber + suffix) : new NumberValue(textNumber + suffix);

            // положительное или отрицательное число
        } else if (peek(TokenType.OPERATOR) && (get(0).text.equals("-") || get(0).text.equals("+")) && get(1).type == TokenType.DIGITS) {
            String sign = get(0).text;
            ++position;
            final String textNumber = sign + get(0).text;
            String suffix = "";
            ++position;

            if (match(TokenType.DOT, TokenType.DIGITS)) {
                suffix = "." + last(1).text;
            }
            return readVariableForce ? new VariableExpression(textNumber + suffix) : new NumberValue(textNumber + suffix);


        } else if (match(TokenType.STRING)) {
            return new StringValue(last(1).text);

            // """  any ${text}  """
        } else if (match(TokenType.STRING_FRAME)) {
            return new StringFrameValue(frameTextToArgList(last(1).text));

        } else if (match(TokenType.LEFT_PAREN)) {
            // The contents of a parenthesized expression can be any
            // expression. This lets us "restart" the precedence cascade
            // so that you can have a lower precedence expression inside
            // the parentheses.
            Expression expression = expression();
            consume(TokenType.RIGHT_PAREN);
            return expression;

        } else if (peek(TokenType.BEGIN_LIST)) {
            // арифм. операции со списками
            Expression expression = expression();
            --position;
            consume(TokenType.END_LIST);
            return expression;
        }

        String position = debugCurrentPosition();
        throw new Error("Can't parse line " + (positionLine + 1) + ": " + position);
    }

    // The following functions are the core low-level operations that the
    // grammar parser is built in terms of. They match and consume tokens in
    // the token stream.

    /**
     * Consumes the next two tokens if they are the given type (in order).
     * Consumes no tokens if either check fais.
     *
     * @param type1 Expected type of the next token.
     * @param type2 Expected type of the subsequent token.
     * @return True if tokens were consumed.
     */
    private boolean match(TokenType type1, TokenType type2) {
        if (get(0).type != type1) return false;
        if (get(1).type != type2) return false;
        position += 2;
        return true;
    }

    private boolean match(TokenType type1, TokenType type2, TokenType type3) {
        if (get(0).type != type1) return false;
        if (get(1).type != type2) return false;
        if (get(2).type != type3) return false;
        position += 3;
        return true;
    }

    /**
     * Consumes the next token if it's the given type.
     *
     * @param type Expected type of the next token.
     * @return True if the token was consumed.
     */
    private boolean match(TokenType type) {
        if (get(0).type != type) return false;
        position++;
        return true;
    }

    private boolean peek(TokenType type) {
        if (get(0).type != type) return false;
        return true;
    }

    private boolean peek_one_of(int position, TokenType... types) {
        final Token token = get(position);
        return Arrays.stream(types).anyMatch(token.type::equals);
    }

    /**
     * Consumes the next token if it's a word token with the given name.
     *
     * @param name Expected name of the next word token.
     * @return True if the token was consumed.
     */
    private boolean match(String name) {
        if (get(0).type != TokenType.WORD) return false;
        if (!get(0).text.equals(name)) return false;
        position++;
        return true;
    }

    /**
     * Consumes the next token if it's the given type. If not, throws an
     * exception. This is for cases where the parser demands a token of a
     * certain type in a certain position, for example a matching ) after
     * an opening (.
     *
     * @param type Expected type of the next token.
     * @return The consumed token.
     */
    private Token consume(TokenType type) {
        final Token token = get(0);
        if (token.type != type)
            throw new Error("Expected " + type + " on line " + (positionLine + 1) + ": " + debugCurrentPosition());
        position++;
        return token;
    }

    private Token consume(TokenType... types) {
        final Token token = get(0);
        if (Arrays.stream(types).noneMatch(token.type::equals))
            throw new Error("Expected one of type " + Arrays.toString(types) + "' in line " + (positionLine + 1) + "+");
        ++position;
        if (token.type == TokenType.LINE) ++positionLine;
        return token;
    }

    /**
     * Consumes the next token if it's a word with the given name. If not,
     * throws an exception.
     *
     * @param name Expected name of the next word token.
     * @return The consumed token.
     */
    private Token consume(String name) {
        if (!match(name))
            throw new Error("Expected '" + name + "' in line " + (positionLine + 1) + " but: " + debugCurrentPosition());
        return last(1);
    }

    /**
     * Gets a previously consumed token, indexing backwards. last(1) will
     * be the token just consumed, last(2) the one before that, etc.
     *
     * @param offset How far back in the token stream to look.
     * @return The consumed token.
     */
    private Token last(int offset) {
        return tokens.get(position - offset);
    }

    /**
     * Gets an unconsumed token, indexing forward. get(0) will be the next
     * token to be consumed, get(1) the one after that, etc.
     *
     * @param offset How far forward in the token stream to look.
     * @return The yet-to-be-consumed token.
     */
    private Token get(int offset) {
        if (position + offset >= tokens.size()) {
            return new Token("", TokenType.EOF);
        }
        return tokens.get(position + offset);
    }

    private int findIndexOfNext(TokenType... types) {
        int i = 0;
        int size = tokens.size();

        while (position + i < size && Arrays.stream(types).noneMatch(tokens.get(position + i).type::equals)) {
            ++i;
        }
        return position + i == size ? -1 : position + i;
    }

}
