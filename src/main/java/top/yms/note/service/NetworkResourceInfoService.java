package top.yms.note.service;

import top.yms.note.entity.NetworkResourceInfo;

public interface NetworkResourceInfoService {

    NetworkResourceInfo findOne(String noteFileId);

}
