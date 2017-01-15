package mj.android.utils.task;


public interface Task<T> {
    T get() throws Throwable;

    DelayedTask<T> delayed();

    <V> Task<V> map(Func<T, V> func);

    //boolean cancel();

    Task<T> clone();
}
