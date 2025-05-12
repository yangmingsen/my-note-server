package top.yms.note.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import top.yms.note.conpont.NoteCacheService;
import top.yms.note.msgcd.BusinessErrorCode;
import top.yms.note.msgcd.CommonErrorCode;
import top.yms.note.comm.NoteConstants;
import top.yms.note.msgcd.NoteIndexErrorCode;
import top.yms.note.conpont.NoteAsyncExecuteTaskService;
import top.yms.note.conpont.task.AsyncTask;
import top.yms.note.dao.NoteIndexQuery;
import top.yms.note.dto.NoteListQueryDto;
import top.yms.note.dto.NoteMoveDto;
import top.yms.note.dto.NoteSearchCondition;
import top.yms.note.entity.*;
import top.yms.note.enums.AsyncExcuteTypeEnum;
import top.yms.note.enums.AsyncTaskEnum;
import top.yms.note.exception.BusinessException;
import top.yms.note.service.NoteIndexService;
import top.yms.note.utils.IdWorker;
import top.yms.note.utils.LocalThreadUtils;
import top.yms.note.vo.MenuListVo;
import top.yms.note.vo.NoteIndexExtVo;
import top.yms.note.vo.NoteInfoVo;
import top.yms.note.vo.NoteSearchVo;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by yangmingsen on 2024/3/30.
 */
@RestController
@RequestMapping("/note-index")
public class NoteIndexController {

    private final static Logger log = LoggerFactory.getLogger(NoteIndexController.class);

    @Resource
    private NoteIndexService noteIndexService;

    @Resource
    private IdWorker idWorker;

    @Resource
    private NoteAsyncExecuteTaskService noteExecuteTaskService;

    @Qualifier(NoteConstants.noteExpireTimeCache)
    @Resource
    private NoteCacheService noteExpireCacheService;

    @GetMapping("/list")
    public RestOut<List<NoteIndex>> findByUid() {
        Long uid = (Long) LocalThreadUtils.get().get(NoteConstants.USER_ID);
        log.info("findByUid: {}", uid);
        List<NoteIndex> noteList = noteIndexService.findByUserId(uid);
//        log.info("findByUid: {} , count: {}", uid, noteList.size());
        return RestOut.success(noteList);
    }

    @GetMapping("/tree")
    public RestOut findNoteTreeByUid() {
        Long uid = (Long) LocalThreadUtils.get().get(NoteConstants.USER_ID);
        log.info("findNoteTreeByUid: {}", uid);
        List<NoteTree> noteTreeList = noteIndexService.findNoteTreeByUid(uid);
//        log.info("findByUid: {} , count: {}", uid, noteTreeList.size());
        return RestOut.success(noteTreeList);
    }

    @GetMapping("/antTree")
    public RestOut findAntTree() {
        Long uid = (Long) LocalThreadUtils.get().get(NoteConstants.USER_ID);
        log.info("antTree: {}", uid);
        List<AntTreeNode> antTreeList = noteIndexService.findAntTreeExcludeEncrypted(uid);
//        log.info("antTree: {} , count: {}", uid, antTreeList.size());
        return RestOut.success(antTreeList);
    }

    /**
     *  文件列表查询
     *  根据传入的id（这个id是目录id）, 查找其子节点笔记文件/文件夹
     * @return
     */
    @PostMapping("/sub")
    public RestOut<List<NoteIndex>> findSubBy(NoteListQueryDto noteListQueryDto) {
        log.info("findSubBy_noteListQueryDto:{}", noteListQueryDto);
        Long uid = (Long) LocalThreadUtils.get().get(NoteConstants.USER_ID);
        Long parentId = noteListQueryDto.getParentId();
        if (parentId == null) {
            throw new BusinessException(CommonErrorCode.E_100101);
        }
//        log.info("findSubBy: uid= {}, parentId={}", uid, parentId);
        List<NoteIndex> resList =  handleSortBy(noteListQueryDto, noteIndexService.findSubBy(parentId, uid));
//        log.info("findSubBy: uid= {}, parentId={}, count:{}", uid, parentId, resList.size());
        return RestOut.success(resList);
    }

