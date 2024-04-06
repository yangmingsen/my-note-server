package top.yms.note.utils;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.web.multipart.MultipartFile;
import top.yms.note.config.SpringContext;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yangmingsen on 2024/4/6.
 */
public class MongoDB {
    /**
     * Log4j logger
     */
    private final static Logger lg = LoggerFactory.getLogger(MongoDB.class);

    // =================================================================
    //		Document
    // =================================================================
    /**
     * 日期格式
     */
    public final static String PATTERN_DATE = "yyyy-MM-dd";




    /**
     * 新增Document
     *
     * @param doc                            文档内容
     * @param collectionName    集合名称
     */
    public static String saveDocument(String doc, String collectionName) throws Exception {
        lg.info("新增Document：{}", collectionName);

        BasicDBObject obj = new BasicDBObject();
        obj.put("doc", doc);

        MongoTemplate mgt = (MongoTemplate) SpringContext.getBean("documentTemplate");
        mgt.save(obj, collectionName);

        return obj.getObjectId("_id").toString();
    }

    /**
     * 更新Document
     *
     * @param id              文档ID
     * @param doc             文档内容
     * @param collectionName  集合名称
     */
    public static void updateDocument(String id, String doc, String collectionName) throws Exception {
        lg.info("修改Document：{}[{}]", id, collectionName);

        MongoTemplate mgt = (MongoTemplate) SpringContext.getBean("documentTemplate");
        mgt.updateFirst(new Query(Criteria.where("_id").is(id)), Update.update("doc", doc), collectionName);
    }

    /**
     * 加载Document
     *
     * @param id              文档ID
     * @param collectionName  集合名称
     */
    public static String loadDocument(String id, String collectionName) throws Exception {
        lg.info("加载Document：{}[{}]", id, collectionName);
        MongoTemplate mgt = (MongoTemplate) SpringContext.getBean("documentTemplate");
        BasicDBObject obj = mgt.findOne(new Query(Criteria.where("_id").is(id)), BasicDBObject.class, collectionName);

        if (null == obj) {
            lg.warn("无效的文档ID：{}[{}]", id, collectionName);
            return "";
        } else {
            // lg.debug(obj.toJson());
            return obj.getString("doc");
        }
    }

    /**
     * 删除Document
     *
     * @param id              文档ID
     * @param collectionName  集合名称
     */
    public static void deleteDocument(String id, String collectionName) throws Exception {
        // TODO 删除全文索引
        lg.info("删除Document：{}[{}]", id, collectionName);
        MongoTemplate mgt = (MongoTemplate) SpringContext.getBean("documentTemplate");
        mgt.remove(new Query(Criteria.where("_id").is(id)), collectionName);
    }

    // =================================================================
    //		GridFS
    // =================================================================

    /**
     * 存储附件（如果存在原附件，系统会自动先删除然后再插入一条新的记录）
     *
     * @deprecated 小尺寸的文件可以使用该方法上传，大尺寸10M以上的文件请调用其它方法
     *
     * @param bytes                  附件字节数组
     * @param id                        原附件ID（新增时传入null）
     * @param fileName            原始文件名称
     * @param contentType        资源mine类型
     * @param metaData            MongoDB元数据
     * @return MongoDB ID
     */
    public static String saveFile(byte[] bytes, String id, String fileName, String contentType, DBObject metaData) throws Exception {
        // 检查是否存在原附件
        if (StringUtils.isNotBlank(id)) {
            // TODO MongoDB暂时不支持update操作，所以更新时先删除再插入新的记录。
            MongoDB.deleteFile(id);
        }

        InputStream is = null;
        String fileId;
        MFileInfo mfileInfo = getNewMFileInfo();
        try {
            // GridFsTemplate gridFs = (GridFsTemplate) SpringContext.getBean("gridFsTemplate");
            GridFsTemplate gridFs = mfileInfo.getGridFsTemplate();
            is = new ByteArrayInputStream(bytes);
            fileId = gridFs.store(is, fileName, contentType, metaData).getId().toString();
        } finally {
            IOUtils.closeQuietly(is);
        }

        // return fileId;
        return mfileInfo.getFileId(fileId);
    }

