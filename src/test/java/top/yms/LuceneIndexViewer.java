package top.yms;

import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;
import java.io.IOException;
import java.nio.file.Paths;

public class LuceneIndexViewer {
    public static void main(String[] args) throws IOException {
        String indexPath = "D:\\Soft\\PersonalSoft\\Server\\note\\index";  // 替换成实际的索引目录
        try (DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)))) {
            System.out.println("索引文档数: " + reader.numDocs());
            for (LeafReaderContext context : reader.leaves()) {
                LeafReader leafReader = context.reader();
                FieldInfos fieldInfos = leafReader.getFieldInfos();
                for (FieldInfo fieldInfo : fieldInfos) {
                    System.out.println("字段: " + fieldInfo.name);
                }
            }
        }
    }
}
