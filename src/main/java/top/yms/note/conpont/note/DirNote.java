package top.yms.note.conpont.note;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import top.yms.note.conpont.ComponentSort;
import top.yms.note.dto.INoteData;

/**
 * 此为目录笔记（特殊类型）
 */
@Component
public class DirNote implements Note{
    @Override
    public boolean support(String type) {
        if (StringUtils.isBlank(type)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean supportSave() {
        return false;
    }

    @Override
    public int compareTo(@NotNull ComponentSort other) {
        return this.getSortValue()-other.getSortValue();
    }

    @Override
    public int getSortValue() {
        return 9999;
    }

    @Override
    public boolean supportEncrypt() {
        return true;
    }

    @Override
    public boolean noteDecrypt(Long id) {
        return false;
    }

    @Override
    public boolean noteEncrypt(Long id) {
        return false;
    }

    @Override
    public boolean supportExport(String noteType, String exportType) {
        return false;
    }

    @Override
    public String export(Long noteId, String exportType) {
        return null;
    }

    @Override
    public boolean supportVersion() {
        return false;
    }

    @Override
    public void addNoteVersion(INoteData iNoteData) {

    }
}
