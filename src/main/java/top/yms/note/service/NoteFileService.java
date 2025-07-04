package top.yms.note.service;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.web.multipart.MultipartFile;
import top.yms.note.entity.NoteFile;
import top.yms.note.entity.NoteMeta;
import top.yms.note.entity.NoteTree;
import top.yms.note.exception.BusinessException;
import top.yms.note.vo.LocalNoteSyncResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;

public interface NoteFileService {
    JSONObject uploadFileForWer(MultipartFile file, Long noteId);

    NoteFile findOne(String fileId);

    void addNote(MultipartFile file, NoteMeta note) throws Exception;

    void syncNoteFromLocalFS(NoteTree noteTree, File file, List<String> mongoRollBackList, List<LocalNoteSyncResult> syncStatisticList) throws Exception;

    void generateTree(File file, Long parentId, final List<String> mongoRollBackList, List<LocalNoteSyncResult> syncStatisticList) throws Exception;

    JSONObject uploadFile(MultipartFile file, Long noteId) throws BusinessException;

    JSONObject uploadText(String textContent, Long parentId);

    void urlToPdf(String htmlUrl, Long parentId);

    void download(String id, HttpServletRequest req, HttpServletResponse resp)  throws Exception;

    Long fetch(String url, String toType, Long parentId);

    void uploadMultiNote(Long parentId, List<MultipartFile> file) throws Exception;
}
