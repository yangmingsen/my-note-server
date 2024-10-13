package top.yms.note.conpont;

public interface SysConfigService {

    Object getObjValue(String key);

    String getStringValue(String key);

    default int getIntValue(String key) { throw new RuntimeException("Not implement");}

    default long getLongValue(String key){ throw new RuntimeException("Not implement");}
}
