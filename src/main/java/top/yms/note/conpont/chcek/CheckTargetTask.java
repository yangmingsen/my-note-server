package top.yms.note.conpont.chcek;

import top.yms.note.conpont.ComponentComparable;
import top.yms.note.entity.CheckTarget;

public interface CheckTargetTask extends ComponentComparable {

    boolean support(String name);

    void excTask(CheckTarget checkTarget);
}
