package top.yms.note.conpont.sync.network;

import org.apache.commons.lang3.StringUtils;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.SysConfigService;
import top.yms.note.conpont.cache.NoteRedisCacheService;
import top.yms.note.conpont.sync.NoteSyncService;
import top.yms.note.entity.*;
import top.yms.note.enums.FileTypeEnum;
import top.yms.note.mapper.NoteMetaMapper;
import top.yms.note.repo.NetworkNoteRepository;
import top.yms.note.service.NoteDataService;
import top.yms.note.service.NoteMetaService;
import top.yms.note.service.NoteUserService;
import top.yms.note.utils.LocalThreadUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

public abstract class AbstractNetworkNoteSyncService implements NoteSyncService {

    @Resource
    protected NetworkNoteRepository networkNoteRepository;

    @Resource
    protected NoteRedisCacheService cacheService;

    @Resource
    private SysConfigService sysConfigService;

    @Resource
    protected NoteMetaService noteMetaService;

    @Resource
    private NoteDataService noteDataService;

    @Resource
    protected NoteMetaMapper noteMetaMapper;

    @Resource
    private NoteUserService noteUserService;

    /**
     * 顶层 网络笔记id
     */
    protected Long rootLevelId;
    /**
     * 顶层 网络笔记
     */
    private static final String rootLevelName = "网络笔记";

    /**
     * 获取二级目录
     * @param parentId
     * @return
     */
    abstract Long getSecondLevelId(Long parentId);

    abstract Long getThirdLevelId(Long parentId, String param);



    /**
     * 获取 网络笔记 目录id
     * @return
     */
    protected Long getRootLevelId() {
        if (rootLevelId != null) {
            return rootLevelId;
        }
        //获取userId
        Long userId = getUserId();
        NoteUser noteUser = noteUserService.findOne(userId);
        //获取对应名称目录id
        rootLevelId = findOrCreate(rootLevelName, noteUser.getNoteRootTreeId());
        return rootLevelId;
    }

    protected Long findOrCreate(String dirName, Long parentId) {
        NoteMetaExample example = new NoteMetaExample();
        NoteMetaExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(dirName);
        criteria.andDelEqualTo(NoteConstants.UN_DELETE_FLAG);
        List<NoteMeta> noteMetas = noteMetaMapper.selectByExample(example);
        Long baseDirId;
        if (noteMetas.isEmpty()) {
            baseDirId = noteMetaService.createDir(dirName, parentId).getId();
        } else {
            //bug20260124 若是存在多个同名情况，可能会拿错数据
            //baseDirId = noteMetas.get(0).getId();
            for (NoteMeta noteMeta : noteMetas) {
                if (noteMeta.getParentId().equals(parentId)) {
                    return noteMeta.getId();
                }
            }
            //若存在同名的情况下，还是没有找到在指定parent目录下，则创建
            baseDirId = noteMetaService.createDir(dirName, parentId).getId();
        }
        return baseDirId;
    }

    public boolean support() {
        return false;
    }

    public void doSync() {
        for (NetworkNote networkNote : networkNoteRepository.findAll()) {
            doDataSync(networkNote);
        }
    }

    protected Long getUserId() {
        //获取userId
        Long userId = LocalThreadUtils.getUserId();
        if (userId == null) {
            userId = sysConfigService.getLongValue("sys.default-user-id");
        }
        return userId;
    }

    protected void doDataSync(NetworkNote networkNote) {
        Long userId = getUserId();
        Long noteId = networkNote.getNoteId();
        //meta
        NoteMeta noteMeta = new NoteMeta();
        noteMeta.setId(networkNote.getNoteId());
        noteMeta.setName(networkNote.getTitle());
        noteMeta.setType(FileTypeEnum.MARKDOWN.getValue());
        noteMeta.setParentId(getThirdLevelId(getSecondLevelId(getRootLevelId()), networkNote.getUrl()));
        noteMeta.setStoreSite(NoteConstants.MYSQL);
        noteMeta.setUserId(userId);
        noteMeta.setIsFile(NoteConstants.FILE_FLAG);
        Date cDate = new Date();
        noteMeta.setCreateTime(cDate);
        noteMeta.setUpdateTime(cDate);
        noteMeta.setSize((long)networkNote.getContent().getBytes(StandardCharsets.UTF_8).length);
        //prepare data
        NoteData noteData = new NoteData();
        noteData.setId(noteId);
        noteData.setUserId(userId);
        noteData.setContent(networkNote.getContent());
        noteData.setCreateTime(cDate);
        noteData.setUpdateTime(cDate);
        noteDataService.addOrUpdateNote(noteMeta, noteData);
    }

    protected  String extractLevelDirectory(String url, int level) {
        String pathName = UrlPathExtractor.extractDirectoryOnlyByLevel(url, level);
        if (StringUtils.isBlank(pathName)) {
            return "["+level+"]级空目录";
        }
        return pathName;
    }

    /**
     * 提取URL中的一级目录
     * @param url 输入的URL字符串
     * @return 一级目录名称，如果没有目录则返回空字符串
     */
    protected  String extractLevelDirectory(String url) {
        return extractLevelDirectory(url, 1);
        /*try {
            // 解析URL
            URI uri = new URI(url);

            // 获取路径部分
            String path = uri.getPath();

            // 如果路径为空或为根路径，返回空字符串
            if (path == null || path.isEmpty() || path.equals("/")) {
                return "";
            }

            // 移除开头和结尾的斜杠，并按斜杠分割
            String cleanPath = path.startsWith("/") ? path.substring(1) : path;

            // 查找第一个斜杠的位置
            int slashIndex = cleanPath.indexOf('/');

            // 如果找到斜杠，取斜杠之前的部分；否则返回整个路径
            String firstLevel = slashIndex > 0 ? cleanPath.substring(0, slashIndex) : cleanPath;

            return firstLevel;

        } catch (URISyntaxException e) {
            // URL格式错误，返回空字符串
            return "其他分类";
        }*/
    }

    protected String transferName(String enName) {
        String toName = getTransferName(enName);
        if (toName != null) {
            return toName;
        }
        return enName;
    }

    protected String getTransferName(String enName) {
        return null;
    }
}
