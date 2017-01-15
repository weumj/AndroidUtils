package mj.android.utils.task;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.Executor;

public interface DelayedTask<T> extends Cancelable {
    DelayedTask<T> result(@Nullable ResultListener<T> r);

    DelayedTask<T> error(@Nullable ErrorListener e);

    DelayedTask<T> atLast(@Nullable Runnable r);

    DelayedTask<T> clone();

    void execute();

    void execute(@NonNull Executor executor);

}
