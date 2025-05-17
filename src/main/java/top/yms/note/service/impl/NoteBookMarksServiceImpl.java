package top.yms.note.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteAsyncExecuteTaskService;
import top.yms.note.conpont.task.AsyncTask;
import top.yms.note.entity.NoteBookmarks;
import top.yms.note.entity.NoteBookmarksExample;
import top.yms.note.entity.NoteUser;
import top.yms.note.enums.AsyncExcuteTypeEnum;
import top.yms.note.enums.AsyncTaskEnum;
import top.yms.note.mapper.NoteBookmarksMapper;
import top.yms.note.mapper.NoteUserMapper;
import top.yms.note.service.NoteBookMarksService;
import top.yms.note.utils.IdWorker;
import top.yms.note.utils.LocalThreadUtils;

import javax.annotation.Resource;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

@Service
public class NoteBookMarksServiceImpl implements NoteBookMarksService {

    private static final Logger log = LoggerFactory.getLogger(NoteBookMarksServiceImpl.class);

    @Resource
    private NoteBookmarksMapper noteBookmarksMapper;

    @Value("${local.bookmarks}")
    private String localBookmarksPath;

    @Resource
    private IdWorker idWorker;

    @Resource
    private NoteUserMapper noteUserMapper;

    @Resource
    private NoteAsyncExecuteTaskService noteExecuteTaskService;

    @Override
    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 60)
    public void syncWithLocalBookmarks() throws Exception {
        // 读取文件内容为字符串
        String json = new String(Files.readAllBytes(Paths.get(localBookmarksPath)));
        // 解析成JSONObject
        JSONObject root = JSON.parseObject(json);
        JSONObject roots = root.getJSONObject("roots");
        JSONObject bookmark_bar = roots.getJSONObject("bookmark_bar");
        NoteUser noteUser = noteUserMapper.selectByPrimaryKey(LocalThreadUtils.getUserId());
        doSyncWithLocalBookmarks(bookmark_bar, noteUser.getNoteRootTreeId());
    }



    private  void doSyncWithLocalBookmarks(JSONObject node, Long parentId) {
        if (node == null) return;
        String type = node.getString("type");
        String url = node.getString("url");
        String name = node.getString("name");
        JSONObject metaInfo = node.getJSONObject("meta_info");
        Long cId = node.getLongValue("id");
        String guid = node.getString("guid");
        String dateLastUsed = node.getString("date_last_used");
        String dateAdded = node.getString("date_added");
        Long id = idWorker.nextId();
        NoteBookmarks noteBookmarks = new NoteBookmarks();
        noteBookmarks.setId(id);
        noteBookmarks.setParentId(parentId);
        noteBookmarks.setName(name);
        noteBookmarks.setType(type);
        noteBookmarks.setUrl(url);
        noteBookmarks.setChromeId(cId);
        noteBookmarks.setGuid(guid);
        noteBookmarks.setDateAdded(dateAdded);
        noteBookmarks.setDateLastUsed(dateLastUsed);
        noteBookmarks.setUserId(LocalThreadUtils.getUserId());
        if (metaInfo != null) {
            noteBookmarks.setMetaInfo(metaInfo.toString());
        }
        noteBookmarks.setCreateTime(new Date());
        noteBookmarks.setUpdateTime(new Date());
        noteBookmarks.setSyncFlag(NoteConstants.BOOKMARKS_SYNC_FLAG_UN);
        NoteBookmarksExample example = new NoteBookmarksExample();
        NoteBookmarksExample.Criteria criteria = example.createCriteria();
        criteria.andGuidEqualTo(guid);
        List<NoteBookmarks> oldBookmarks = noteBookmarksMapper.selectByExample(example);
        if (oldBookmarks.isEmpty()) {
            noteBookmarksMapper.insertSelective(noteBookmarks);
        } else {
            noteBookmarksMapper.updateByExampleSelective(noteBookmarks, example);
        }
        if (NoteConstants.BOOKMARKS_FOLDER.equals(type)) {
            JSONArray children = node.getJSONArray(NoteConstants.BOOKMARKS_CHILDREN);
            if (children != null) {
                for (int i = 0; i < children.size(); i++) {
                    JSONObject child = children.getJSONObject(i);
                    doSyncWithLocalBookmarks(child, id);
                }
            }
        }
    }

    @Override
    public void syncBookmarksNote() throws Exception {
        try {
            Long userId = LocalThreadUtils.getUserId();
            NoteBookmarksExample example = new NoteBookmarksExample();
            NoteBookmarksExample.Criteria criteria = example.createCriteria();
            criteria.andUserIdEqualTo(userId);
//            criteria.andSyncFlagEqualTo(NoteConstants.BOOKMARKS_SYNC_FLAG_UN);
            List<NoteBookmarks> noteBookmarks = noteBookmarksMapper.selectByExample(example);
            log.debug("userId={}, getNoteBookmarks size={}", userId, noteBookmarks.size());
            for (NoteBookmarks bookmarks : noteBookmarks) {
                AsyncTask visitComputeTask = AsyncTask.Builder.build()
                        .taskId(idWorker.nextId())
                        .type(AsyncTaskEnum.BOOKMARKS_SYNC_TASK)
                        .taskName(AsyncTaskEnum.BOOKMARKS_SYNC_TASK.getName())
                        .executeType(AsyncExcuteTypeEnum.CUSTOM_TASK)
                        .createTime(new Date())
                        .userId(LocalThreadUtils.getUserId())
                        .taskInfo(bookmarks)
                        .get();
                noteExecuteTaskService.addTask(visitComputeTask);
            }
        } catch (Throwable t) {
            log.error("syncBookmarksNote error", t);
        }

    }

}
