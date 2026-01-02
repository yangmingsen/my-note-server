package top.yms.note.conpont.sync;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.yms.note.entity.NoteMeta;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;

@Component
public class DeepSeekNoteSyncServiceImpl extends AbstractChatNoteSyncService {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekNoteSyncServiceImpl.class);

    private String defaultDirName = "DeepSeek";

    @Value("${chat.data-deepseek-path}")
    private String chatNoteDataPath;

    @Override
    protected String getDefaultDirName() {
        return defaultDirName;
    }

    @Override
    protected String getChatNoteDataPath() {
        return chatNoteDataPath;
    }

    @Override
    protected List<ChatMarkdownResult> parse(File file) throws Exception {
        // 读取文件内容为字符串
        String jsonData = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
        List<ChatMarkdownResult> cmrList = convertConversationsToMarkdown(jsonData);
        log.info("parse {} deepseek doc", cmrList.size());
        return cmrList;
    }

    @Override
    protected Long getDefaultStoreParentId(Long parentId) {
        return getDefaultAndCreateDirName(parentId);
    }

    private static Date formDate(String dateStr) {
        // 使用 ISO 8601 格式解析
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateStr);
        // 转换为 Instant
        Instant instant = offsetDateTime.toInstant();
        // 转换为 Date
        Date date = Date.from(instant);
        return date;
    }


    private List<ChatMarkdownResult> convertConversationsToMarkdown(String jsonData) {
        // 1. 解析 JSON 数组
        JSONArray conversations = JSON.parseArray(jsonData);
        List<ChatMarkdownResult>  cmrList = new LinkedList<>();
        // 2. 遍历每个对话
        for (int i = 0; i < conversations.size(); i++) {
            JSONObject conversation = conversations.getJSONObject(i);
            // 3. 构建 Markdown 文档
            StringBuilder markdown = new StringBuilder();
            // 添加标题
            String title = conversation.getString("title");
            // 添加元数据
            String id = conversation.getString("id");
            Date createTime = formDate(conversation.getString("inserted_at"));
            Date updateTime = formDate(conversation.getString("updated_at"));
            // 获取对话映射
            JSONObject mapping = conversation.getJSONObject("mapping");
            // 4. 找到根节点并构建对话树
            Map<String, JSONObject> nodeMap = new HashMap<>();
            String rootId = null;
            // 首先收集所有节点并找到根节点
            for (String key : mapping.keySet()) {
                JSONObject node = mapping.getJSONObject(key);
                nodeMap.put(key, node);
                if ("root".equals(node.getString("id"))) {
                    rootId = key;
                }
            }
            // 5. 从根节点开始遍历对话
            if (rootId != null) {
                JSONObject rootNode = nodeMap.get(rootId);
                List<String> children = rootNode.getJSONArray("children").toList(String.class);
                // 遍历所有子节点（对话链）
                for (String childId : children) {
                    traverseConversation(childId, nodeMap, markdown);
                }
            }
            String markdownDoc = markdown.toString();
            if (StringUtils.isEmpty(markdownDoc)) {
                markdown.append(title);
                markdownDoc = markdown.toString();
            }
            cmrList.add(new ChatMarkdownResult(id, title, createTime, updateTime, markdownDoc));
        }
        return cmrList;
    }

    private  void traverseConversation(String nodeId,
                                             Map<String, JSONObject> nodeMap,
                                             StringBuilder markdown) {
        JSONObject node = nodeMap.get(nodeId);
        if (node == null) return;
        JSONObject message = node.getJSONObject("message");
        if (message != null) {
            JSONArray fragments = message.getJSONArray("fragments");
            if (fragments != null && !fragments.isEmpty()) {
                JSONObject fragment = fragments.getJSONObject(0);
                String type = fragment.getString("type");
                String content = fragment.getString("content");
                String model = message.getString("model");
                // 添加对话内容
                if ("REQUEST".equals(type)) {
                    markdown.append("### 💬 提问\n\n");
//                    markdown.append("**模型:** ").append(model).append("  \n");
                    markdown.append("```\n").append(content).append("\n```\n\n");
                } else if ("RESPONSE".equals(type)) {
                    markdown.append("### 🤖 回答\n\n");
//                    markdown.append("**模型:** ").append(model).append("  \n");
                    // 保持 Markdown 格式，所以直接添加内容
                    markdown.append(content).append("\n\n");
                    markdown.append("---\n\n");
                }
            }
        }
        // 递归遍历子节点
        List<String> children = node.getJSONArray("children").toList(String.class);
        for (String childId : children) {
            traverseConversation(childId, nodeMap, markdown);
        }
    }

}