    /**
     * 存储附件（如果存在原附件，系统会自动先删除然后再插入一条新的记录）
     *
     * @param filePath            附件磁盘绝对路径
     * @param id                        原附件ID（新增时传入null）
     * @param metaData            MongoDB元数据
     * @return MongoDB ID
     */
    public static String saveFile(String filePath, String id, DBObject metaData) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            lg.warn("无效的磁盘附件：{}", filePath);
            return null;
        }

        // 检查是否存在原附件
        if (StringUtils.isNotBlank(id)) {
            // TODO MongoDB暂时不支持update操作，所以更新时先删除再插入新的记录。
            MongoDB.deleteFile(id);
        }

        InputStream is = null;
        String fileId;
        MFileInfo mfileInfo = getNewMFileInfo();
        try {
            // GridFsTemplate gridFs = (GridFsTemplate) SpringContext.getBean("gridFsTemplate");
            GridFsTemplate gridFs = mfileInfo.getGridFsTemplate();
            is = new FileInputStream(filePath);
            fileId = gridFs.store(is, file.getName(), Files.probeContentType(Paths.get(filePath)), metaData).getId().toString();
        } finally {
            IOUtils.closeQuietly(is);
        }
        // return fileId;
        return mfileInfo.getFileId(fileId);
    }

    /**
     * 存储附件（如果存在原附件，系统会自动先删除然后再插入一条新的记录）
     *
     * @param filePath            附件磁盘绝对路径
     * @param id                        原附件ID（新增时传入null）
     * @param metaData            MongoDB元数据
     * @return MongoDB ID
     */
    public static String saveFile(String filePath, String fileName, String id, DBObject metaData) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            lg.warn("无效的磁盘附件：{}", filePath);
            return null;
        }

        // 检查是否存在原附件
        if (StringUtils.isNotBlank(id)) {
            // TODO MongoDB暂时不支持update操作，所以更新时先删除再插入新的记录。
            MongoDB.deleteFile(id);
        }

        InputStream is = null;
        String fileId;
        MFileInfo mfileInfo = getNewMFileInfo();
        try {
            // GridFsTemplate gridFs = (GridFsTemplate) SpringContext.getBean("gridFsTemplate");
            GridFsTemplate gridFs = mfileInfo.getGridFsTemplate();
            is = new FileInputStream(filePath);
            fileId = gridFs.store(is, fileName, Files.probeContentType(Paths.get(filePath)), metaData).getId().toString();
        } finally {
            IOUtils.closeQuietly(is);
        }
        // return fileId;
        return mfileInfo.getFileId(fileId);
    }

    /**
     * 存储附件（如果存在原附件，系统会自动先删除然后再插入一条新的记录）
     *
     * @param file                    SpringMVC上传文件
     * @param id                        原附件ID（新增时传入null）
     * @param metaData            MongoDB元数据
     * @return MongoDB ID
     */
    public static String saveFile(MultipartFile file, String id, DBObject metaData) throws Exception {
        // 检查是否存在原附件
        if (StringUtils.isNotBlank(id)) {
            // TODO MongoDB暂时不支持update操作，所以更新时先删除再插入新的记录。
            MongoDB.deleteFile(id);
        }

        InputStream is = null;
        String fileId;
        MFileInfo mfileInfo = getNewMFileInfo();
        try {
            // GridFsTemplate gridFs = (GridFsTemplate) SpringContext.getBean("gridFsTemplate");
            GridFsTemplate gridFs = mfileInfo.getGridFsTemplate();
            is = file.getInputStream();
            fileId = gridFs.store(is, file.getOriginalFilename(), file.getContentType(), metaData).getId().toString();
        } finally {
            IOUtils.closeQuietly(is);
        }
        // return fileId;
        return mfileInfo.getFileId(fileId);
    }

    /**
     * 存储附件（如果存在原附件，系统会自动先删除然后再插入一条新的记录）
     *
     * @param id                原附件ID（新增时传入null）
     * @param fileName            原始文件名称
     * @param contentType        资源mine类型
     * @param inputStream        文件内容
     * @param metaData            MongoDB元数据
     * @return MongoDB ID
     */
    public static String saveFile(String id, String fileName, String contentType, InputStream inputStream, DBObject metaData) throws Exception {
        // 检查是否存在原附件
        if (StringUtils.isNotBlank(id)) {
            // TODO MongoDB暂时不支持update操作，所以更新时先删除再插入新的记录。
            MongoDB.deleteFile(id);
        }
        MFileInfo mfileInfo = getNewMFileInfo();
        GridFsTemplate gridFs = mfileInfo.getGridFsTemplate();
        // GridFsTemplate gridFs = (GridFsTemplate) SpringContext.getBean("gridFsTemplate");
        // return gridFs.store(inputStream, fileName, contentType, metaData).getId().toString();
        String fileId = gridFs.store(inputStream, fileName, contentType, metaData).getId().toString();
        return mfileInfo.getFileId(fileId);
    }

    /**
     * 删除附件
     *
     * @param id    附件ID
     */
    public static void deleteFile(String id) throws Exception {
//		// 删除全文索引
//		try{
//			FullTextIndex.delete(id);
//		}catch (Exception e){
//			lg.info("删除全文索引：{}{}", id, e.getMessage());
//		}
        // GridFsTemplate gridFs = (GridFsTemplate) SpringContext.getBean("gridFsTemplate");
        MFileInfo mfileInfo = getFileInfo(id);
        GridFsTemplate gridFs = mfileInfo.getGridFsTemplate();
        gridFs.delete(new Query(Criteria.where("_id").is(id)));
    }

    /**
     * 加载附件
     *
     * @param id  附件ID
     * @return GridFSDBFile
     */
    public static GridFSDBFile loadFile(String id) throws Exception {
        if (StringUtils.isNotBlank(id)) {
            MFileInfo mfileInfo = getFileInfo(id);
            GridFsTemplate gridFs = mfileInfo.getGridFsTemplate();
            return gridFs.findOne(new Query(Criteria.where("_id").is(mfileInfo.id)));
            // GridFsTemplate gridFs = (GridFsTemplate) SpringContext.getBean("gridFsTemplate");
            // return gridFs.findOne(new Query(Criteria.where("_id").is(id)));
        } else {
            return null;
        }
    }

    /**
     * 批量删除附件
     *
     * @param type    附件ID
     */
    public static void batchDeleteFile(String type) throws Exception {
        MFileInfo mfileInfo = getFileInfo(type);
        GridFsTemplate gridFs = mfileInfo.getGridFsTemplate();
        gridFs.delete(new Query(Criteria.where("metadata.type").is(mfileInfo.id)));
        // GridFsTemplate gridFs = (GridFsTemplate) SpringContext.getBean("gridFsTemplate");
        // gridFs.delete(new Query(Criteria.where("metadata.type").is(type)));
    }

    /**
     * 删除附件
     *
     * @param id    附件ID
     */
    public static void deleteFileZQ(String id) throws Exception {
        MFileInfo mfileInfo = getFileInfo(id);
        GridFsTemplate gridFs = mfileInfo.getGridFsTemplate();
        gridFs.delete(new Query(Criteria.where("metadata.type").is(mfileInfo.id)));
        // GridFsTemplate gridFs = (GridFsTemplate) SpringContext.getBean("gridFsTemplate");
        // gridFs.delete(new Query(Criteria.where("_id").is(id)));
    }









    private static ConcurrentHashMap<String, GridFsTemplate> GRIDFSTEMPLATEMAP = new ConcurrentHashMap<String, GridFsTemplate>();

    /**
     * 	获取文件存储配置
     * @return
     * @throws Exception
     */
    private static MFileInfo getNewMFileInfo() throws Exception {
        return new MFileInfo(null, formateDate(new Date(), "yyyyMMdd"));
    }

    /**
     *  获取附件MongodbId和分表时间
     *  @param fileInfo
     *  @return
     * @throws Exception
     */
    public static MFileInfo getFileInfo(String fileInfo) throws Exception {
        String fileId = null;
        String fileDate = null;
        String[] fileInfos = fileInfo.split("-");
        if (fileInfos.length == 1) {
            // 兼容投行系统文件存储
            fileId = fileInfo;
            fileDate = null;    //投行不存在file_history  获取月份为null
        } else if (fileInfos.length == 2) {
            fileDate = fileInfo.split("-")[0];
            fileId = fileInfo.split("-")[1];
        }
        return new MFileInfo(fileId, fileDate);
    }

    /**
     * 根据日期字符串判断当月第几周
     *
     * @param date
     * @return
     * @throws Exception
     */
    public static int getWeekOfYear(String date, String pattern) throws Exception {
        // 将字符串格式化
        Calendar calendar = DateUtils.toCalendar(DateUtils.parseDate(date, pattern));
        // 第几周
        return calendar.get(Calendar.MONTH) + 1;
    }

    /**
     * 格式化显示日期
     *
     * @param date
     * @param pattern
     * @return
     */
    public static String formateDate(Date date, String pattern) {
        if (date == null) {
            return "";
        }
        return DateFormatUtils.format(date, pattern);
    }

    private static final Object objLock = new Object();

    private static class MFileInfo {
        private String id;
        private String date;
        private String bucketName = "th";

        public MFileInfo(String id, String date) throws Exception {
            super();
            this.id = id;
            this.date = date;
            if (StringUtils.isNotBlank(date)) {
                if (date.length() == 8) {
                    this.bucketName = "th_" + date.substring(0, 6) + "_" + getWeekOfYear(date, "yyyyMMdd");
                } else {
                    // 兼容老数据
                    this.bucketName = "th_" + date.substring(0, 6);
                }
            } else {
                this.bucketName = null;
            }
        }

        /**
         * 	获取存储ID
         * @param id
         * @return
         */
        public String getFileId(String id) {
            return this.date + "-" + id;
        }

        /**
         * 获取文件存储模板
         *
         * @return
         */
        public GridFsTemplate getGridFsTemplate() {
            GridFsTemplate gridFsTemplate = bucketName != null ? GRIDFSTEMPLATEMAP.get(bucketName) : null;
            if (gridFsTemplate == null) {
                synchronized (objLock) {
                    gridFsTemplate = bucketName != null ? GRIDFSTEMPLATEMAP.get(bucketName) : null;
                    if (gridFsTemplate == null) {
                        MongoDbFactory dbFactory = (MongoDbFactory) SpringContext.getBean("mongoDbFactory");
                        MongoConverter converter = (MongoConverter) SpringContext.getBean("converter");
                        gridFsTemplate = new GridFsTemplate(dbFactory, converter, bucketName);
                        if(bucketName != null) {
                            GRIDFSTEMPLATEMAP.put(bucketName, gridFsTemplate);
                        }
                    }
                }
            }
            return gridFsTemplate;
        }
    }
}