    /**
     * 控制排序规则
     * @param noteListQueryDto
     * @param list
     * @return
     */
    private List<NoteIndex> handleSortBy(NoteListQueryDto noteListQueryDto, List<NoteIndex> list) {
        return list.stream().sorted((o1,o2) -> {
            if (noteListQueryDto.getSortBy() == 0) {
                if (noteListQueryDto.getAsc() == 0) {
                    return o2.getCreateTime().compareTo(o1.getCreateTime());
                }
                return o1.getCreateTime().compareTo(o2.getCreateTime());
            } else if (noteListQueryDto.getSortBy() == 1) {
                Date t1 = o1.getUpdateTime() != null ? o1.getUpdateTime(): o1.getCreateTime();
                Date t2 = o2.getUpdateTime() != null ? o2.getUpdateTime(): o2.getCreateTime();
                if (noteListQueryDto.getAsc() == 0) {
                    return t2.compareTo(t1);
                }
                return t1.compareTo(t2);
            } else if (noteListQueryDto.getSortBy() == 2) {
                if (noteListQueryDto.getAsc() == 0) {
                    return o2.getName().compareTo(o1.getName());
                }
                return o1.getName().compareTo(o2.getName());
            } else if (noteListQueryDto.getSortBy() == 3) {
                if (noteListQueryDto.getAsc() == 0) {
                    return o2.getSize().compareTo(o1.getSize());
                }
                return o1.getSize().compareTo(o2.getSize());
            } else {
                //增加对访问时间的排序，规则为，如果访问时间不为空就就用访问时间, 如果空就用updateTime, 如果updateTime空，就用createTime
                Date t1 = o1.getViewTime() != null ? o1.getViewTime() : o1.getUpdateTime() != null ? o1.getUpdateTime(): o1.getCreateTime();
                Date t2 = o2.getViewTime() != null ? o2.getViewTime() : o2.getUpdateTime() != null ? o2.getUpdateTime(): o2.getCreateTime();
                if (noteListQueryDto.getAsc() == 0) {
                    return t2.compareTo(t1);
                }
                return t1.compareTo(t2);
            }
        }).collect(Collectors.toList());
    }

    @GetMapping("/findRoot")
    public RestOut<NoteIndex> findRootNoteIndex() {
        return RestOut.success(noteIndexService.findRoot());
    }

    /**
     * 需求： 从子层返回上一层。
     * <p>使用当前层的id找到parentId, 然后根据parentId找到所有该parentId下面的子节点</p>
     * @param id
     * @return
     */
    @GetMapping("/findBackParentDir")
    public RestOut<List<NoteIndex>> findBackParentDir(@RequestParam("id") Long id) {
        if (id == null) {
            throw new BusinessException(NoteIndexErrorCode.E_203104);
        }
        List<NoteIndex> resList =  noteIndexService.findBackParentDir(id);
        log.info("findBackParentDir: id= {},  count:{}", id, resList.size());

        return RestOut.success(resList);
    }

    @GetMapping("/findOne")
    public RestOut<NoteIndex> findOne(@RequestParam("id") Long id) {
        if (id == null) {
            throw new BusinessException(NoteIndexErrorCode.E_203104);
        }
        NoteIndex res = noteIndexService.findOne(id);
//        log.info("findOne: id= {},  count:{}", id, res);

        return RestOut.success(res);
    }

    @PostMapping("/findNoteByCondition")
    public RestOut<NoteSearchVo> findNoteByCondition(@RequestBody NoteSearchCondition searchCondition) {
        log.info("findNoteByCondition: condition={}", searchCondition);
        if(searchCondition == null) {
            return RestOut.success(NoteSearchVo.getEmpty());
        }
        if (StringUtils.isBlank(searchCondition.getSearchContent())) {
            return RestOut.success(NoteSearchVo.getEmpty());
        }
        return RestOut.success(noteIndexService.findNoteByCondition(searchCondition));
    }


    /**
     * 没有用到了
     * @param nid
     * @return
     */
    @Deprecated
    @GetMapping("/menuList")
    public RestOut<MenuListVo> findMenuList(@RequestParam("nid") Long nid) {
        //0->dir(menu); 1->file(content)
        Map<String, List<NoteIndex>>  mapList = Optional.ofNullable(noteIndexService.findSubBy(nid, LocalThreadUtils.getUserId())).orElse(Collections.emptyList()).stream().collect(Collectors.groupingBy(NoteIndex::getIsFile));
        MenuListVo res = new MenuListVo();
        res.setMenuList(mapList.get("0"));
        res.setNoteContentMenuList(mapList.get("1"));

        return RestOut.success(res);
    }

