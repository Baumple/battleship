import java.util.function.Consumer;
import java.util.function.Function;

public sealed interface Result<T, R> permits Result.Ok, Result.Error {
    record Ok<T, R>(T value) implements Result<T, R> {
        /**
         * Does some type magic so that the compiler does
         * not complain when the Error generic differs from
         * the expected one but the Ok generic is valid.
         */
        public Result.Ok<T, R> convert() {
            return Result.ok(this.value());
        }
    }

    record Error<T, R>(R error) implements Result<T, R> {
        /**
         * Does some type magic so that the compiler does
         * not complain when the Ok generic differs from
         * the expected one but the Error generic is valid.
         */
        public <S> Result.Error<S, R> convert() {
            return Result.error(this.error());
        }
    }

    static <T, R> Ok<T, R> ok(T value) {
        return new Ok<>(value);
    }

    static <T, R> Error<T, R> error(R error) {
        return new Error<>(error);
    }

    @FunctionalInterface
    public interface Block<T> {
        public T exec() throws Exception;
    }

    default boolean isError() {
        return this instanceof Result.Error<T, R>;
    }

    default boolean isOk() {
        return this instanceof Result.Ok<T, R>;
    }

    default <S> Result<S, R> mapOk(Function<T, S> f) {
        switch (this) {
            case Result.Error<T, R> e:
                return e.convert();
            case Result.Ok<T, R> o:
                return Result.ok(f.apply(o.value()));
        }
    }

    static <T, R> Result<T, R> voidOk() {
        return ok(null);
    }

    /**
     * Takes a function which returns a value or throws an Exception.
     *
     * If an exception is thrown, it is caught and wrapped into an Result.Error.
     *
     * Otherwise returns Result.Ok containing the Ok value
     *
     * @param b The function to be exectuted
     **/
    static <T> Result<T, Exception> runCatching(Block<T> b) {
        try {
            return Result.ok(b.exec());
        } catch (Exception e) {
            return Result.error(e);
        }
    }

    /**
     * Takes a function which returns a value or throws an Exception.
     *
     * If an exception is thrown, it is caught and wrapped by the given map
     * function and returns as Result.Error
     *
     * @param b   The function to be exectuted
     * @param map The wrapping function
     **/
    static <T, R> Result<T, R> runErrorMapping(Block<T> b, Function<Exception, R> map) {
        try {
            return Result.ok(b.exec());
        } catch (Exception e) {
            return Result.error(map.apply(e));
        }
    }

    /**
     * Takes a function which takes the error value of the Result.Error record as a
     * parameter and executes it, if the Result is an Error.
     **/
    default void ifError(Consumer<R> c) {
        if (this instanceof Result.Error<T, R> e) {
            c.accept(e.error());
        }
    }
}
