package top.yms.note.dto;

/**
 * Created by yangmingsen on 2024/10/17.
 */
public class NoteIndexLuceneUpdateDto {
    /**
     * 用于在创建文件夹或文件夹时使用，不包含索引内容
     */
    public static int updateNoteIndex = 0;

    /**
     * 用于包含内容的索引创建
     */
    public static int updateNoteContent = 1;

    /**
     * 用于查找某个笔记内容时使用
     */
    public static int findOne = 2;

    /**
     * 用于删除某个笔记索引时使用
     */
    public static int deleteOne = 3;

    /**
     * 删除列表
     */
    public static int deleteList = 4;


    /**
     * type 与上面关联。比如使用删除某个笔记索引，就用deleteOne
     */
    private int type;


    private Object data;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public static class Builder {
        private Builder(){}
        private NoteIndexLuceneUpdateDto noteIndexLuceneUpdateDto;

        public static Builder build() {
            Builder builder = new Builder();
            builder.noteIndexLuceneUpdateDto = new NoteIndexLuceneUpdateDto();

            return builder;
        }

        public  Builder type(int type) {
            noteIndexLuceneUpdateDto.setType(type);
            return this;
        }

        public Builder data(Object data) {
            noteIndexLuceneUpdateDto.setData(data);
            return this;
        }

        public NoteIndexLuceneUpdateDto get() {
            return noteIndexLuceneUpdateDto;
        }
    }
}



