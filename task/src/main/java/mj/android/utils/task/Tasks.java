package mj.android.utils.task;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Tasks {
    static final Executor EXECUTOR = AsyncTask.THREAD_POOL_EXECUTOR;
    public static final Executor UI_THREAD_EXECUTOR = new Executor() {
        final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            handler.post(command);
        }
    };

    private Tasks() {
    }

    public static boolean cancelTask(DelayedTask task) {
        return task != null && task.cancel();
    }

    public static void sendCancelToCancelable(Object o) {
        if (o != null && o instanceof Cancelable) {
            ((Cancelable) o).cancel();
        }
    }

    /**
     * 주어진 작업을 실행한다.
     */
    public static void execute(Runnable r) {
        EXECUTOR.execute(r);
    }


    /**
     * 주어진 작업을 실행하는 Task 를 생성한다.
     */
    public static <T> Task<T> newTask(Callable2<T> c) {
        return new BaseTask<>(c);
    }

    /**
     * Task 로 부터 FutureTask 를 생성하여,  Task 를 병렬로 실행 할 수 있게한다.
     */
    private static <T> FutureTask<T> futureTask(final Task<T> task) {
        return new FutureTask<>(new Callable<T>() {
            @Override
            public T call() throws Exception {
                try {
                    return task.get();
                } catch (Throwable throwable) {
                    if (throwable instanceof Exception)
                        throw (Exception) throwable;
                    else
                        throw new Exception(throwable);
                }
            }
        });
    }


    public static final class Parallel {

        private Parallel() {
        }

        /**
         * 순차적으로 주어진 Tasks 를 실행하는 Task 를 반환한다.
         */
        public static Task<Object[]> serialTask(final Task... tasks) {
            return new SerialTaskImpl(tasks);
        }

        /**
         * 순차적으로 주어진 Tasks 를 실행하는 Task 를 반환한다.
         */
        public static Task<Object[]> serialTask(final Collection<Task> collection) {
            return serialTask(collection.toArray(new Task[collection.size()]));
        }


        /**
         * 순차적으로 주어진 Tasks 를 실행하는 Task 를 반환한다.
         * <p>
         * Task 의 Type 은 같아야 한다.
         * <p>
         * 결과로 얻어지는 List 에는 null 이 포함될 수 있다.
         */
        public static <T, Q extends Task<T>> Task<List<T>> serialTaskTyped(final Collection<Q> collection) {
            return new SerialTaskImpl(collection.toArray(new Task[collection.size()])).map(Parallel.<T>resultsTypeMapper());
        }


        /**
         * 순차적으로 주어진 Tasks 를 실행하는 Task 를 반환한다.
         * <p>
         * Task 의 Type 은 같아야 한다.
         * <p>
         * 결과로 얻어지는 List 에는 null 이 포함될 수 있다.
         */
        public static <T, Q extends Collection<T>, R extends Task<Q>> Task<List<T>> serialTaskTypedCollection(final Collection<R> collection) {
            return new SerialTaskImpl(collection.toArray(new Task[collection.size()])).map(Parallel.<T>resultsTypeMapper());
        }

        /**
         * 순차적으로 주어진 Tasks 를 실행한 뒤, 결과를 변환하는 Task 를 반환한다.
         */
        public static <T> Task<T> serialTask(final Task[] tasks, final Func<Object[], T> func) {
            return serialTask(tasks).map(func);
        }

        /**
         * 순차적으로 주어진 Tasks 를 실행한 뒤, 결과를 변환하는 Task 를 반환한다.
         */
        public static <T> Task<T> serialTask(final Collection<Task> collection, final Func<Object[], T> func) {
            return serialTask(collection.toArray(new Task[collection.size()]), func);
        }

        /**
         * 병렬적으로 주어진 Tasks 를 실행하는 Task 를 반환한다.
         * <p>
         * Task 에서 에러가 발생했을 경우엔, 결과 Object 배열 및 ErrorListener 모두에 에러가 전달된다.
         */
        public static Task<Object[]> parallelTask(final Task... tasks) {
            return new ParallelTaskImpl(tasks);
        }

        /**
         * 병렬적으로 주어진 Tasks 를 실행하는 Task 를 반환한다.
         */
        public static Task<Object[]> parallelTask(final Collection<Task> collection) {
            return parallelTask(collection.toArray(new Task[collection.size()]));
        }

        /**
         * 병렬적으로 주어진 Tasks 를 실행하는 Task 를 반환한다.
         * <p>
         * Task 의 Type 은 같아야 한다.
         * <p>
         * 결과로 얻어지는 List 에는 null 이 포함될 수 있다.
         */
        public static <T, Q extends Task<T>> Task<List<T>> parallelTaskTyped(final Collection<Q> collection) {
            return parallelTask(collection.toArray(new Task[collection.size()])).map(Parallel.<T>resultsTypeMapper());
        }

        /**
         * 병렬적으로 주어진 Tasks 를 실행하는 Task 를 반환한다.
         * <p>
         * Task 의 Type 은 같아야 한다.
         * <p>
         * 결과로 얻어지는 List 에는 null 이 포함될 수 있다.
         */
        public static <T, Q extends Collection<T>, R extends Task<Q>> Task<List<T>> parallelTaskTypedCollection(final Collection<R> collection) {
            return parallelTask(collection.toArray(new Task[collection.size()])).map(Parallel.<T>resultsTypeMapper());
        }

        public static <T, Q extends Collection<T>, R extends Task<Q>> Task<List<T>> parallelTaskTypedCollection(final Collection<R> collection, int threadCount) {
            if (threadCount < 2) {
                return serialTaskTypedCollection(collection);
            }
            final int N = collection.size();
            if (N < threadCount) {
                return parallelTaskTypedCollection(collection);
            } else {
                ArrayList<R> list = new ArrayList<>();
                list.addAll(collection);

                ArrayList<Task<List<T>>> list1 = new ArrayList<>();
                int div = N / threadCount;
                for (int i = 0; i < threadCount; i++) {
                    int start = i * div, end = (i + 1) * div;
                    end = N > end ? end : N;
                    Task<List<T>> t = subTask(list.subList(start, end));
                    list1.add(t);
                }
                return parallelTaskTypedCollection(list1);
            }


        }

        private static <T> Func<Object[], List<T>> resultsTypeMapper() {
            return new Func<Object[], List<T>>() {
                @Override
                public List<T> func(Object[] objects) throws Throwable {
                    ArrayList<T> list = new ArrayList<>();
                    for (Object o : objects) {
                        //noinspection unchecked
                        list.addAll((List<T>) o);
                    }
                    return list;
                }
            };
        }

        private static <T, Q extends Collection<T>, R extends Task<Q>> Task<List<T>> subTask(final Collection<R> collection) {
            return newTask(new Callable2<List<T>>() {
                @Override
                public List<T> call() throws Throwable {
                    List<T> results = new ArrayList<>();

                    for (Task<Q> request : collection)
                        results.addAll(request.get());

                    return results;
                }
            });
        }

        /**
         * 병렬적으로 Task 를 실행한 뒤, 결과를 변환하는 Task 를 반환한다.
         */
        public static <T> Task<T> parallelTask(final Task[] tasks, final Func<Object[], T> func) {
            return new ParallelTaskImpl(tasks).map(func);
        }

        /**
         * 병렬적으로 Task 를 실행한 뒤, 결과를 변환하는 Task 를 반환한다.
         */
        public static <T> Task<T> parallelTask(final Collection<Task> tasks, final Func<Object[], T> func) {
            return parallelTask(tasks.toArray(new Task[tasks.size()]), func);
        }

        /**
         * 순차적으로 Task 를 실행한다.
         */
        @WorkerThread
        public static Object[] workSerial(Collection<Task> collection) throws Throwable {
            return workSerial(collection.toArray(new Task[collection.size()]));
        }

        /**
         * 순차적으로 Task 를 실행한다.
         */
        @WorkerThread
        public static Object[] workSerial(Task... tasks) throws Throwable {
            Object[] objects = new Object[tasks.length];
            for (int i = 0; i < tasks.length; i++) {
                objects[i] = tasks[i].get();
            }

            return objects;
        }

        /**
         * 병렬적으로 Task 를 실행한다.
         */
        @WorkerThread
        public static Object[] workParallel(Collection<Task> collection) {
            return workParallel(collection.toArray(new Task[collection.size()]));
        }

        /**
         * 병렬적으로 Task 를 실행한다.
         */
        @WorkerThread
        public static Object[] workParallel(Task... tasks) {
            FutureTask[] futureTasks = new FutureTask[tasks.length];
            Object[] objects = new Object[tasks.length];

            for (int i = 0; i < tasks.length; i++) {
                FutureTask futureTask = futureTask(tasks[i]);
                futureTasks[i] = futureTask;
                execute(futureTask);
            }

            for (int i = 0; i < futureTasks.length; i++) {
                try {
                    objects[i] = futureTasks[i].get();
                } catch (Exception e) {
                    objects[i] = e;
                }
            }

            return objects;
        }

        private static class SerialTaskImpl extends BaseTask<Object[]> {
            private Task[] tasks;

            SerialTaskImpl(final Task[] tasks) {
                super(new SerialCallable(tasks));
                this.tasks = tasks;
            }


            @Override
            public Task<Object[]> clone() {
                Task[] tasks1 = new Task[tasks.length];
                for (int i = 0; i < tasks.length; i++) {
                    tasks1[i] = tasks[i].clone();
                }
                return new SerialTaskImpl(tasks1);
            }

            private static class SerialCallable implements Callable2<Object[]>, Cancelable {
                private Task[] tasks;
                private boolean isCanceled = false;

                SerialCallable(final Task[] tasks) {
                    this.tasks = tasks;
                }

                @Override
                public Object[] call() throws Throwable {
                    Object[] objects = new Object[tasks.length];
                    for (int i = 0; i < tasks.length; i++) {
                        if (isCanceled) {
                            return null;
                        }
                        objects[i] = tasks[i].get();
                    }

                    return objects;
                }

                @Override
                public boolean cancel() {
                    this.isCanceled = true;
                    return true;
                }
            }
        }

        //todo cancel
        private static class ParallelTaskImpl extends BaseTask<Object[]> {
            private Task[] tasks;

            ParallelTaskImpl(final Task[] tasks) {
                super(new ParallelCallable(tasks));
                this.tasks = tasks;
            }

            private static class ParallelCallable implements Callable2<Object[]>, Cancelable {
                private Task[] tasks;
                private boolean isCanceled = false;

                ParallelCallable(final Task[] tasks) {
                    this.tasks = tasks;
                }

                @Override
                public Object[] call() throws Throwable {
                    final int N = tasks.length;
                    FutureTask[] futureTasks = new FutureTask[N];
                    Object[] objects = new Object[N];

                    for (int i = 0; i < N; i++) {
                        FutureTask futureTask = futureTask(tasks[i]);
                        futureTasks[i] = futureTask;
                        execute(futureTask);
                    }

                    for (int i = 0; i < N; i++) {
                        if (isCanceled) {
                            for (FutureTask futureTask : futureTasks) {
                                try {
                                    futureTask.cancel(true);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            return null;
                        }
                        try {
                            objects[i] = futureTasks[i].get();
                        } catch (Exception e) {
                            objects[i] = e;
                        }
                    }

                    return objects;
                }

                @Override
                public boolean cancel() {
                    this.isCanceled = true;
                    return true;
                }
            }

            @Override
            public Task<Object[]> clone() {
                Task[] tasks1 = new Task[tasks.length];
                for (int i = 0; i < tasks.length; i++) {
                    tasks1[i] = tasks[i].clone();
                }
                return new ParallelTaskImpl(tasks1);
            }
        }
    }

    static class ConvertCallable<T, V> implements Callable2<V>, Cancelable {
        private Callable2<T> call;
        private Func<T, V> func;
        private AtomicBoolean isCancelled = new AtomicBoolean(false);

        ConvertCallable(Callable2<T> callable, Func<T, V> func) {
            this.call = callable;
            this.func = func;
        }

        @Override
        public V call() throws Throwable {
            try {
                if (isCancelled.get()) {
                    sendCancelToCancelable(call);
                    return null;
                }

                T t = call.call();

                if (isCancelled.get()) {
                    sendCancelToCancelable(func);
                    return null;
                }

                V v = func.func(t);

                if (isCancelled.get()) {
                    return null;
                }

                return v;
            } finally {
                call = null;
                func = null;
            }
        }

        @Override
        public boolean cancel() {
            isCancelled.set(true);
            Callable2 callable = call;
            while (callable instanceof Tasks.ConvertCallable) {
                ConvertCallable convertCallable = (ConvertCallable) callable;
                convertCallable.cancel();
                callable = convertCallable.call;
            }
            return true;
        }
    }

}
