package com.zip.zipUtils.functional_interfaces;

@FunctionalInterface
public interface UseInstance<T, X extends Throwable> {
    void accept(T instance) throws X;
}
