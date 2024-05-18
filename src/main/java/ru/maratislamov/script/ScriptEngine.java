package ru.maratislamov.script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.maratislamov.script.context.ScriptRunnerContext;
import ru.maratislamov.script.parser.ParserSession;
import ru.maratislamov.script.parser.Token;
import ru.maratislamov.script.parser.Tokenizer;
import ru.maratislamov.script.statements.Statement;
import ru.maratislamov.script.values.MapValue;
import ru.maratislamov.script.values.SuspendValue;
import ru.maratislamov.script.values.Value;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RecursiveTask;

/**
 * This defines a single class that contains an entire interpreter for a
 * language very similar to the original BASIC. Everything is here (albeit in
 * very simplified form): tokenizing, parsing, and interpretation. The file is
 * organized in phases, with each appearing roughly in the order that they
 * occur when a program is run. You should be able to read this top-down to walk
 * through the entire process of loading and running a program.
 * <p>
 * Jasic language syntax
 * ---------------------
 * <p>
 * Comments start with ' and proceed to the end of the line:
 * <p>
 * print "hi there" ' this is a comment
 * <p>
 * Numbers and strings are supported. Strings should be in "double quotes", and
 * only positive integers can be parsed (though numbers are double internally).
 * <p>
 * Variables are identified by name which must start with a letter and can
 * contain letters or numbers. Case is significant for names and keywords.
 * <p>
 * Each statement is on its own line. Optionally, a line may have a label before
 * the statement. A label is a name that ends with a colon:
 * <p>
 * foo:
 * <p>
 * <p>
 * The following statements are supported:
 *
 * <name> = <expression>
 * Evaluates the expression and assigns the result to the given named
 * variable. All variables are globally scoped.
 * <p>
 * pi = (314159 / 10000)
 * <p>
 * print <expression>
 * Evaluates the expression and prints the result.
 * <p>
 * print "hello, " + "world"
 * <p>
 * input <name>
 * Reads in a line of input from the user and stores it in the variable with
 * the given name.
 * <p>
 * input guess
 * <p>
 * goto <label>
 * Jumps to the statement after the label with the given name.
 * <p>
 * goto loop
 * <p>
 * if <expression> then <label>
 * Evaluates the expression. If it evaluates to a non-zero number, then
 * jumps to the statement after the given label.
 * <p>
 * if a < b then dosomething
 * <p>
 * <p>
 * The following expressions are supported:
 *
 * <expression> = <expression>
 * Evaluates to 1 if the two expressions are equal, 0 otherwise.
 *
 * <expression> + <expression>
 * If the left-hand expression is a number, then adds the two expressions,
 * otherwise concatenates the two strings.
 *
 * <expression> - <expression>
 * <expression> * <expression>
 * <expression> / <expression>
 * <expression> < <expression>
 * <expression> > <expression>
 * You can figure it out.
 *
 * <name>
 * A name in an expression simply returns the value of the variable with
 * that name. If the variable was never set, it defaults to 0.
 * <p>
 * All binary operators have the same precedence. Sorry, I had to cut corners
 * somewhere.
 * <p>
 * To keep things simple, I've omitted some stuff or hacked things a bit. When
 * possible, I'll leave a "HACK" note there explaining what and why. If you
 * make your own interpreter, you'll want to address those.
 *
 * @author Marat Islamov, Bob Nystrom
 */
public class ScriptEngine {
    private static final Logger logger = LoggerFactory.getLogger(ScriptEngine.class);

    // Loaded program as tokens
    List<Statement> statements;

    public final Map<String, Integer> labels;

    // todo: вынести константы скрипта в персистентную сущность по логике близкой к ScriptSession (один скрипт на разных пользователей)
    public MapValue constants;

    // Token data --------------------------------------------------------------

    // Parsing -----------------------------------------------------------------

    // Abstract syntax tree (AST) ----------------------------------------------

    // These classes define the syntax tree data structures. This is how code is
    // represented internally in a way that's easy for the interpreter to
    // understand.
    //
    // HACK: Unlike most real compilers or interpreters, the logic to execute
    // the code is baked directly into these classes. Typically, it would be
    // separated out so that the AST us just a static data structure.

    // Value types -------------------------------------------------------------

    // Interpreter -------------------------------------------------------------

    /**
     * Constructs a new Jasic instance. The instance stores the global state of
     * the interpreter such as the values of all of the variables and the
     * current statement.
     */
    public ScriptEngine() {
        this.labels = new HashMap<>();
        constants = new MapValue();
    }

    public ScriptEngine(MapValue constants) {
        this.labels = new HashMap<>();
        this.constants = constants;
    }

    /**
     * @param script A string containing the script code of a .jas script to interpret.
     * @return
     */
    public List<Statement> scriptToStatements(String script) {
        return scriptToStatements(script, false);
    }

    public List<Statement> scriptToStatements(String script, boolean forceHolder) {
        return scriptToStatements(new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8)), forceHolder);
    }

    public List<Statement> scriptToStatements(InputStream source) {
        return scriptToStatements(source, false);
    }

    public List<Statement> scriptToStatements(InputStream source, boolean forceHolder) {
        // Tokenize.
        List<Token> tokens = Tokenizer.tokenize(source);

        // Parse.
        ParserSession parserSession = new ParserSession(this, tokens);
        parserSession.parseCommandsUntilEndBlock(labels, forceHolder);
        return parserSession.getStatements();


    }

    public void load(InputStream source) {
        statements = scriptToStatements(source, false);
    }


    /**
     * This is where the magic happens. This runs the code through the parsing
     * pipeline to generate the AST. Then it executes each statement. It keeps
     * track of the current line in a member variable that the statement objects
     * have access to. This lets "goto" and "if then" do flow control by simply
     * setting the index of the current statement.
     * <p>
     * In an interpreter that didn't mix the interpretation logic in with the
     * AST node classes, this would be doing a lot more work.
     */
    public <TSession extends ScriptSession> TSession interpret(TSession session, List<Statement> statements) {
        if (statements == null) throw new RuntimeException("script is not loaded");
        if (!session.isActive()) throw new RuntimeException("Not active session for interpret");

        // Interpret until we're done.
        while (session.getCurrentStatement() < statements.size()) {
            int thisStatement = session.getCurrentStatement();
            Statement statementEntity = statements.get(thisStatement);

            logger.debug("interpret: {}", statementEntity);

            Value value = statementEntity.execute(session);

            if (session.getCurrentStatement() == thisStatement) {
                // если курсор исполняемого кода остался неизменен после execute (например из-за goto)
                session.incCurrentStatement();
            }

            if (value == SuspendValue.SUSPEND) {
                session.decCurrentStatement();
                return session;
            }

        }
        return doFinish(session);
    }


    public <TSession extends ScriptSession> TSession interpret(ScriptRunnerContext runnerContext) {
        return (TSession) interpret(new ScriptSession(runnerContext).activate());
    }

    public <TSession extends ScriptSession> TSession interpret(TSession session) {
        TSession resultSession = interpret(session, this.statements);
        return resultSession;
    }

    public <TSession extends ScriptSession> TSession doFinish(TSession session) {
        logger.info("THE END");
        session.close();
        return session;
    }

    /**
     * @return
     * @deprecated только для отладки
     */
    @Deprecated
    public List<Statement> getStatements() {
        return statements;
    }
}
