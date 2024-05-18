package ru.maratislamov.script.parser;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.maratislamov.script.ScriptEngine;
import ru.maratislamov.script.expressions.*;
import ru.maratislamov.script.statements.*;
import ru.maratislamov.script.statements.loops.CreateIteratorStatement;
import ru.maratislamov.script.statements.loops.MoveIteratorStatement;
import ru.maratislamov.script.values.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This defines the Jasic parser session. The parser takes in a sequence of tokens
 * and generates an abstract syntax tree.
 */
public class ParserSession {
    public static final String ELSE_LABEL_PREFIX = "____else";
    Logger logger = LoggerFactory.getLogger(ParserSession.class);

    private final ScriptEngine botScript;

    private Stack<String[]> blocksAndLoopsLabels = new Stack<>(); // пары меток: начала итераций и точки выхода из циклов

    private List<Statement> statements = new ArrayList<>(); // дерево программы

    private final List<Token> tokens;
    private int position;
    private int positionLine;

    public ParserSession(ScriptEngine botScript, List<Token> tokens) {
        this.botScript = botScript;
        this.tokens = tokens;
        position = 0;
        positionLine = 0;
    }

    public List<Statement> getStatements() {
        return statements;
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
     * @param labels
     * @return
     * @deprecated for unit tests only
     */
    @Deprecated
    public List<Statement> parseCommandsUntilEndBlock(Map<String, Integer> labels) {
        parseCommandsUntilEndBlock(labels, false);
        return getStatements();
    }

    /**
     * @param labels
     * @param parseHolder - если true - принудительно парсим переменную или холдер: ${ _from_here_ }
     * @return
     */
    public void parseCommandsUntilEndBlock(Map<String, Integer> labels, boolean parseHolder) {

        while (true) {

            skipSpacesAndSeps();

            if (match(TokenType.EOF)) {
                return;

            } else if (match(TokenType.LABEL)) {
                // label:
                // Mark the index of the statement after the label.
                labels.put(last(1).text, statements.size());

                // fn ...
            } else if (peek(TokenType.WORD)) {
                boolean isEndBlock = parseInstructionCommand(labels, parseHolder);
                if (isEndBlock) {
                    return;
                }

            } else {
                throw new RuntimeException("ERROR [line " + (positionLine + 1) + "]: near '" + debugCurrentPosition() + "'");
            }
        }
    }


    Statement lastStat() {
        return statements.isEmpty() ? null : statements.get(statements.size() - 1);
    }

    /**
     * @param labels
     * @param parseHolder
     * @return признак окончания чтения текущего блока
     */
    private boolean parseInstructionCommand(Map<String, Integer> labels, boolean parseHolder) {
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


                        // for var in list  ....... end
                    } else if (atomWord.equals("for")) {

                        consume(TokenType.WORD); // var
                        assert get(-1).type == TokenType.WORD;
                        String varIteratorUserName = get(-1).text;

                        consume("in");
                        Expression collection = mathExpression(TokenType.LINE, TokenType.COMMAND_SEP, TokenType.EOF); // list

                        int idx = statements.size();
                        String forBeginLabel = "____for_in_begin_" + idx;
                        String forEndLabel = "____for_in_end_" + idx;
                        String forCollectionVarName = "____for_collection_" + idx;
                        blocksAndLoopsLabels.add(new String[]{forBeginLabel, forEndLabel});

                        String varIteratorInternal = "____iterator_impl_" + idx;
                        statements.add(new AssignStatement(new VariableExpression(forCollectionVarName), collection)); // for_collection = []
                        statements.add(new CreateIteratorStatement(varIteratorInternal, new VariableExpression(forCollectionVarName)));  // инициализируем итератор

                        labels.put(forBeginLabel, statements.size()); // метка начала итерации

                        statements.add(new MoveIteratorStatement(varIteratorInternal, varIteratorUserName)); // двигаем итератор
                        statements.add(new IfThenStatement(botScript, // если итерация невозможна - выход из цикла
                                new VariableExpression(varIteratorUserName), null, forEndLabel
                        ));

                        parseCommandsUntilEndBlock(labels, parseHolder);
                        assert get(-1).type == TokenType.WORD;
                        if (!Objects.equals(get(-1).text, "end")) throw new RuntimeException("end[for] expected");

                        statements.add(new GotoStatement(botScript, new StringValue(forBeginLabel)));
                        // end label
                        labels.put(forEndLabel, statements.size()); // метка выхода из цикла
                        blocksAndLoopsLabels.pop(); // assert


                        // while (cond)  ....... end
                    } else if (atomWord.equals("while")) {
                        String whileBeginLabel = "____while_begin_" + (statements.size());
                        String whileEndLabel = "____while_end_" + (statements.size());
                        blocksAndLoopsLabels.add(new String[]{whileBeginLabel, whileEndLabel});

                        Expression condition = expression();

                        labels.put(whileBeginLabel, statements.size()); // метка следующей итерации
                        statements.add(new IfThenStatement(botScript, condition, null, whileEndLabel)); // условие итерации

                        parseCommandsUntilEndBlock(labels, parseHolder);
                        assert get(-1).type == TokenType.WORD;
                        if (!Objects.equals(get(-1).text, "end")) throw new RuntimeException("end[while] expected");

                        statements.add(new GotoStatement(botScript, new StringValue(whileBeginLabel)));

                        // end label
                        labels.put(whileEndLabel, statements.size()); // метка выхода из цикла
                        blocksAndLoopsLabels.pop(); // assert

                    } else if (atomWord.equals("break")) {
                        statements.add(new GotoStatement(botScript, new StringValue(blocksAndLoopsLabels.peek()[1])));

                    } else if (atomWord.equals("continue")) {
                        statements.add(new GotoStatement(botScript, new StringValue(blocksAndLoopsLabels.peek()[0])));

                    } else if (atomWord.equals("begin")) {
                        // begin ...
                        /*String blockBeginLabel = BLOCK_BEGIN_LABEL_PREFIX + statements.size();
                        String blockEndLabel = BLOCK_END_LABEL_PREFIX + statements.size();
                        blocksAndLoopsLabels.add(new String[]{blockBeginLabel, blockEndLabel});
                        labels.put(blockBeginLabel, statements.size()); */

                        // к смещению начала данного блока добавляем смещение к текущей позиции в блоке = смещение нового блока
                        parseCommandsUntilEndBlock(labels, parseHolder);
                        assert get(-1).type == TokenType.WORD;
                        if (!Objects.equals(get(-1).text, "end")) throw new RuntimeException("end expected");


                    } else if (atomWord.equals("end")) {
                        // ..... end

                        /*String[] currentBlockLabels = blocksAndLoopsLabels.pop();

                        if (!currentBlockLabels[0].startsWith(BLOCK_BEGIN_LABEL_PREFIX)) {
                            // если не блок (значит цикл) - возвращаем управление в начало итерации к условию цикла
                            statements.add(new GotoStatement(botScript, new StringValue(currentBlockLabels[0]))); // отсылка н итерацию
                            labels.put(currentBlockLabels[1], statements.size()); // метка выхода из цикла
                        }*/
                        return true;

                        // if
                    } else if (atomWord.equals("if")) {
                        Expression condition = expression();
                        consume("then");

                        String labelForElse = ELSE_LABEL_PREFIX + statements.size();
                        // если condition=true тогда продолжаем (null) иначе прыгаем на labelForElse
                        statements.add(new IfThenStatement(botScript, condition, null, labelForElse));

                        parseInstructionCommand(labels, parseHolder);

                        // todo: проверить тут на else

                        labels.put(labelForElse, statements.size());


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
        return false; // простая команда - не окончание блока
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
        boolean skip = (stoppers == null || stoppers.length == 0); // for: cmd arg arg arg \n

        // список
        if (match(TokenType.BEGIN_LIST)) {
            List<Expression> expressionList = new ArrayList<>();

            while (!match(TokenType.END_LIST)) {
                if (skip) skipSpaces(); // [\n  ,\n

                expressionList.add(expression());

                if (skip) skipSpaces(); // xxx\n]

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
                if (skip) skipSpaces(); // {\n  ,\n

                consume(TokenType.STRING, TokenType.LABEL);
                String key = get(-1).text;

                if (get(-1).type != TokenType.LABEL) {
                    consume(TokenType.MAP_SEP); // :
                }
                Expression val = expression();

                map.put(key, val);
                if (skip) skipSpaces(); // xxx\n}

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

            if (operators.contains(operator)) {
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

                case "===", "==", "!=", "=", "/", "%", "*", ">", "<", ">=", "<=", "&&", "||" -> {
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
        boolean skip = (stoppers == null || stoppers.length == 0); // for: cmd arg arg arg \n

        Stack<Expression> stack = new Stack<>();
        Stack<String> operators = new Stack<>();

        parsingMath:
        while (true) {

            if (stoppers != null && Arrays.asList(stoppers).contains(get(0).type)) break;

            if (skip) skipSpaces();

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

                        case "*", "/", "%" -> {
                            while (!operators.isEmpty() && (operators.peek().equals("*") || operators.peek().equals("/"))) {
                                Expression right = stack.pop();
                                stack.push(new BinaryOperatorExpression(stack.pop(), operators.pop(), right));
                            }
                            operators.push(currentOperatorCode);
                        }

                        case "===", "==", "=", "<", ">", "<=", ">=", "!=", "&&", "||" -> {
                            // todo: после || и && и некоторых других - сразу вычислять мат.выражение справа, иначе будет взят только токен
                            // и произойдет ошибочное чтение для   if  {a==1 || b}==2 then
                            String bracket = stackEvaluteCurrentBracketLevel(stack, operators);
                            if (bracket != null) { // EOF
                                operators.push(bracket); // возвращаем скобку обратно
                            }
                            operators.push(currentOperatorCode);
                        }

                        default ->
                                throw new RuntimeException("NYR operator: " + currentOperatorCode + "  here: " + debugCurrentPosition());
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
                    if (skip) skipSpaces();

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
                    if (skip) skipSpaces();

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
            if ("TRUE".equals(prevUpper)) {
                return Value.from(true);
            }
            if ("FALSE".equals(prevUpper)) {
                return Value.from(false);
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
            VariableExpression theVariable = new VariableExpression(prev);
            VariableExpression lastSubVariable = theVariable;

            // fn[index][][][]
            while (peek(TokenType.BEGIN_LIST)) {
                Expression arg = expression();
                assert get(-1).type == TokenType.END_LIST;
                assert arg instanceof ListExpressions && ((ListExpressions) arg).size() == 1;

                lastSubVariable = new VariableExpression(lastSubVariable, ((ListExpressions) arg).get(0));
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

    private boolean peekPerv(TokenType type) {
        if (get(-1).type != type) return false;
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
