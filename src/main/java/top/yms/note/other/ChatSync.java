package top.yms.note.other;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import top.yms.note.comm.NoteConstants;
import top.yms.note.entity.*;

import top.yms.note.mapper.NoteDataMapper;
import top.yms.note.mapper.NoteMetaMapper;
import top.yms.note.mapper.NoteUserMapper;
import top.yms.note.repo.ChatNoteRepository;
import top.yms.note.service.NoteDataService;
import top.yms.note.service.NoteMetaService;
import top.yms.note.utils.DateHelper;
import top.yms.note.utils.IdWorker;
import top.yms.note.utils.LocalThreadUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ChatSync  {

    private static final Logger log = LoggerFactory.getLogger(ChatSync.class);

    @Resource
    private NoteMetaMapper noteMetaMapper;

    @Resource
    private NoteDataMapper noteDataMapper;

    @Resource
    private IdWorker idWorker;

    @Resource
    private NoteMetaService noteMetaService;

    @Resource
    private NoteUserMapper noteUserMapper;

    @Resource
    private ChatNoteRepository chatNoteRepository;

    @Value("${chat.data-path}")
    private String chatNoteDataPath;

    private String getChatNoteDataPath() {
        return chatNoteDataPath;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class, timeout = 60)
    public void getChatNote() {
        List<ChatMarkdownResult> chatMarkdownResults = null;
        try {
            File file = new File(getChatNoteDataPath());
            chatMarkdownResults = parse(file);
        } catch (Exception e) {
            log.error("GetChatNote error", e);
        }
        if (chatMarkdownResults == null) return;
        log.info("size={}", chatMarkdownResults.size());
//        List<ChatMarkdownResult> newList = chatMarkdownResults.subList(0, 10);
        //get parent dir
        String defaultName = "Chat";
        NoteMetaExample example = new NoteMetaExample();
        NoteMetaExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(defaultName);
        List<NoteMeta> noteMetas = noteMetaMapper.selectByExample(example);
        Long defaultParentId;
        Long userId = LocalThreadUtils.getUserId();
        if (noteMetas.isEmpty()) {
            NoteUser noteUser = noteUserMapper.selectByPrimaryKey(userId);
            defaultParentId = noteMetaService.createParentDir(defaultName, noteUser.getNoteRootTreeId()).getId();
        } else {
            defaultParentId = noteMetas.get(0).getId();
        }
        Map<String, NoteMeta> metaNameMap = noteMetaService.findNoteMetaByParentId(defaultParentId)
                .stream()
                .filter(note -> NoteConstants.DIR_FLAG.equals(note.getIsFile()))
                .collect(Collectors.toMap(NoteMeta::getName, note -> note));
        for (ChatMarkdownResult cmr : chatMarkdownResults) {
            Date date = cmr.createTime;
            String dateStr = DateHelper.dateStrMatch(date, DateHelper.PATTERN3);
            NoteMeta parentNoteMeta = metaNameMap.get(dateStr);
            if (parentNoteMeta == null) {
                //create new dateDir
                NoteMeta newParentDir = noteMetaService.createParentDir(dateStr, defaultParentId);
                metaNameMap.put(newParentDir.getName(), newParentDir);
                parentNoteMeta = newParentDir;
            }
            ChatNote oldChatNote = chatNoteRepository.findByChatId(cmr.id);
            long noteId = idWorker.nextId();
            if (oldChatNote == null) {
                Long pid = parentNoteMeta.getId();
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
                noteMetaMapper.insertSelective(noteMeta);
                NoteData noteData = new NoteData();
                noteData.setId(noteId);
                noteData.setContent(cmr.markdownContent);
                noteData.setUserId(userId);
                noteData.setCreateTime(date);
                noteData.setUpdateTime(date);
                noteDataMapper.insertSelective(noteData);
                //relation
                ChatNote chatNote = new ChatNote();
                chatNote.setNoteId(noteId);
                chatNote.setChatId(cmr.id);
                chatNote.setTitle(cmr.title);
                chatNote.setCreateTime(date);
                chatNote.setUpdateTime(cmr.updateTime);
                chatNoteRepository.save(chatNote);
            } else {
                //note data
                noteId = oldChatNote.getNoteId();
                NoteData noteData = new NoteData();
                noteData.setId(noteId);
                noteData.setContent(cmr.markdownContent);
                noteData.setUpdateTime(new Date());
                noteDataMapper.updateByPrimaryKeySelective(noteData);
            }

        }
        log.info("sync ok=================");
    }


    private static Date fromUnix(double unixSeconds) {
        return new Date((long)(unixSeconds * 1000));
    }

    public static List<ChatMarkdownResult> parse(File jsonArrayFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootArray = mapper.readTree(jsonArrayFile);
        List<ChatMarkdownResult> resultList = new ArrayList<>();

        for (JsonNode chatNode : rootArray) {
            String id = chatNode.path("id").asText();
            String title = chatNode.path("title").asText("对话记录");
            Date createTime = fromUnix(chatNode.path("create_time").asDouble(0));
            Date updateTime = fromUnix(chatNode.path("update_time").asDouble(0));
            JsonNode mapping = chatNode.path("mapping");

            Map<String, Message> messages = new HashMap<>();
            for (Iterator<Map.Entry<String, JsonNode>> it = mapping.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();
                JsonNode msgNode = entry.getValue().get("message");
                if (msgNode == null) continue;
                String role = msgNode.path("author").path("role").asText();
                JsonNode parts = msgNode.path("content").path("parts");
                String content = (parts != null && parts.size() > 0) ? parts.get(0).asText() : "";
                double msgTime = msgNode.has("create_time") ? msgNode.get("create_time").asDouble(0) : 0;
                String msgId = msgNode.path("id").asText();
                messages.put(msgId, new Message(msgId, role, content, msgTime));
            }

            String markdown = generateMarkdown(mapping, messages, title);
            resultList.add(new ChatMarkdownResult(id, title, createTime, updateTime, markdown));
        }

        return resultList;
    }

    private static String generateMarkdown(JsonNode mapping, Map<String, Message> messages, String title) {
        StringBuilder sb = new StringBuilder();
        List<String> stack = new ArrayList<>();
        if (mapping.has("client-created-root")) {
            for (JsonNode child : mapping.get("client-created-root").path("children")) {
                stack.add(child.asText());
            }
        } else { //fix bug 202506181156 发现部分不以`client-created-root`作为头节点的
            for (Iterator<Map.Entry<String, JsonNode>> it = mapping.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();
                JsonNode parentNode = entry.getValue().get("parent");
                if (parentNode.isNull()) {
                    for (JsonNode child : entry.getValue().path("children")) {
                        stack.add(child.asText());
                    }
                }
            }
        }

        while (!stack.isEmpty()) {
            String nodeId = stack.remove(0);
            Message msg = messages.get(nodeId);
            if (msg != null && msg.text != null && !msg.text.trim().isEmpty()) {
                if ("user".equals(msg.role)) {
                    String userContent = msg.text;
                    sb.append("## 🤔 用户提问：\n\n")
                            .append("时间: ").append(DateHelper.dateStrMatch(fromUnix(msg.createTime), DateHelper.PATTERN4)).append("\n\n")
                            .append(userContent)
                            .append("\n\n");
                } else if ("assistant".equals(msg.role)) {
                    sb.append("### 💡 ChatGPT 回答：\n\n").append(msg.text).append("\n\n---\n\n");
                }
            }
            JsonNode children = mapping.path(nodeId).path("children");
            if (children.isArray()) {
                for (JsonNode child : children) {
                    stack.add(child.asText());
                }
            }
        }

        return sb.toString().isEmpty() ? "# 空对话\n" : "# " + title + "\n\n" + sb.toString();
    }


    private static class Message {
        String role;
        String text;
        String id;
        double createTime;

        public Message(String id, String role, String text, double createTime) {
            this.id = id;
            this.role = role;
            this.text = text;
            this.createTime = createTime;
        }
    }

    private static class ChatMarkdownResult {
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
    }

}
