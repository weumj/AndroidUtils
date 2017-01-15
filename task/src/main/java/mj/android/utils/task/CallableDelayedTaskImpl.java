package mj.android.utils.task;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

class CallableDelayedTaskImpl<T> implements DelayedTask<T> {
    private AtomicBoolean isCancelled = new AtomicBoolean(false);
    private Callable2<T> call;
    private ResultListener<T> resultListener;
    private ErrorListener errorListener;
    private Runnable atLastListener;

    CallableDelayedTaskImpl(Callable2<T> callable) {
        this.call = callable;
    }

    private boolean isCancelled() {
        return isCancelled.get();
    }

    @Override
    public boolean cancel() {
        Tasks.sendCancelToCancelable(call);
        return isCancelled.compareAndSet(false, true);
    }

    @Override
    public DelayedTask<T> result(@Nullable ResultListener<T> r) {
        this.resultListener = r;
        return this;
    }

    @Override
    public DelayedTask<T> error(@Nullable ErrorListener e) {
        this.errorListener = e;
        return this;
    }

    @Override
    public DelayedTask<T> atLast(@Nullable Runnable r) {
        this.atLastListener = r;
        return this;
    }

    @Override
    public void execute() {
        Tasks.execute(running(resultListener, errorListener, atLastListener));
    }

    @Override
    public void execute(@NonNull Executor executor) {
        executor.execute(running(resultListener, errorListener, atLastListener));
    }

    @Override
    public DelayedTask<T> clone() {
        return new CallableDelayedTaskImpl<>(call);
    }


    private Runnable running(final ResultListener<T> r, final ErrorListener e, final Runnable atLast) {
        return new Runnable() {
            @Override
            public void run() {
                if (isCancelled())
                    return;
                try {
                    final T t = call.call();

                    if (isCancelled())
                        return;

                    notifyResult(t, r);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();

                    if (isCancelled())
                        return;

                    notifyError(throwable, e);
                } finally {
                    notifyAtLast(atLast);
                }

            }
        };
    }

    private static <T> void notifyResult(final T t, final ResultListener<T> r) {
        if (r == null) {
            Log.w("CallableDelayedTask", "ResultListener == null");
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (r != null) r.onResult(t);
            }
        });
    }

    private static void notifyError(final Throwable t, final ErrorListener e) {
        if (e == null) {
            Log.w("CallableDelayedTask", "ErrorListener == null");
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (e != null) e.onError(t);
            }
        });
    }

    private static void notifyAtLast(final Runnable r) {
        if (r == null) {
            Log.w("CallableDelayedTask", "AtLastRunner == null");
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (r != null) r.run();
            }
        });
    }

    private static void runOnUiThread(Runnable r) {
        Tasks.UI_THREAD_EXECUTOR.execute(r);
    }

}
