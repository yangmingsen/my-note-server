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

import top.yms.note.mapper.NoteMetaMapper;
import top.yms.note.mapper.NoteUserMapper;
import top.yms.note.repo.ChatNoteRepository;
import top.yms.note.service.NoteDataService;
import top.yms.note.service.NoteMetaService;
import top.yms.note.utils.IdWorker;
import top.yms.note.utils.LocalThreadUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class ChatSync  {

    private static final Logger log = LoggerFactory.getLogger(ChatSync.class);

    @Resource
    private NoteMetaMapper noteMetaMapper;

    @Resource
    private NoteDataService noteDataService;

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
        List<ChatMarkdownResult> newList = chatMarkdownResults.subList(0, 5);
        //get parent dir
        String defaultName = "Chat";
        NoteMetaExample example = new NoteMetaExample();
        NoteMetaExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo("Chat");
        List<NoteMeta> noteMetas = noteMetaMapper.selectByExample(example);
        Long defaultParentId;
        Long userId = LocalThreadUtils.getUserId();
        if (noteMetas.isEmpty()) {
            NoteUser noteUser = noteUserMapper.selectByPrimaryKey(userId);
            defaultParentId = noteMetaService.createParentDir(defaultName, noteUser.getNoteRootTreeId());
        } else {
            defaultParentId = noteMetas.get(0).getId();
        }
        for (ChatMarkdownResult cmr : newList) {
            ChatNote oldChatNote = chatNoteRepository.findByChatId(cmr.id);
            if (oldChatNote != null) {
                log.info("已存在chatNote记录, id={}", cmr.id);
                continue;
            }
            long noteId = idWorker.nextId();
            NoteMeta noteMeta = new NoteMeta();
            noteMeta.setId(noteId);
            noteMeta.setName(cmr.title);
            noteMeta.setParentId(defaultParentId);
            noteMeta.setUserId(userId);
            noteMeta.setIsFile(NoteConstants.FILE_FLAG);
            noteMeta.setType(NoteConstants.markdownSuffix);
            Date date = new Date();
            noteMeta.setCreateTime(date);
            noteMeta.setUpdateTime(date);
            noteMeta.setStoreSite(NoteConstants.MYSQL);
            noteMeta.setSize((long)cmr.markdownContent.getBytes(StandardCharsets.UTF_8).length);
            noteMetaService.add(noteMeta);
            //note data
            NoteData noteData = new NoteData();
            noteData.setId(noteId);
            noteData.setContent(cmr.markdownContent);
            noteData.setUserId(userId);
            noteData.setCreateTime(date);
            noteData.setUpdateTime(date);
            noteDataService.save(noteData);
            //relation
            ChatNote chatNote = new ChatNote();
            chatNote.setNoteId(noteId);
            chatNote.setChatId(cmr.id);
            chatNote.setTitle(cmr.title);
            chatNote.setCreateTime(cmr.createTime);
            chatNote.setUpdateTime(cmr.updateTime);
            chatNoteRepository.save(chatNote);
        }
        log.info("sync ok=================");
    }

    private List<ChatMarkdownResult> doGetChatNote() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootArray = mapper.readTree(new File(getChatNoteDataPath())); // 改为你的路径
        List<ChatMarkdownResult> resultList = new ArrayList<>();
        for (JsonNode chatNode : rootArray) {
            String id = chatNode.path("id").asText();
            long createTime = (long) chatNode.path("create_time").asDouble(0);
            long updateTime = (long) chatNode.path("update_time").asDouble(0);
            String title = chatNode.path("title").asText();
            JsonNode mapping = chatNode.path("mapping");
            // 提取消息（和上一段逻辑一样）
            Map<String, Message> messages = new HashMap<>();
            for (Iterator<Map.Entry<String, JsonNode>> it = mapping.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();
                JsonNode msgNode = entry.getValue().get("message");
                if (msgNode != null && msgNode.has("author") && msgNode.has("content")) {
                    String role = msgNode.get("author").get("role").asText();
                    JsonNode parts = msgNode.get("content").get("parts");
                    String content = (parts != null && parts.size() > 0) ? parts.get(0).asText() : "";
                    double time = msgNode.has("create_time") ? msgNode.get("create_time").asDouble() : 0;
                    String msgId = msgNode.get("id").asText();
                    String parent = entry.getValue().has("parent") ? entry.getValue().get("parent").asText() : null;
                    messages.put(msgId, new Message(role, content, time, msgId, parent));
                }
            }

            // 排序消息
            List<Message> sorted = new ArrayList<>(messages.values());
            sorted.sort(Comparator.comparingDouble(m -> m.time));

            StringBuilder markdown = new StringBuilder("# " + title + "\n\n");
            for (int i = 0; i < sorted.size(); i++) {
                Message m = sorted.get(i);
                if ("user".equals(m.role)) {
                    markdown.append("## 🤔 用户提问：\n\n").append(m.text).append("\n\n");

                    for (int j = i + 1; j < sorted.size(); j++) {
                        Message next = sorted.get(j);
                        if ("assistant".equals(next.role)) {
                            markdown.append("### 💡 ChatGPT 回答：\n\n")
                                    .append(next.text).append("\n\n---\n\n");
                            i = j;
                            break;
                        }
                    }
                }
            }
            double createSeconds = chatNode.path("create_time").asDouble(0);
            double updateSeconds = chatNode.path("update_time").asDouble(0);
            Date createDate = unixSecondToDate(createSeconds);
            Date updateDate = unixSecondToDate(updateSeconds);
            resultList.add(new ChatMarkdownResult(id, createDate, updateDate, title, markdown.toString()));
        }
        return resultList;
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
        }

        while (!stack.isEmpty()) {
            String nodeId = stack.remove(0);
            Message msg = messages.get(nodeId);
            if (msg != null && msg.text != null && !msg.text.trim().isEmpty()) {
                if ("user".equals(msg.role)) {
                    String userContent = msg.text;
                    sb.append("## 🤔 用户提问：\n\n").append(userContent).append("\n\n");
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


    // 工具方法：将秒为单位的时间戳转为 Date
    public static Date unixSecondToDate(double unixSeconds) {
        return new Date((long) (unixSeconds * 1000)); // 秒 → 毫秒
    }


    private static class Message {
        String role;
        String text;
        double time;
        String id;
        String parent;
        double createTime;

        Message(String role, String text, double time, String id, String parent) {
            this.role = role;
            this.text = text;
            this.time = time;
            this.id = id;
            this.parent = parent;
        }

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

        public ChatMarkdownResult(String id, Date createTime, Date updateTime, String title, String content) {
            this.id = id;
            this.createTime = createTime;
            this.updateTime = updateTime;
            this.title = title;
            this.markdownContent = content;
        }
        public ChatMarkdownResult(String id, String title, Date createTime, Date updateTime, String markdownContent) {
            this.id = id;
            this.title = title;
            this.createTime = createTime;
            this.updateTime = updateTime;
            this.markdownContent = markdownContent;
        }
    }


}