    @PostMapping("add")
    public RestOut<NoteIndex> add(@RequestBody NoteIndex note) {
        Long uid = (Long) LocalThreadUtils.get().get(NoteConstants.USER_ID);
        note.setUserId(uid);
        log.info("add: {}", note);
        if (StringUtils.isBlank(note.getName())) {
            throw new BusinessException(NoteIndexErrorCode.E_203100);
        }
        if (StringUtils.isBlank(note.getIsFile())) {
            throw new BusinessException(NoteIndexErrorCode.E_203102);
        }
        if ("1".equals(note.getIsFile()) && StringUtils.isBlank(note.getType())) {
            throw new BusinessException(NoteIndexErrorCode.E_203103);
        }
        noteIndexService.add(note);

        return RestOut.success(note);
    }

    @PostMapping("/update")
    public RestOut<String> update(@RequestBody NoteIndex note) {
        Long uid = (Long) LocalThreadUtils.get().get(NoteConstants.USER_ID);
        note.setUserId(uid);
        log.info("update: {}", note);
        if (note == null) {
            throw new BusinessException(CommonErrorCode.E_200202);
        }
        if (note.getId() == null) {
            throw new BusinessException(CommonErrorCode.E_203001);
        }
        noteIndexService.update(note);
        return RestOut.succeed("ok");
    }


    @GetMapping("/delDir")
    public RestOut<String> delDir(@RequestParam("parentId") Long parentId) {
        log.info("delDir: parentId={}", parentId);
        if (parentId == null) {
            throw new BusinessException(CommonErrorCode.E_203000);
        }
        noteIndexService.delDir(parentId);
        return RestOut.succeed("ok");
    }


    @PostMapping("/delNote")
    public RestOut<String> delNote(@RequestBody NoteIndex note) {
        log.info("delNote: {}", note);
        Long uid = (Long) LocalThreadUtils.get().get(NoteConstants.USER_ID);
        note.setUserId(uid);
        if (note.getId() == null) {
            throw new BusinessException(CommonErrorCode.E_203001);
        }

        noteIndexService.delNote(note.getId());
        return RestOut.succeed("ok");
    }

    @GetMapping("/destroyNote")
    public RestOut<String> destroyNote(@RequestParam("id") Long id) {
        log.info("destroyNote id= {}", id);
        noteIndexService.destroyNote(id);
        return RestOut.succeed("ok");
    }

    @PostMapping("/findBy")
    public RestOut<List<NoteIndex>> findBy(@RequestBody NoteIndexQuery query) {
        log.info("findBy: {}", query);
        if (query == null) {
            throw new BusinessException(CommonErrorCode.E_100101);
        }
        List<NoteIndex> resList = noteIndexService.findBy(query);

//        log.info("findBy Result: 共{}条", resList.size());
        return RestOut.success(resList);
    }

    /**
     * 传入一个父目录id
     * @param id
     * @return
     */
    @GetMapping("/findBreadcrumb")
    public RestOut<List<NoteIndex>> findBreadcrumb (@RequestParam("id") Long id) {
        log.info("findBreadcrumb: id={}", id);
        if (id == null) {
            throw new BusinessException(CommonErrorCode.E_203001);
        }
        List<NoteIndex> res = new LinkedList<>();
        noteIndexService.findBreadcrumb(id, res);
//        log.info("findBreadcrumb Result: 共{}条", res.size());
        return RestOut.success(res);
    }

    @GetMapping("/getNoteAndSite")
    public RestOut<NoteInfoVo> getNoteAndSite(@RequestParam("id") Long id) {
        if (id == null) {
            throw new BusinessException(CommonErrorCode.E_203001);
        }
        NoteInfoVo res = noteIndexService.getNoteAndSite(id);
        return RestOut.success(res);
    }

    @PostMapping("/getRecentFiles")
    public RestOut<List<NoteIndex>> getRecentFiles(NoteListQueryDto noteListQueryDto) {
        log.info("getRecentFiles => {}", noteListQueryDto);
        List<NoteIndex> resList = handleSortBy(noteListQueryDto, noteIndexService.getRecentFiles());
//        log.info("getRecentFiles Result: 共{}条", resList.size());

        return RestOut.success(resList);
    }

    @PostMapping("/getDeletedFiles")
    public RestOut<List<NoteIndex>> getDeletedFiles(NoteListQueryDto noteListQueryDto) {
        log.info("getDeletedFiles => {}", noteListQueryDto);
        List<NoteIndex> resList = handleSortBy(noteListQueryDto, noteIndexService.getDeletedFiles());
//        log.info("getDeletedFiles Result: 共{}条", resList.size());

        return RestOut.success(resList);
    }

    @GetMapping("/recentVisitList")
    public RestOut recentVisitList() {
        List<NoteIndex> recentVisits = noteIndexService.recentVisitList();
        return RestOut.success(recentVisits);
    }

