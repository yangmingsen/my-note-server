package top.yms.note.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import top.yms.note.entity.SearchLog;
import top.yms.note.mapper.SearchLogMapper;
import top.yms.note.utils.IdWorker;

/**
 * Created by yangmingsen on 2024/8/17.
 */
@Service
public class NoteSearchLogServiceImpl {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Autowired
    private SearchLogMapper searchLogMapper;


    @Autowired
    IdWorker idWorker;


    public void add(SearchLog searchLog) {
        if (searchLog.getId() == null) {
            long id = idWorker.nextId();
            searchLog.setId(id);
        }
        searchLogMapper.insertSelective(searchLog);
    }
}
