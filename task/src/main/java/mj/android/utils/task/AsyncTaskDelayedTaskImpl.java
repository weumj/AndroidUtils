package mj.android.utils.task;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.Executor;

class AsyncTaskDelayedTaskImpl<T> implements DelayedTask<T> {
    final InternalAsyncTask<T> task;

    AsyncTaskDelayedTaskImpl(Callable2<T> c) {
        task = new InternalAsyncTask<T>().callable(c);
    }

    @Override
    public boolean cancel() {
        Tasks.sendCancelToCancelable(task.callable);
        return task.cancel(true);
    }

    @Override
    public DelayedTask<T> result(ResultListener<T> r) {
        task.result(r);
        return this;
    }

    @Override
    public DelayedTask<T> error(ErrorListener e) {
        task.error(e);
        return this;
    }

    @Override
    public DelayedTask<T> atLast(@Nullable Runnable r) {
        task.postRunnable(r);
        return this;
    }

    @Override
    public DelayedTask<T> clone() {
        return new AsyncTaskDelayedTaskImpl<>(task.callable);
    }

    @Override
    public void execute() {
        task.executeOnExecutor(Tasks.EXECUTOR);
    }

    @Override
    public void execute(@NonNull Executor executor) {
        task.executeOnExecutor(executor);
    }
}