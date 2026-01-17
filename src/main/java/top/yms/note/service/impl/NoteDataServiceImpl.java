package top.yms.note.service.impl;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import top.yms.note.comm.NoteCacheKey;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.AnyFile;
import top.yms.note.conpont.FileStoreService;
import top.yms.note.conpont.NoteDataIndexService;
import top.yms.note.conpont.NoteStoreService;
import top.yms.note.conpont.cache.NoteRedisCacheService;
import top.yms.note.conpont.search.NoteLuceneIndex;
import top.yms.note.dao.NoteFileQuery;
import top.yms.note.dao.NoteIndexQuery;
import top.yms.note.dto.INoteData;
import top.yms.note.dto.NoteDataDto;
import top.yms.note.entity.*;
import top.yms.note.enums.FileTypeEnum;
import top.yms.note.exception.BusinessException;
import top.yms.note.mapper.NoteDataMapper;
import top.yms.note.mapper.NoteDataVersionMapper;
import top.yms.note.mapper.NoteFileMapper;
import top.yms.note.mapper.NoteMetaMapper;
import top.yms.note.msgcd.CommonErrorCode;
import top.yms.note.msgcd.NoteIndexErrorCode;
import top.yms.note.service.NoteDataService;
import top.yms.note.service.NoteMetaService;
import top.yms.note.utils.LocalThreadUtils;

import javax.annotation.Resource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yangmingsen on 2024/4/6.
 */
@Service
public class NoteDataServiceImpl implements NoteDataService {

    private static final Logger log = LoggerFactory.getLogger(NoteDataServiceImpl.class);

    @Resource
    private NoteDataMapper noteDataMapper;

    @Resource
    private NoteDataVersionMapper noteDataVersionMapper;

    @Resource
    private NoteMetaMapper noteMetaMapper;

    @Resource
    private FileStoreService fileStoreService;

    @Resource
    @Qualifier(NoteConstants.noteLuceneSearch)
    private NoteDataIndexService noteDataIndexService;

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private NoteStoreService noteStoreService;

    @Resource
    private NoteFileMapper noteFileMapper;

    @Resource
    private NoteMetaService noteMetaService;


    @Resource
    private NoteRedisCacheService cacheService;

    private final String noteMindMap = NoteConstants.noteMindMap;

