package top.yms.note.service;

import com.alibaba.fastjson2.JSONObject;

public interface NoteCustomConfService {

    Object findUserConfig(Long userId);

    void updateUserConfig(JSONObject jsonObject);
}
