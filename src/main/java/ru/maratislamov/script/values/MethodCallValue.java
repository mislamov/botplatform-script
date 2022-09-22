package ru.maratislamov.script.values;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.maratislamov.script.ScriptFunctionsImplemntator;
import ru.maratislamov.script.ScriptFunctionsImplemntatorFactory;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.expressions.Expression;
import ru.maratislamov.script.statements.Statement;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A string value.
 */
public class MethodCallValue implements Statement, Value {

    private static final Logger logger = LoggerFactory.getLogger(MethodCallValue.class);

    private final String call;

    private final List<Expression> args;

    public MethodCallValue(String call, List<Expression> args) {
        this.call = call;
        this.args = args;
    }

    @Override
    public String toString() {
        return "$" + call;
    }

    public BigDecimal toNumber() {
        throw new ClassCastException("MethodCallValue can't be cast to Number");
    }

    @Override
    public Value copy() {
        throw new RuntimeException("NYR");
    }

    public Value evaluate(ScriptSession session) {
        try {
            List<Value> args = (this.args == null) ? null : this.args.stream().map(e -> e.evaluate(session)).collect(Collectors.toList());
            return ScriptFunctionsImplemntatorFactory.onExec(call, args, session);

        } catch (Exception | Error e) {
            logger.error("CallMethodError[" + call + "]", e);
            throw new Error("ERROR when call function " + call + ": " + e.getMessage());
        }
    }

    @Override
    public Value execute(ScriptSession session/*, ScriptFunctionsImplemntator functionsImplemntator*/) {
        return evaluate(session/*, functionsImplemntator*/);
    }


}