    /**
     * 过期与20240927，请使用 NoteStoreService#save
     * @param noteId
     * @param jsonContent
     * @return
     */
    @Deprecated
    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 10)
    public RestOut saveMindMapData(Long noteId, String jsonContent) {
        ObjectId objId = null;
        NoteMeta upNoteMeta = new NoteMeta();
        Document oldDoc = null;
        try {
            Document document = Document.parse(jsonContent);
            NoteMeta noteMeta1 = noteMetaService.findOne(noteId);
            if (StringUtils.isBlank(noteMeta1.getSiteId())) {
                Document saveRes = mongoTemplate.save(document, noteMindMap);
                objId = saveRes.getObjectId("_id");
                upNoteMeta.setSiteId(objId.toString());
            } else {
                oldDoc = mongoTemplate.findById(noteMeta1.getSiteId(), Document.class, noteMindMap);
                ObjectId objectId = new ObjectId(noteMeta1.getSiteId());
                document.put("_id", objectId);
                mongoTemplate.save(document, noteMindMap);
            }
            Date opTime = new Date();
            long size = jsonContent.getBytes(StandardCharsets.UTF_8).length;
            //更新index信息
            upNoteMeta.setId(noteId);
            upNoteMeta.setUpdateTime(opTime);
            upNoteMeta.setSize(size);
            noteMetaService.update(upNoteMeta);
        } catch (Exception e) {
            if (oldDoc != null) {
                mongoTemplate.save(oldDoc, noteMindMap);
            }
            throw new BusinessException(CommonErrorCode.E_203008);
        }
        return RestOut.succeed();
    }

    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 10)
    public void save(INoteData iNoteData) {
        noteStoreService.save(iNoteData);
    }

    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 10)
    public NoteData findNoteData(Long id) {
        return (NoteData)noteStoreService.findOne(id);
    }

    public NoteData findOneByPk(Long id) {
        //cache it
        Object cVal = cacheService.hGet(NoteCacheKey.NOTE_DATA_LIST_KEY, id.toString());
        if (cVal != null) {
            return (NoteData) cVal;
        }
        //find it
        NoteData noteData =noteDataMapper.selectByPrimaryKey(id);
        //to data
        cacheService.hSet(NoteCacheKey.NOTE_DATA_LIST_KEY, id.toString(), noteData);
        return noteData;
    }

    /**
     * 过期于20240927, 请使用 NoteStoreService#save
     * @param noteDataDto
     */
    @Deprecated
    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 10)
    public void addAndUpdate(NoteDataDto noteDataDto) {
        ObjectId objId = null;
        Document oldDoc = null;
        try {
            NoteData noteData = new NoteData();
            BeanUtils.copyProperties(noteDataDto, noteData);
            Long id = noteData.getId();
            NoteData dbNote = noteDataMapper.findById(id);
            if (checkContent(noteData.getContent())) {
                throw new BusinessException(NoteIndexErrorCode.E_203112);
            }
            Date opTime = new Date();
            if (dbNote == null) {
                noteData.setCreateTime(opTime);
                noteDataMapper.insert(noteData);
            } else {
                noteData.setUpdateTime(opTime);
                update(noteData);
            }
            NoteMeta noteMeta1 = noteMetaService.findOne(id);
            //更新index信息
            NoteMeta noteMeta = new NoteMeta();
            noteMeta.setId(id);
            noteMeta.setUpdateTime(opTime);
            noteMeta.setSize((long)noteData.getContent().getBytes(StandardCharsets.UTF_8).length);
            if (StringUtils.isNotBlank(noteDataDto.getTextContent())) {
                if ("wer".equals(noteDataDto.getType())) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", noteDataDto.getId());
                    jsonObject.put("textConent", noteDataDto.getTextContent());
                    Document document = Document.parse(jsonObject.toString());

                    if (StringUtils.isNotBlank(noteMeta1.getSiteId())) {
                        oldDoc = mongoTemplate.findById(noteMeta1.getSiteId(), Document.class, NoteConstants.noteWerTextContent);
                        ObjectId objectId = new ObjectId(noteMeta1.getSiteId());
                        document.put("_id", objectId);
                        mongoTemplate.save(document,  NoteConstants.noteWerTextContent);
                    } else {
                        Document saveRes = mongoTemplate.save(document, NoteConstants.noteWerTextContent);
                        objId = saveRes.getObjectId("_id");
                        noteMeta.setSiteId(objId.toString());
                    }
                }

            }
            noteMetaService.update(noteMeta);
            //通知更新lucene索引
            NoteLuceneIndex noteLuceneIndex = new NoteLuceneIndex();
            noteLuceneIndex.setId(id);
            noteLuceneIndex.setUserId(noteMeta1.getUserId());
            noteLuceneIndex.setParentId(noteMeta1.getParentId());
            noteLuceneIndex.setTitle(noteMeta1.getName());
            if (StringUtils.isNotBlank(noteDataDto.getType()) && "wer".equals(noteDataDto.getType())) {
                noteLuceneIndex.setContent(noteDataDto.getTextContent());
            } else {
                noteLuceneIndex.setContent(noteData.getContent());
            }
            noteLuceneIndex.setIsFile(noteMeta1.getIsFile());
            noteLuceneIndex.setType(noteMeta1.getType());
            noteLuceneIndex.setCreateDate(opTime);
            noteLuceneIndex.setEncrypted(noteMeta1.getEncrypted());
            noteDataIndexService.update(noteLuceneIndex);
            //版本记录
            NoteDataVersion dataVersion = new NoteDataVersion();
            dataVersion.setNoteId(id);
            dataVersion.setContent(noteData.getContent());
            dataVersion.setUserId(noteData.getUserId());
            dataVersion.setCreateTime(opTime);
            noteDataVersionMapper.insertSelective(dataVersion);
        } catch (Exception e) {
            if (objId != null) {
                mongoTemplate.remove(new Document("_id", objId), NoteConstants.noteWerTextContent);
            }
            if (oldDoc != null) {
                mongoTemplate.save(oldDoc, NoteConstants.noteWerTextContent);
            }
            log.error("addAndUpdate异常", e);
            throw new RuntimeException(e);
        }

    }

    private static final String [] ILLEGAL_LIST = {
            "<p><br></p>",
            "<p style=\"text-align: start;\"><br></p>"
    };
    private boolean checkContent(String content) {
        if (content == null || StringUtils.isBlank(content)) {
            return true;
        }
        for(String illegalStr : ILLEGAL_LIST) {
            if (content.equals(illegalStr)) {
                return true;
            }
        }

        return false;
    }

    private static final String [] SUPPORT_View_FILE = {
            "md","txt","java","xml","go","html","css","js","ts","vue","json","c","scala","yml",
            "cpp","py","bash","conf","ini"
    };

    private final ConcurrentHashMap<Long, Boolean> canPreviewCache = new ConcurrentHashMap<>();

    /**
     * 集成到了PreviewNoteType, 见NotePreview接口
     * @param id
     * @return
     */
    @Deprecated
    public boolean checkFileCanPreviewByCache(Long id) {
        Boolean canPreview = canPreviewCache.get(id);
        if ( canPreview != null) {
            return canPreview;
        }
        canPreview = checkFileCanPreview(id);
        canPreviewCache.put(id, canPreview);
        return canPreview;
    }

    /**
     * 检查当前文件是否可预览
     * @param id
     * @return
     */
    private boolean checkFileCanPreview(Long id) {
        NoteMeta noteMeta = noteMetaService.findOne(id);
        //1. 先通过noteIndex的f_type判断是否在 SUPPORT_View_FILE 列表中
        for (String st : SUPPORT_View_FILE) {
            if (st.equals(noteMeta.getType())) {
                return true;
            }
        }
        //2. 不在的话再去通过内容判断是否为文本。
        //todo 哎，这个判断算法还有问题，后续在看
        if (!NoteConstants.MONGO.equals(noteMeta.getStoreSite())) {
            log.debug("查询的文件id={}, 未存储在mongo上", id);
            //目前都是存储在mongo上的,
            return false;
        }

        AnyFile anyFile = fileStoreService.loadFile(noteMeta.getSiteId());
        if (anyFile.getLength() == 0L) {
            log.debug("文件id={}, 为空文件", id);
            return false;
        }

        int bufferSize = 512;  // 读取前512字节来判断
        byte[] buffer = new byte[bufferSize];
        try(InputStream is = anyFile.getInputStream()) {
            int rLen = is.read(buffer);
            for (int i = 0; i < rLen; i++) {
                byte b = buffer[i];
                if (b < 0x09 || (b > 0x0A && b < 0x20) || b > 0x7E) {
                    return false;  // 如果发现不可打印字符，则不是文本文件
                }
            }
        } catch (Exception ee) {
            return false;
        }
        return true;
    }


    /**
     * 过期与20240927, 请使用 NoteStoreService#findOne
     * @param id
     * @return
     */
    @Deprecated
    public NoteData findOne(Long id) {
        NoteMeta noteMeta = noteMetaService.findOne(id);
        NoteData noteData = new NoteData();
        if (NoteConstants.MYSQL.equals(noteMeta.getStoreSite())) {
            noteData = findOneByPk(id);
        } else {
            if (FileTypeEnum.MINDMAP.compare(noteMeta.getType())) {
                NoteData res = new NoteData();
                Document resDoc = mongoTemplate.findById(noteMeta.getSiteId(), Document.class, noteMindMap);
                if (resDoc == null) {
                    return null;
                }
                res.setUserId(LocalThreadUtils.getUserId());
                res.setId(id);
                res.setContent(resDoc.toJson());
                return res;
            }
            //前提,当前文件要可预览, 目前使用markdown预览
            //因此 文本内容前后加了 " ```xxx  内容  ```` "
            if (!checkFileCanPreviewByCache(id)) {
                throw new BusinessException(NoteIndexErrorCode.E_203113);
            }
            AnyFile anyFile = fileStoreService.loadFile(noteMeta.getSiteId());

            StringBuilder contentStr = new StringBuilder("```");
            contentStr.append(noteMeta.getType()).append("\n");
            try(InputStream is = anyFile.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                int bufLen = 1024;
                char [] cBuf = new char[bufLen];
                int rLen = 0;
                while ((rLen = isr.read(cBuf)) > 0) {
                    contentStr.append(new String(cBuf, 0, rLen));
                }
            }catch (Exception e) {
                log.error("读取mongo文件内容出错", e);
            }
            contentStr.append("\n```");
            noteData.setId(id);
            noteData.setContent(contentStr.toString());
        }
        return noteData;
    }

    
    @Transactional(propagation= Propagation.REQUIRED , rollbackFor = Throwable.class, timeout = 20)
    public void syncDataSize() {
        Long uid = LocalThreadUtils.getUserId();
        noteMetaMapper.selectByExample(NoteIndexQuery.Builder.build().uid(uid).filter(3).storeSite(NoteConstants.MYSQL).get().example())
                .forEach(index -> {
                    Long id = index.getId();
                    NoteData noteData = findOneByPk(id);
                    if (noteData != null) {
                        NoteMeta upIndex = new NoteMeta();
                        upIndex.setId(id);
                        upIndex.setSize((long)noteData.getContent().getBytes(StandardCharsets.UTF_8).length);
                        noteMetaService.update(upIndex);
                    }

                });
        noteMetaMapper.selectByExample(NoteIndexQuery.Builder.build().uid(uid).filter(3).storeSite(NoteConstants.MONGO).get().example())
                .forEach(index -> {
                    Long id = index.getId();
                    String fileId = index.getSiteId();
                    NoteFile noteFile = noteFileMapper.selectByExample(NoteFileQuery.Builder.build().fileId(fileId).get().example()).get(0);

                    if (noteFile != null) {
                        NoteMeta upIndex = new NoteMeta();
                        upIndex.setId(id);
                        upIndex.setSize(noteFile.getSize());
                        noteMetaService.update(upIndex);
                    }
                });
    }


    @Override
    public List<NoteDataVersion> findDataVersionList(Long noteId) {
        return noteDataVersionMapper.selectByNoteId(noteId);
    }

    @Override
    public void deleteDataVersion(Long id) {
        noteDataVersionMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void update(NoteData noteData) {
        //更新缓存
        cacheService.hDel(NoteCacheKey.NOTE_DATA_LIST_KEY, noteData.getId().toString());
        //更新db
        noteDataMapper.updateByPrimaryKeySelective(noteData);
    }
}
