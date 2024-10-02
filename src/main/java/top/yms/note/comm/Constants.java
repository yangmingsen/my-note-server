package top.yms.note.comm;

/**
 * Created by yangmingsen on 2024/4/6.
 */
public abstract class Constants {
    public final static String USER_ID = "USER_ID";
    public final static String userid = "userid";
    public final static String MONGO = "mongo";
    public final static String MYSQL = "mysql";
    public final static String BASE_URL = "http://api.notetest.yms.top/note/file/view?id=";
    public final static String BASE_TMP_VIEW_URL = "http://api.notetest.yms.top/note/file/tmpView?id=";
    public final static String MONGO_FILE_SITE = "note.file";

    public final static String markdownSuffix = "md";
    public final static String mindmapSuffix = "mindmap";
    public final static String defaultSuffix = "wer";

    public final static String noteMindMap = "note_mindmap";
    public final static String noteWerTextContent = "wer_text_content";
    public final static String customConfig = "custom_config";
    public final static String tmpUploadFile = "tmp_upload_file";
    public final static String taskInfoMessage = "task_info_message";


    public final static String defaultNoteCache = "defaultNoteCache";
    public final static String userMemoryNoteCache = "userMemoryNoteCache";
    public final static String noteLuceneSearch = "noteLuceneSearch";
    public final static String noteDefaultSearch = "noteDefaultSearch";
    public final static String noteLuceneIndexMemoryQueue = "noteLuceneIndexMemoryQueue";
    public final static String asyncTaskMemoryQueue = "asyncTaskMemoryQueue";

    public final static String noteThreadPoolExecutor = "noteThreadPoolExecutor";
    public final static String noteScheduledThreadPoolExecutor = "noteScheduledThreadPoolExecutor";


    public final static String bgImgInfo = "bgImgInfo";



    public final static String token = "token";


}
