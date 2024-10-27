package exceptions;

public record Success<T>(T result) implements Try<T> {
    @Override
    public T getResult() {
        return result;
    }

    @Override
    public Throwable getError() {
        return new RuntimeException("Invalid invocation");
    }
}
