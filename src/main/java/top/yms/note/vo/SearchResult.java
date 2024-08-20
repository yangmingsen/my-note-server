package top.yms.note.vo;

public interface SearchResult {
    int Note_Index_Type = 0;
    int Note_Title_Type = 1;
    int Note_Content_Type = 2;


    int getResType();
    Long getId();
    String getResult();
}
