package ru.maratislamov.script.values;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.maratislamov.script.ScriptFunctionsService;
import ru.maratislamov.script.ScriptSession;
import ru.maratislamov.script.expressions.Expression;
import ru.maratislamov.script.statements.Statement;


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

    public List<Expression> getArgs() {
        return args;
    }

    public Double toNumber() {
        throw new ClassCastException("MethodCallValue can't be cast to Number");
    }

    @Override
    public Value copy() {
        throw new RuntimeException("NYR");
    }

    public Value evaluate(ScriptSession session) {
        try {
            List<Value> args = (this.args == null) ? null : this.args.stream().map(e -> Expression.evaluate(e , session)).collect(Collectors.toList());
            return ScriptFunctionsService.execFunction(call, args, session);

        } catch (Exception | Error e) {
            logger.error("CallMethodError[" + call + "]");
            throw new Error("ERROR when call function " + call + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Value execute(ScriptSession session) {
        return evaluate(session);
    }

    @Override
    public String toString() {
        return "{CALL " + call + " " + args + "}";
    }

}
