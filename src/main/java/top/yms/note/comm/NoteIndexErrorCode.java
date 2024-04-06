package top.yms.note.comm;

public enum NoteIndexErrorCode implements ErrorCode{
    E_203100(203100, "目录名称不能为空"),
    E_203101(203101, "文件名称不能为空"),
    E_203102(203102, "文件类型标识不能为空"),
    E_203103(203103, "文件类型不能为空"),
    E_203104(203104, "note id不能为空"),
    E_203105(203105, "note parentId不能为空"),


    E_203333(203333, "Note未知错误");
    private int code;
    private String desc;

    private NoteIndexErrorCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public int getCode() {
        return 0;
    }

    @Override
    public String getDesc() {
        return null;
    }
}
