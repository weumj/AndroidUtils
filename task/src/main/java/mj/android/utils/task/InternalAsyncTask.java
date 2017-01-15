package mj.android.utils.task;


import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.CancellationException;

class InternalAsyncTask<T> extends AsyncTask<Void, Integer, T> {

    private static final String TAG = "InternalAsyncTask";

    Callable2<T> callable;
    private Throwable mOccurredException;
    private ResultListener<T> resultListener;
    private ErrorListener errorListener;
    private Runnable postRunnable;

    InternalAsyncTask() {
    }

    InternalAsyncTask(@NonNull Callable2<T> callable, @Nullable ResultListener<T> resultListener) {
        callable(callable).result(resultListener);
    }

    InternalAsyncTask<T> callable(Callable2<T> callable) {
        this.callable = callable;
        return this;
    }

    InternalAsyncTask<T> result(ResultListener<T> resultListener) {
        this.resultListener = resultListener;
        return this;
    }

    InternalAsyncTask<T> error(@Nullable ErrorListener errorListener) {
        this.errorListener = errorListener;
        return this;
    }

    InternalAsyncTask<T> postRunnable(@Nullable Runnable runnable) {
        this.postRunnable = runnable;
        return this;
    }

    @Override
    protected T doInBackground(Void... params) {
        if (callable == null || isCancelled()) {
            callable = null;
            return null;
        }
        try {
            T t = callable.call();
            return isCancelled() ? null : t;
        } catch (Throwable e) {
            setException(e);
            return null;
        } finally {
            callable = null;
        }
    }

    @Override
    protected void onPostExecute(T result) {
        try {
            if (isCancelled())
                return;

            if (isExceptionOccurred())
                notifyException();
            else
                notifyResult(result);
        } finally {
            if(postRunnable != null){
                postRunnable.run();
                postRunnable = null;
            }
            errorListener = null;
        }
    }

    @Override
    protected void onCancelled(T result) {
        setException(new CancellationException("Canceled."));
    }

    protected void setException(Throwable e) {
        mOccurredException = e;
    }

    private boolean isExceptionOccurred() {
        return this.mOccurredException != null;
    }

    private void notifyException() {
        if (errorListener != null) {
            errorListener.onError(mOccurredException);
            mOccurredException = null;
        }
    }

    private void notifyResult(T result) {
        if (resultListener != null)
            resultListener.onResult(result);
        else
            Log.w(TAG, "result listener was empty. result : " + result);
    }

}
