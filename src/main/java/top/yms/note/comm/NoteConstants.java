package top.yms.note.comm;

import org.apache.commons.lang3.StringUtils;
import top.yms.note.config.SpringContext;
import top.yms.note.conpont.SysConfigService;

/**
 * Created by yangmingsen on 2024/4/6.
 */
public abstract class NoteConstants {

    public final static String METHOD_NOT_IMPLEMENT = "METHOD_NOT_IMPLEMENT";

    public final static String USER_ID = "USER_ID";
    public final static String userid = "userid";
    public final static String MONGO = "mongo";
    public final static String MYSQL = "mysql";
    public final static String BASE_URL = "http://api.notetest.yms.top/note/file/view?id=";
    public final static String FILE_VIEW_URL = "file/view?id=";
    public final static String FILE_DOWNLOAD_URL = "file/download?id=";

    public final static String BASE_TMP_VIEW_URL = "http://api.notetest.yms.top/note/file/tmpView?id=";
    public final static String TMP_FILE_VIEW_URL = "file/tmpView?id=";
    public final static String MONGO_FILE_SITE = "note.file";

    public final static String markdownSuffix = "md";
    public final static String mindmapSuffix = "mindmap";
    public final static String defaultSuffix = "wer";
    public final static String zipSuffix = "zip";

    public final static String noteMindMap = "note_mindmap";
    public final static String noteWerTextContent = "wer_text_content";
    public final static String customConfig = "custom_config";
    public final static String tmpUploadFile = "tmp_upload_file";
    public final static String taskInfoMessage = "task_info_message";


    public final static String defaultNoteCache = "defaultNoteCache";
    public final static String userMemoryNoteCache = "userMemoryNoteCache";
    public final static String weakMemoryNoteCache = "weakMemoryNoteCache";
    public final static String noteExpireTimeCache = "noteExpireTimeCache";
    public final static String noteLuceneSearch = "noteLuceneSearch";
    public final static String noteDefaultSearch = "noteDefaultSearch";
    public final static String noteLuceneIndexMemoryQueue = "noteLuceneIndexMemoryQueue";
    public final static String asyncTaskMemoryQueue = "asyncTaskMemoryQueue";

    public final static String noteThreadPoolExecutor = "noteThreadPoolExecutor";
    public final static String noteScheduledThreadPoolExecutor = "noteScheduledThreadPoolExecutor";
    public final static String defaultNoteStoreServiceImpl = "defaultNoteStoreServiceImpl";
    public final static String noteLuceneDataServiceImpl = "noteLuceneDataServiceImpl";
    public final static String noteContentOptimizeServiceImpl = "noteContentOptimizeServiceImpl";


    public final static String bgImgInfo = "bgImgInfo";
    public final static String lastvisit = "lastvisit";
    public final static String noteId = "noteId";
    public final static String id = "id";
    public final static String textContent = "textContent";

    public final static String tmpReadPasswordToken = "tmp_read_password_token";

    public final static String token = "token";

    /** lucene index data const ***/
    public final static String IDX_ID = "id";
    public final static String IDX_USER_ID = "userId";
    public final static String IDX_PARENT_ID = "parentId";
    public final static String IDX_TITLE = "title";
    public final static String IDX_CONTENT = "content";
    public final static String IDX_TYPE = "type";
    public final static String IDX_IS_FILE = "isFile";
    public final static String IDX_CREATE_DATE = "createDate";
    public final static String IDX_ENCRYPTED = "encrypted";
    public final static String IDX_NOTE_PATH = "notePath";

    /**
     * 加密标志
     */
    public final static String ENCRYPTED_FLAG = "1";
    public final static String ENCRYPTED_UN_FLAG = "0";

    public final static String FILE_FLAG = "1";
    public final static String DIR_FLAG = "0";



    /**
     * root目录标志
     */
    public final static String ROOT_DIR_FLAG = "0";

    /**
     * mongo id标志
     */
    public final static String _id = "_id";



    public final static String PDF = "pdf";
    public final static String DOCX = "docx";
    public final static String MARKDOWN = "md";
    public final static String WER = "wer";

    //加密临时访问标志
    public final static String TMP_VISIT_TOKEN = "tmp_token:";
    public final static String TMP_TOKEN_FLAG = "tmpToken";


    //mongo component
    public final static String legacyGridFsTemplate = "legacyGridFsTemplate";
    public final static String legacyGridFSBucket = "legacyGridFSBucket";
    public final static String bigFileGridFsTemplate = "bigFileGridFsTemplate";
    public final static String bigFileGridFsBucket = "bigFileGridFsBucket";

    public final static String mongoFileStoreService = "mongoFileStoreService";
    public final static String mongoFileStore449 = "mongoFileStore449";
    //mongo file start prefix
    public final static String NEW_BIG_FILE_PREFIX = "b:";
    //小文件前缀
    public final static String NEW_SMALL_FILE_PREFIX = "d:";
    //noteOptionMap const
    public final static String OPTION_FILE_NAME = "fileName";
    public final static String OPTION_FILE_TYPE = "fileType";
    public final static String OPTION_FILE_SIZE = "fileSize";


    //cache PREFIX
    public final static String C_METHOD = "c:m:";











    public static String getFileViewUrlSuffix(String id) {
        return FILE_VIEW_URL+id;
    }

    public static String getFileDownloadUrlSuffix(String id) {
        return FILE_DOWNLOAD_URL+id;
    }

    public static String getTmpFileViewUrlSuffix(String id) {
        return TMP_FILE_VIEW_URL+id;
    }

    public static String getBaseUrl() {
        SysConfigService sysConfigService = SpringContext.getBean(SysConfigService.class);
        String baseUrl = sysConfigService.getStringValue("system.base_url");
        if (StringUtils.isBlank(baseUrl)) {
            throw new RuntimeException("base_url is 空");
        }
        return baseUrl;
    }


}