    @GetMapping("/treeClick")
    public RestOut treeClick(@RequestParam("id") Long id) {
        NoteIndex noteMeta = noteIndexService.findOne(id);
        AsyncTask visitComputeTask = AsyncTask.Builder
                .build()
                .taskId(idWorker.nextId())
                .taskName(AsyncTaskEnum.SYNC_COMPUTE_RECENT_VISIT.getName())
                .createTime(new Date())
                .userId(LocalThreadUtils.getUserId())
                .type(AsyncTaskEnum.SYNC_COMPUTE_RECENT_VISIT)
                .executeType(AsyncExcuteTypeEnum.SYNC_TASK)
                .taskInfo(noteMeta)
                .get();
        noteExecuteTaskService.addTask(visitComputeTask);
        return RestOut.success("Ok");
    }


    @GetMapping("/allDestroy")
    public RestOut allDestroy() {
        int cnt = noteIndexService.allDestroy();
        log.info("allDestroy: {}", cnt);
        return RestOut.succeed("OK");
    }

    @GetMapping("/allRecover")
    public RestOut allRecover() {
        int cnt = noteIndexService.allRecover();
        log.info("allRecover: {}", cnt);
        return RestOut.succeed("OK");
    }

    /**
     * 笔记之间的移动
     * 比如笔记A移动到目录C下
     */
    @PostMapping("/updateMove")
    public RestOut updateMove(NoteMoveDto noteMoveDto) {
        log.info("updateMove: {}", noteMoveDto);
        if (noteMoveDto.getFromId() == null || noteMoveDto.getToId() == null) {
            throw new BusinessException(CommonErrorCode.E_200202);
        }
        noteIndexService.updateMove(noteMoveDto);
        return RestOut.succeed("ok");
    }

    /**
     * 加密笔记或文件夹验证接口
     * @param id
     * @param password
     * @return
     */
    @PostMapping("/note-pass-auth")
    public RestOut<NoteIndexExtVo> notePasswordAuth(@RequestParam("id") Long id,
                                                    @RequestParam("password") String password) {
        log.debug("notePasswordAuth: id={}, password={}", id, password);
        //todo 去做密码验证， 暂时先不验证，因为密码还不知道存哪里
        if (StringUtils.isBlank(password) || id == null) {
            throw new BusinessException(CommonErrorCode.E_200202);
        }
        Long userId = LocalThreadUtils.getUserId();
        NoteUser noteUser = (NoteUser) LocalThreadUtils.get().get(userId.toString());
        String srcStr = noteUser.getId().toString()+"_"+password;
        String encryptedStr = DigestUtils.md5DigestAsHex(srcStr.getBytes(StandardCharsets.UTF_8));
        if (encryptedStr.equals(noteUser.getPassword())) {
            NoteIndex noteIndex = noteIndexService.findOne(id);
            NoteIndexExtVo resVo = new NoteIndexExtVo();
            BeanUtils.copyProperties(noteIndex, resVo);
            String tmpTokenKey = NoteConstants.TMP_VISIT_TOKEN+id;
            String tmpToken = UUID.randomUUID().toString();
            resVo.setTmpToken(tmpToken);
            noteExpireCacheService.update(tmpTokenKey, tmpToken);
            return RestOut.success(resVo);
        } else {
            throw new BusinessException(BusinessErrorCode.E_204006);
        }

    }

    /**
     * 设置当前笔记需要阅读密码
     * @param id
     * @return
     */
    @GetMapping("/encrypted-read-note")
    public RestOut<String> encryptedReadNote(@RequestParam("id") Long id) {
        noteIndexService.encryptedReadNote(id);
        return RestOut.succeed("ok");
    }

    /**
     * 解除加密
     * @param id
     * @param password
     * @return
     */
    @PostMapping("/unencrypted-read-note")
    public RestOut<NoteIndexExtVo> unEncryptedReadNote(@RequestParam("id") Long id,
                                                       @RequestParam("password") String password) {
        RestOut<NoteIndexExtVo> authResult = notePasswordAuth(id, password);
        if (!authResult.isSuccess()) {
            return authResult;
        }
        noteIndexService.unEncryptedReadNote(id);
        return authResult;
    }

    //@GetMapping("/autoScanEncrypt")
    public RestOut<String> autoScanEncrypt() {
         noteIndexService.autoScanEncrypt();
         return RestOut.succeed("ok");
    }

    @GetMapping("/auto-decrypted-all-note")
    public RestOut<String> autoDecryptedAllNote() {
        noteIndexService.autoDecryptedAllNote();
        return RestOut.succeed("ok");
    }

}
