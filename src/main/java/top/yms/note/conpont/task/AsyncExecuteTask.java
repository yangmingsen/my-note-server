package top.yms.note.conpont.task;

import top.yms.note.conpont.ComponentSort;

public interface AsyncExecuteTask extends Runnable, ComponentSort, Comparable<ComponentSort>{
    void addTask(AsyncTask task);
    boolean support(AsyncTask task);
}
