package top.yms.note.conpont.chcek;

import org.springframework.stereotype.Component;
import top.yms.note.entity.CheckTarget;
import top.yms.note.scheduled.MySQLBackupTask;

import javax.annotation.Resource;

@Component
public class MysqlDataBaseBackupTask extends AbstractCheckTargetTask{

    @Resource
    private MySQLBackupTask mySQLBackupTask;

    public boolean support(String name) {
        return name.equals("database-backup");
    }

    public boolean needTx() {
        return true;
    }

    @Override
    void doCheckTask(CheckTarget checkTarget) {
        mySQLBackupTask.backupEachDatabase();
    }

    public int getSortValue() {
        return 10;
    }
}
