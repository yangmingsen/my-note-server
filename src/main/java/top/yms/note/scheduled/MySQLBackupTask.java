package top.yms.note.scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.FileStoreService;
import top.yms.note.entity.BackupRecord;
import top.yms.note.entity.ResourceFile;
import top.yms.note.mapper.ResourceFileMapper;
import top.yms.note.repo.BackupRecordRepository;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class MySQLBackupTask {

    private final static Logger log = LoggerFactory.getLogger(MySQLBackupTask.class);

    //host port username
    private static final String host = "localhost";
    private static final String port = "3306";
    private static final String username = "root";

    @Value("${system.backup.mysqldump-path}")
    private  String mysqldumpPath ; // 改为绝对路径或配置 PATH

    @Value("${system.backup.database-password}")
    private String password;

    @Value("${system.backup.path}")
    private  String backupDir ;

    @Value("${system.backup.database-list}")
    private String backupDatabaseList;

    @Resource
    private FileStoreService fileStoreService;

    @Resource
    private BackupRecordRepository backupRecordRepository;

    @Resource
    private ResourceFileMapper resourceFileMapper;

    private List<String> getBackupDatabaseList() {
        return Arrays.asList(backupDatabaseList.split(","));
    }

   // @Scheduled(cron = "0 0 12 * * ?") // 每天12点
    public void backupEachDatabase() {
        log.info("=============开始备份数据===========");
        for (String db : getBackupDatabaseList()) {
            try {
                String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                String backupFileName = db + "_" + timestamp + ".sql";
                String backupFilePath = backupDir + backupFileName;
                log.info("backupFilePath={}", backupFilePath);
                //检查备份目录
                File dir = new File(backupDir);
                if (!dir.exists()) dir.mkdirs();
                //创建cmd
                String[] command = new String[] {
                        mysqldumpPath,
                        "-h" + host,
                        "-P" + port,
                        "-u" + username,
                        "-p" + password,
                        db
                };
                log.info("backup cmd={}", Arrays.toString(command));
                ProcessBuilder pb = new ProcessBuilder(command);
                pb.redirectOutput(new File(backupFilePath));
                Process process = pb.start();
                //
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line;
                while ((line = errorReader.readLine()) != null) {
                    log.error("STDERR: " + line);
                }
                boolean finished = process.waitFor(2, TimeUnit.MINUTES);
                if (!finished || process.exitValue() != 0) {
                    log.error("备份数据库失败：{}",db);
                    continue;
                }
                log.info("✅ 备份成功：{}", backupFilePath);
                //上传备份文件
                File file = new File(backupFilePath);
                String fileId = fileStoreService.saveFile(file);
                BackupRecord backupRecord = new BackupRecord();
                backupRecord.setFileId(fileId);
                backupRecord.setBackFrom(NoteConstants.MYSQL);
                backupRecord.setBackName(backupFileName);
                backupRecord.setBackupPath(backupFilePath);
                backupRecord.setBackDate(new Date());
                backupRecordRepository.save(backupRecord);
                log.info("🚀 上传成功：数据库 {} → 文件ID = {}" , db, fileId);
                // 可选：备份成功后删除本地文件
                 new File(backupFilePath).delete();
                //resource reg
                ResourceFile resourceFile = new ResourceFile();
                resourceFile.setFileId(fileId);
                resourceFile.setName(backupFileName);
                resourceFile.setType("sql");
                resourceFile.setSource("backup");
                resourceFile.setSize(file.length());
                resourceFile.setCreateTime(new Date());
                resourceFile.setUpdateTime(new Date());
                resourceFileMapper.insertSelective(resourceFile);
            } catch (Exception e) {
                log.error("❌ 数据库备份异常: "+ db, e);
            }
        }
        log.info("=============结束备份数据===========");
    }


}
