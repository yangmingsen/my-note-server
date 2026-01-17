package top.yms.note.conpont.sync;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import top.yms.note.comm.NoteConstants;
import top.yms.note.entity.*;
import top.yms.note.mapper.NoteDataMapper;
import top.yms.note.mapper.NoteDataVersionMapper;
import top.yms.note.mapper.NoteMetaMapper;
import top.yms.note.mapper.NoteUserMapper;
import top.yms.note.other.ChatSync;
import top.yms.note.repo.ChatNoteRepository;
import top.yms.note.service.NoteDataService;
import top.yms.note.service.NoteMetaService;
import top.yms.note.utils.DateHelper;
import top.yms.note.utils.FNVHash;
import top.yms.note.utils.IdWorker;
import top.yms.note.utils.LocalThreadUtils;

import javax.annotation.Resource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractChatNoteSyncService implements GptChatNoteSyncService {

    private static final Logger log = LoggerFactory.getLogger(AbstractChatNoteSyncService.class);

    @Resource
    protected NoteMetaMapper noteMetaMapper;

    @Resource
    protected NoteDataMapper noteDataMapper;

    @Resource
    protected NoteDataVersionMapper noteDataVersionMapper;

    @Resource
    private NoteDataService noteDataService;

    @Resource
    protected IdWorker idWorker;

    @Resource
    protected NoteMetaService noteMetaService;

    @Resource
    protected NoteUserMapper noteUserMapper;

    @Resource
    protected ChatNoteRepository chatNoteRepository;

    /**
     * 对话数据基础目录
     */
    @Value("${chat.base-dir}")
    private String baseDir = "Chat";

    protected static class ChatMarkdownResult {
        public String id;
        public Date createTime;
        public Date updateTime;
        public String title;
        public String markdownContent;

        public ChatMarkdownResult(String id, String title, Date createTime, Date updateTime, String markdownContent) {
            this.id = id;
            this.title = title;
            this.createTime = createTime;
            this.updateTime = updateTime;
            this.markdownContent = markdownContent;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Date getCreateTime() {
            return createTime;
        }

        public void setCreateTime(Date createTime) {
            this.createTime = createTime;
        }

        public Date getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(Date updateTime) {
            this.updateTime = updateTime;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getMarkdownContent() {
            return markdownContent;
        }

        public void setMarkdownContent(String markdownContent) {
            this.markdownContent = markdownContent;
        }
    }

    protected String getBaseDir() {
        return baseDir;
    }

    @Override
    public boolean support() {
        return true;
    }

    protected abstract String getDefaultDirName();

    /**
     * 获取当前markdown数据存储位置：比如deepseek的对话数据应该存在DeepSeek目录下，其他也是
     *  子类应该要：比如DeepSeek目录不存在Chat目录下时，新建DeepSeek目录，然后返回DeepSeek目录的id；
     *            若是存在，则只需返回DeepSeek目录id即可
     * @return
     */
    protected Long getDefaultAndCreateDirName(Long parentId) {
        Long userId = LocalThreadUtils.getUserId();
        NoteMetaExample example = new NoteMetaExample();
        NoteMetaExample.Criteria criteria = example.createCriteria();
        criteria.andParentIdEqualTo(parentId);
        criteria.andUserIdEqualTo(userId);
        criteria.andNameEqualTo(getDefaultDirName());
        List<NoteMeta> noteMetas = noteMetaMapper.selectByExample(example);
        Long baseDirId;
        if (noteMetas.isEmpty()) {
            baseDirId = noteMetaService.createDir(getDefaultDirName(), parentId).getId();
        } else {
            baseDirId =  noteMetas.get(0).getId();
        }
        return baseDirId;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class, timeout = 60)
    public void doSync() {
        List<ChatMarkdownResult> chatMarkdownResults = null;
        try {
            File file = new File(getChatNoteDataPath());
            chatMarkdownResults = parse(file);
        } catch (Exception e) {
            log.error("doSync parse error", e);
        }
        if (chatMarkdownResults == null) return;
        log.info("parse size={}", chatMarkdownResults.size());
        String baseDirName = getBaseDir();
        NoteMetaExample example = new NoteMetaExample();
        NoteMetaExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(baseDirName);
        List<NoteMeta> noteMetas = noteMetaMapper.selectByExample(example);
        Long baseDirId; //当前Chat 目录id
        Long userId = LocalThreadUtils.getUserId();
        if (noteMetas.isEmpty()) {
            NoteUser noteUser = noteUserMapper.selectByPrimaryKey(userId);
            baseDirId = noteMetaService.createDir(baseDirName, noteUser.getNoteRootTreeId()).getId();
        } else {
            baseDirId = noteMetas.get(0).getId();
        }
        //获取到真正存储位置
        Long storeParentId = getDefaultStoreParentId(baseDirId);
        //按照目录名称分类
        Map<String, NoteMeta> metaNameMap = noteMetaService.findNoteMetaByParentId(storeParentId)
                .stream()
                .filter(note -> NoteConstants.DIR_FLAG.equals(note.getIsFile()))
                .collect(Collectors.toMap(NoteMeta::getName, note -> note));
        //foreach entry
        for (ChatMarkdownResult cmr : chatMarkdownResults) {
            //version data
            NoteDataVersion noteDataVersion = new NoteDataVersion();
            noteDataVersion.setContent(cmr.markdownContent);
            noteDataVersion.setUserId(userId);
            noteDataVersion.setCreateTime(new Date());
            //find old data
            ChatNote oldChatNote = chatNoteRepository.findByChatId(cmr.id);
            //获取存储位置parentId
            Long pid = getRealStoreParentId(storeParentId, metaNameMap, cmr);
            //check
            if (oldChatNote == null) {
                long noteId = idWorker.nextId();
                Date date = cmr.createTime;
                //pack meta info
                NoteMeta noteMeta = new NoteMeta();
                noteMeta.setId(noteId);
                noteMeta.setName(cmr.title);
                noteMeta.setParentId(pid);
                noteMeta.setUserId(userId);
                noteMeta.setIsFile(NoteConstants.FILE_FLAG);
                noteMeta.setType(NoteConstants.markdownSuffix);
                noteMeta.setCreateTime(date);
                noteMeta.setUpdateTime(date);
                noteMeta.setStoreSite(NoteConstants.MYSQL);
                noteMeta.setSize((long)cmr.markdownContent.getBytes(StandardCharsets.UTF_8).length);
                //todo 这几个insert 要优化成批量插入
                noteMetaMapper.insertSelective(noteMeta);
                //note data
                NoteData noteData = new NoteData();
                noteData.setId(noteId);
                noteData.setContent(cmr.markdownContent);
                noteData.setUserId(userId);
                noteData.setCreateTime(date);
                noteData.setUpdateTime(date);
                noteDataMapper.insertSelective(noteData);
                //version
                noteDataVersion.setNoteId(noteId);
                //relation
                ChatNote chatNote = new ChatNote();
                chatNote.setNoteId(noteId);
                chatNote.setChatId(cmr.id);
                chatNote.setTitle(cmr.title);
                chatNote.setCreateTime(date);
                chatNote.setUpdateTime(cmr.updateTime);
                chatNoteRepository.save(chatNote);
            } else {
                long noteId = oldChatNote.getNoteId();
                NoteMeta noteMeta = new NoteMeta();
                noteMeta.setId(noteId);
                noteMeta.setUpdateTime(new Date());
                noteMeta.setSize((long)cmr.markdownContent.getBytes(StandardCharsets.UTF_8).length);
                noteMetaService.update(noteMeta);
                //note data
                NoteData noteData = new NoteData();
                noteData.setId(noteId);
                noteData.setContent(cmr.markdownContent);
                noteData.setUpdateTime(new Date());
                noteDataService.update(noteData);
                //version
                noteDataVersion.setNoteId(noteId);
            }
            //add version
            addToDataVersion(noteDataVersion);
        }
        log.info("sync ok=================");

    }

    /**
     * 获取对话数据地址
     * @return
     */
    protected abstract String getChatNoteDataPath();

    /**
     * 数据解析成markdown列表
     * @param file
     * @return
     * @throws Exception
     */
    protected abstract List<ChatMarkdownResult> parse(File file) throws Exception;

    /**
     * 获取当前markdown数据存储位置：比如deepseek的对话数据应该存在DeepSeek目录下，其他也是
     *  子类应该要：比如DeepSeek目录不存在Chat目录下时，新建DeepSeek目录，然后返回DeepSeek目录的id；
     *            若是存在，则只需返回DeepSeek目录id即可
     * @return
     */
    protected abstract Long getDefaultStoreParentId(Long parentId);

    /**
     * 获取真正存储位置：也就是说当前这篇文章应该要放到那个目录下由子类决定
     * @return
     */
    protected  Long getRealStoreParentId(Long storeParentId, Map<String, NoteMeta> metaNameMap,
                                                 ChatMarkdownResult cmr) {
        Date createTime = cmr.getCreateTime();
        //先找1级目录  yyyy 格式
        String yearStr = DateHelper.dateStrMatch(createTime, DateHelper.PATTERN8);
        NoteMeta noteMeta1 = metaNameMap.get(yearStr);
        if (noteMeta1 == null) {
            noteMeta1 = noteMetaService.createDir(yearStr, storeParentId);
            metaNameMap.put(noteMeta1.getName(), noteMeta1);
        }
        //再找2级目录 yyyy-MM 格式
        String dateStr2 = DateHelper.dateStrMatch(createTime, DateHelper.PATTERN3);
        NoteMeta noteMeta2 = metaNameMap.get(dateStr2);
        if (noteMeta2 == null) {
            noteMeta2 = noteMetaService.createDir(dateStr2, noteMeta1.getId());
            metaNameMap.put(noteMeta2.getName(), noteMeta2);
        }
        return noteMeta2.getId();
    }

    /**
     * 若是新数据，得做版本。 旧数据看与上次版本是否一致，不一致则加入新版本
     * @param noteDataVersion
     */
    protected void addToDataVersion(NoteDataVersion noteDataVersion) {
        Long noteId = noteDataVersion.getNoteId();
        List<NoteDataVersion> noteDataVersions = noteDataVersionMapper.selectByNoteId(noteId);
        NoteDataVersion lastOne = null;
        if (!noteDataVersions.isEmpty()) {
            noteDataVersions.sort(Comparator.comparing(NoteDataVersion::getCreateTime));
            lastOne = noteDataVersions.get(noteDataVersions.size() - 1);
        }
        if (lastOne != null) {
            long h1 = FNVHash.fnv1aHash64(noteDataVersion.getContent());
            long h2 = FNVHash.fnv1aHash64(lastOne.getContent());
            if (h1 != h2) {
                noteDataVersionMapper.insertSelective(noteDataVersion);
            }
        } else {
            noteDataVersionMapper.insertSelective(noteDataVersion);
        }
    }
}
