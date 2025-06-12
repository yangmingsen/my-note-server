package top.yms.note.conpont.fetch;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import top.yms.note.comm.NoteConstants;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteIndex;
import top.yms.note.exception.BusinessException;
import top.yms.note.mapper.NoteIndexMapper;
import top.yms.note.msgcd.CommonErrorCode;
import top.yms.note.service.NoteDataService;
import top.yms.note.utils.IdWorker;
import top.yms.note.utils.LocalThreadUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public abstract class AbstractNoteFetch implements NoteFetch {
    private static  final Logger log = LoggerFactory.getLogger(AbstractNoteFetch.class);
    @Resource
    protected RestTemplate restTemplate;

    @Value("${note.fetch.req-host}")
    private String fetchReqHost;

    @Resource
    protected IdWorker idWorker;

    @Resource
    protected NoteIndexMapper noteIndexMapper;

    @Resource
    protected NoteDataService noteDataService;

    public static class FetchMeta {
        private String url;
        private Long parentId;
        private NoteIndex noteIndex;
        private String content;

        public FetchMeta(String url, Long parentId) {
            this.url = url;
            this.parentId = parentId;
        }

        public FetchMeta(String url) {
            this.url = url;
        }
        public FetchMeta() {
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Long getParentId() {
            return parentId;
        }

        public void setParentId(Long parentId) {
            this.parentId = parentId;
        }

        public NoteIndex getNoteIndex() {
            return noteIndex;
        }

        public void setNoteIndex(NoteIndex noteIndex) {
            this.noteIndex = noteIndex;
        }
    }

    protected String getFetchReqHost() {
        return fetchReqHost;
    }



    abstract String toType();

    @Override
    public boolean supportFetch(String type) {
        return false;
    }

    public Long fetch(FetchMeta fetchMeta) {
        if (StringUtils.isBlank(fetchMeta.getUrl())) {
            log.error("fetchMeta url must be not null");
            throw new BusinessException(CommonErrorCode.E_200202);
        }
        if (!beforeFetch(fetchMeta)) {
            return null;
        }
        Long noteId = doFetch(fetchMeta);
        afterFetch(fetchMeta);
        return noteId;
    }

    @Override
    public Long fetch(String url, Long parentId) {
        FetchMeta fetchMeta = new FetchMeta(url, parentId);
        return fetch(fetchMeta);
    }

    protected boolean beforeFetch(FetchMeta fetchMeta) {
        if (fetchMeta.getNoteIndex() == null) {
            Long uid = (Long) LocalThreadUtils.get().get(NoteConstants.USER_ID);
            long id = idWorker.nextId();
            NoteIndex noteMeta = new NoteIndex();
            noteMeta.setId(id);
            noteMeta.setParentId(fetchMeta.getParentId());
            noteMeta.setUserId(uid);
            noteMeta.setName(id+"");
            noteMeta.setIsFile(NoteConstants.FILE_FLAG);
            noteMeta.setType(toType());
            noteMeta.setCreateTime(new Date());
            noteMeta.setStoreSite(NoteConstants.MYSQL);
            //插入meta
            noteIndexMapper.insertSelective(noteMeta);
            fetchMeta.setNoteIndex(noteMeta);
        }
        return true;
    }

    abstract Long doFetch(FetchMeta fetchMeta);

    protected void afterFetch(FetchMeta fetchMeta) {
        NoteIndex noteMeta = fetchMeta.getNoteIndex();
        //插入到data
        String content = fetchMeta.getContent();
        NoteData noteData = new NoteData();
        noteData.setId(fetchMeta.getNoteIndex().getId());
        noteData.setUserId(fetchMeta.getNoteIndex().getUserId());
        noteData.setContent(content);
        noteData.setCreateTime(new Date());
        noteData.setUpdateTime(new Date());
        //保存
        noteDataService.save(noteData);
        //更新大小
        noteMeta.setSize((long)content.getBytes(StandardCharsets.UTF_8).length);
        //再更新一次
        noteIndexMapper.updateByPrimaryKeySelective(noteMeta);
    }

}
