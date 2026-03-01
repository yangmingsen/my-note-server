package top.yms.note.service;

import top.yms.note.entity.RestOut;

/**
 * 用于一次性或临时作业服务
 */
public interface TempService {

    RestOut<String> networkResourceInfoFromMongoToMysql();

}
