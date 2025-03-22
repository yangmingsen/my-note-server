package top.yms.note.conpont;

public interface INoteCache <K,V>{
    V findOne(K k);

    void put(K k , V v);
}
