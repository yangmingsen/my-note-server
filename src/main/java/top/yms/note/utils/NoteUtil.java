package top.yms.note.utils;

import top.yms.note.enums.FileTypeEnum;

public class NoteUtil {

    public static String getFileType(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot > 0) {
            int len = fileName.length();
            //获取文件后缀
            String fileType = fileName.substring(dot + 1, len).toLowerCase();
            if (fileType.length() > 10) {
                return FileTypeEnum.UNKNOWN.getValue();
            } else {
                return fileType;
            }

        } else {
            return FileTypeEnum.UNKNOWN.getValue();
        }
    }

}
