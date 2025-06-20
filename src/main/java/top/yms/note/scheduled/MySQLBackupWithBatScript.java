package top.yms.note.scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

@Component
public class MySQLBackupWithBatScript {

    private final static Logger log = LoggerFactory.getLogger(MySQLBackupWithBatScript.class);

    @Value("${system.backup.path}")
    private String backupPath;

    @Value("${system.backup.script-path}")
    private String batScriptPath;

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


    private String getBackupPath() {
        return backupPath;
    }

    private String getBatScriptPath() {
        return batScriptPath;
    }


    public void backup() throws Exception {
        log.info("=============开始备份数据===========");
        String backupPath = getBackupPath();
        File dir = new File(backupPath);
        if (!dir.exists()) {
            log.error("backup 目录不存在");
            dir.mkdirs();
        }
        for (String dbName : getBackupDatabaseList()) {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = dbName + timestamp + ".sql";
            String fullPath = backupPath + fileName;
            // 脚本路径
            String batPath = getBatScriptPath()+File.separator+dbName+".bat";
            // 执行脚本并传参
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", batPath, fullPath);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            // 打印控制台输出（可选）
            log.info("read back msg");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("[输出] {}" , line);
                }
            }
            log.info("wait.......");
            int exitCode = process.waitFor();
            log.info("备份完成，状态码: " + exitCode);
            log.info("备份文件路径：" + fullPath);
            //上传备份文件
            File file = new File(fullPath);
            String fileId = fileStoreService.saveFile(file);
            BackupRecord backupRecord = new BackupRecord();
            backupRecord.setFileId(fileId);
            backupRecord.setBackFrom(NoteConstants.MYSQL);
            backupRecord.setBackName(fileName);
            backupRecord.setBackupPath(fullPath);
            backupRecord.setBackDate(new Date());
            backupRecordRepository.save(backupRecord);
            log.info("🚀 上传成功：数据库 {} → 文件ID = {}" , dbName, fileId);
            // 可选：备份成功后删除本地文件
//            new File(fullPath).delete();
            //resource reg
            ResourceFile resourceFile = new ResourceFile();
            resourceFile.setFileId(fileId);
            resourceFile.setName(fileName);
            resourceFile.setType("sql");
            resourceFile.setSource("backup");
            resourceFile.setSize(file.length());
            resourceFile.setCreateTime(new Date());
            resourceFile.setUpdateTime(new Date());
            resourceFileMapper.insertSelective(resourceFile);
        }
        log.info("=============结束备份数据===========");
    }
}
