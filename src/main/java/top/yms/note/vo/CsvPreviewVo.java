package top.yms.note.vo;

import com.alibaba.fastjson2.JSONArray;

/**
 * Created by yangmingsen on 2024/10/5.
 */
public class CsvPreviewVo {
    private JSONArray columns;
    private JSONArray dataSource;

    public JSONArray getColumns() {
        return columns;
    }

    public void setColumns(JSONArray columns) {
        this.columns = columns;
    }

    public JSONArray getDataSource() {
        return dataSource;
    }

    public void setDataSource(JSONArray dataSource) {
        this.dataSource = dataSource;
    }
}
