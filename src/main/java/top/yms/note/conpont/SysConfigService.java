package top.yms.note.conpont;

/**
 * 系统配置获取服务
 */
public interface SysConfigService {

    Object getObjValue(String key);

    /**
     * 根据key从数据库中获取一个字符串配置
     * @param key
     * @return
     */
    String getStringValue(String key);

    default int getIntValue(String key) { throw new RuntimeException("Not implement");}

    default long getLongValue(String key){ throw new RuntimeException("Not implement");}
}
