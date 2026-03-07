package top.yms.note.service;

import top.yms.note.entity.RestOut;

/**
 * 用于一次性或临时作业服务
 */
public interface TempService {

    /**
     * 网络资源存储由mongo存储改为mysql存储
     * @return
     */
    RestOut<String> networkResourceInfoFromMongoToMysql();

    /**
     * 减少file storage 使用 的空间，改为使用网络资源
     * @return
     */
    RestOut<String> reduceFileStorageCap();

}
