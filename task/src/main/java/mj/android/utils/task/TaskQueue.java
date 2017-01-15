package mj.android.utils.task;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TaskQueue {
    private final Map<String, DelayedTask> TASK_MAP;

    public TaskQueue() {
        TASK_MAP = new ConcurrentHashMap<>();
    }

    public TaskQueue(Map<String, DelayedTask> taskMap) {
        TASK_MAP = new ConcurrentHashMap<>(taskMap);
    }

    public boolean exist(String tag) {
        return TASK_MAP.containsKey(tag);
    }

    public <T> DelayedTask<T> enqueue(final String tag, final Task<T> task) {
        Task<T> task1 = task.map(new Func<T, T>() {

            @Override
            public T func(T t) throws Throwable {
                TaskQueue.this.removeTask(tag);
                return t;

            }
        });

        DelayedTask<T> delayedTask = task1.delayed();

        addTask(tag, delayedTask);

        return delayedTask;
    }

    protected void addTask(String tag, DelayedTask<?> task) {
        if (tag != null)
            TASK_MAP.put(tag, task);
    }

    protected void removeTask(String tag) {
        if (tag != null)
            TASK_MAP.remove(tag);
    }

    public void cancelAll() {
        for (DelayedTask<?> task : TASK_MAP.values()) {
            task.cancel();
        }
        TASK_MAP.clear();
    }

    public void cancel(String tag) {
        if (TASK_MAP.containsKey(tag))
            TASK_MAP.remove(tag).cancel();
    }


    public void cancel(TaskFilter filter) {
        for (String tag : TASK_MAP.keySet()) {
            DelayedTask<?> task = TASK_MAP.get(tag);
            if (filter.match(task, tag)) {
                task.cancel();
                TASK_MAP.remove(tag);
            }
        }
    }


    public interface TaskFilter {
        boolean match(DelayedTask<?> task, String tag);
    }
}
