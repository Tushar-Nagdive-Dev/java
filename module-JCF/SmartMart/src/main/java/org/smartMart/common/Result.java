package org.smartMart.common;

public sealed interface Result<T> {
    record Ok<T>(T value) implements Result<T> {}

    record Err<T>(String message) implements Result<T> {}

    static <T> Ok<T> ok(T value) { return new Ok<>(value); }

    static <T> Err<T> err(String message) { return new Err<>(message); }
}
