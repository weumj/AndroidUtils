package mj.android.utils.task;

public class BaseTask<T> implements Task<T>, Cloneable {

    private Callable2<T> callable2;

    public BaseTask(Callable2<T> callable2) {
        this.callable2 = callable2;
    }

    @Override
    public T get() throws Throwable {
        try {
            return callable2.call();
        } finally {
            callable2 = null;
        }
    }

    @Override
    public DelayedTask<T> delayed() {
        return new CallableDelayedTaskImpl<>(callable2);
    }

    @Override
    public <V> Task<V> map(Func<T, V> func) {
        return new BaseTask<>(new Tasks.ConvertCallable<>(callable2, func));
    }

    @Override
    public Task<T> clone() {
        BaseTask<T> task;
        try {
            //noinspection unchecked
            task = (BaseTask<T>) super.clone();
            task.callable2 = this.callable2;
        } catch (Exception e) {
            e.printStackTrace();
            task = new BaseTask<>(callable2);
        }
        return task;
    }
}
