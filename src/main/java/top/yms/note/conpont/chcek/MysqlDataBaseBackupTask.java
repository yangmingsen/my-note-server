package top.yms.note.conpont.chcek;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.yms.note.entity.CheckTarget;
import top.yms.note.scheduled.MySQLBackupTask;
import top.yms.note.scheduled.MySQLBackupWithBatScript;

import javax.annotation.Resource;

@Component
public class MysqlDataBaseBackupTask extends AbstractCheckTargetTask{

    private final static Logger log = LoggerFactory.getLogger(MysqlDataBaseBackupTask.class);

    @Resource
    private MySQLBackupTask mySQLBackupTask;

    @Resource
    private MySQLBackupWithBatScript mySQLBackupWithBatScript;

    public boolean support(String name) {
        return name.equals("database-backup");
    }

    public boolean needTx() {
        return true;
    }

    @Override
    void doCheckTask(CheckTarget checkTarget) throws Exception{
        Thread thread = new Thread(
                () -> {mySQLBackupTask.backupEachDatabase();}
        );
        thread.start();
    }

    public int getSortValue() {
        return 10;
    }
}
