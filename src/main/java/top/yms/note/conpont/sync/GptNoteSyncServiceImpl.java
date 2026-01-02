package top.yms.note.conpont.sync;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.yms.note.entity.NoteMeta;
import top.yms.note.entity.NoteMetaExample;
import top.yms.note.utils.DateHelper;
import top.yms.note.utils.LocalThreadUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Component
public class GptNoteSyncServiceImpl extends AbstractChatNoteSyncService {

    private static final Logger log = LoggerFactory.getLogger(GptNoteSyncServiceImpl.class);

    @Value("${chat.data-gpt-path}")
    private String chatNoteDataPath;

    private String defaultDirName = "GPT Chat";

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

    @Override
    protected String getChatNoteDataPath() {
        return chatNoteDataPath;
    }

    protected String getDefaultDirName() {
        return defaultDirName;
    }

    @Override
    protected Long getDefaultStoreParentId(Long parentId) {
        return getDefaultAndCreateDirName(parentId);
    }

    private static Date fromUnix(double unixSeconds) {
        return new Date((long)(unixSeconds * 1000));
    }

    public  List<ChatMarkdownResult> parse(File jsonArrayFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootArray = mapper.readTree(jsonArrayFile);
        List<ChatMarkdownResult> resultList = new ArrayList<>();
        for (JsonNode chatNode : rootArray) {
            try {
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
            } catch (Exception e) {
                log.error("generateMarkdown error", e);
            }
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
                //=================================================
                //bug 20250620 不想用alibaba 的 JSONObject的， 可惜JsonNode太难用
                String content = msg.text;
                if (content.startsWith("{")) {
                    try {
                        JSONObject jsonObject = JSONObject.parseObject(content);
                        JSONArray updates = jsonObject.getJSONArray("updates");
                        if (updates != null) {
                            int size = updates.size();
                            for (int i=0; i<size; i++) {
                                JSONObject js1 = updates.getJSONObject(i);
                                String replacement = js1.getString("replacement");
                                content = replacement;
                            }
                        }
                        String prompt = jsonObject.getString("prompt");
                        if (StringUtils.isNoneBlank(prompt)) {
                            content = prompt;
                        }
                    } catch (Exception e) {
                        log.error("parse error", e);
                    }
                }
                //===================================================
                if ("user".equals(msg.role)) {
                    sb.append("## 🤔 用户提问：\n\n")
                            .append("时间: ").append(DateHelper.dateStrMatch(fromUnix(msg.createTime), DateHelper.PATTERN4)).append("\n\n")
                            .append(content)
                            .append("\n\n");
                } else if ("assistant".equals(msg.role)) {
                    sb.append("### 💡 ChatGPT 回答：\n\n").append(content).append("\n\n---\n\n");
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

}
