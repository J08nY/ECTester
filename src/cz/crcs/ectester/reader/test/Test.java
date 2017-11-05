package cz.crcs.ectester.reader.test;

import cz.crcs.ectester.reader.command.Command;
import cz.crcs.ectester.reader.response.Response;

import javax.smartcardio.CardException;
import java.util.function.BiFunction;
import java.util.function.Function;

import static cz.crcs.ectester.reader.test.Result.ExpectedValue;
import static cz.crcs.ectester.reader.test.Result.Value;

/**
 * An abstract test that can be run and has a Result.
 *
 * @author Jan Jancar johny@neuromancer.sk
 */
public abstract class Test {
    boolean hasRun = false;
    Result result;

    public Result getResult() {
        if (!hasRun) {
            return null;
        }
        return result;
    }

    public Value getResultValue() {
        if (!hasRun) {
            return null;
        }
        return result.getValue();
    }

    public String getResultCause() {
        if (!hasRun) {
            return null;
        }
        return result.getCause();
    }

    public boolean ok() {
        if (!hasRun) {
            return true;
        }
        return result.ok();
    }

    public abstract String getDescription();

    public boolean hasRun() {
        return hasRun;
    }

    public abstract void run() throws CardException;

    /**
     * A simple test that runs one Command to get and evaluate one Response
     * to get a Result and compare it with the expected one.
     */
    public static class Simple extends Test {
        private BiFunction<Command, Response, Result> callback;
        private Command command;
        private Response response;

        public Simple(Command command, BiFunction<Command, Response, Result> callback) {
            this.command = command;
            this.callback = callback;
        }

        public Simple(Command command, ExpectedValue expected, String ok, String nok) {
            this(command, (cmd, resp) -> {
                Value resultValue = Value.fromExpected(expected, resp.successful());
                return new Result(resultValue, resultValue.ok() ? ok : nok);
            });
        }

        public Simple(Command command, ExpectedValue expected) {
            this(command, expected, null, null);
        }

        public Command getCommand() {
            return command;
        }

        public Response getResponse() {
            return response;
        }

        @Override
        public void run() throws CardException {
            if (hasRun)
                return;

            response = command.send();
            if (callback != null) {
                result = callback.apply(command, response);
            } else {
                if (response.successful()) {
                    result = new Result(Value.SUCCESS);
                } else {
                    result = new Result(Value.FAILURE);
                }
            }
            hasRun = true;
        }

        @Override
        public String getDescription() {
            return response.getDescription();
        }
    }

    /**
     * A compound test that runs many Tests and has a Result dependent on all/some of their Results.
     */
    public static class Compound extends Test {
        private Function<Test[], Result> callback;
        private Test[] tests;
        private String description;

        private Compound(Function<Test[], Result> callback, Test... tests) {
            this.callback = callback;
            this.tests = tests;
        }

        private Compound(Function<Test[], Result> callback, String descripiton, Test... tests) {
            this(callback, tests);
            this.description = descripiton;
        }

        public static Compound function(Function<Test[], Result> callback, Test... tests) {
            return new Compound(callback, tests);
        }

        public static Compound function(Function<Test[], Result> callback, String description, Test... tests) {
            return new Compound(callback, description, tests);
        }

        public static Compound all(ExpectedValue what, Test... all) {
            return new Compound((tests) -> {
                for (Test test : tests) {
                    if (!Value.fromExpected(what, test.ok()).ok()) {
                        return new Result(Value.FAILURE, "At least one of the sub-tests did not have the expected result.");
                    }
                }
                return new Result(Value.SUCCESS, "All sub-tests had the expected result.");
            }, all);
        }

        public static Compound all(ExpectedValue what, String description, Test... all) {
            Compound result = Compound.all(what, all);
            result.setDescription(description);
            return result;
        }

        public static Compound any(ExpectedValue what, Test... any) {
            return new Compound((tests) -> {
                for (Test test : tests) {
                    if (Value.fromExpected(what, test.ok()).ok()) {
                        return new Result(Value.SUCCESS, "At least one of the sub-tests did have the expected result.");
                    }
                }
                return new Result(Value.FAILURE, "None of the sub-tests had the expected result.");
            }, any);
        }

        public static Compound any(ExpectedValue what, String description, Test... any) {
            Compound result = Compound.any(what, any);
            result.setDescription(description);
            return result;
        }

        public static Compound mask(ExpectedValue[] results, Test... masked) {
            return new Compound((tests) -> {
                for (int i = 0; i < results.length; ++i) {
                    if (!Value.fromExpected(results[i], tests[i].ok()).ok()) {
                        return new Result(Value.FAILURE, "At least one of the sub-tests did not match the result mask.");
                    }
                }
                return new Result(Value.SUCCESS, "All sub-tests matched the expected mask.");
            }, masked);
        }

        public static Compound mask(ExpectedValue[] results, String description, Test... masked) {
            Compound result = Compound.mask(results, masked);
            result.setDescription(description);
            return result;
        }

        public Test[] getTests() {
            return tests;
        }

        @Override
        public void run() throws CardException {
            if (hasRun)
                return;

            for (Test test : tests) {
                test.run();
            }
            result = callback.apply(tests);
            this.hasRun = true;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public String getDescription() {
            return description;
        }
    }
}
