package mj.android.utils.task;

public interface Func<T, V> {
    V func(T t) throws Throwable;
}
