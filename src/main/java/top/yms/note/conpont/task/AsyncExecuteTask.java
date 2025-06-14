package top.yms.note.conpont.task;

import top.yms.note.conpont.ComponentComparable;


public interface AsyncExecuteTask extends NoteTask, ComponentComparable {

    void addTask(AsyncTask task);

    boolean support(AsyncTask task);
}
