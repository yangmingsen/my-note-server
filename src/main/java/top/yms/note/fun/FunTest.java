package top.yms.note.fun;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import top.yms.note.entity.NoteIndex;
import top.yms.note.utils.IdWorker;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by yangmingsen on 2024/3/30.
 */
public class FunTest {

    static String searchPath = "D:\\MinGW\\";

    final static IdWorker idWorker = new IdWorker(0,1);

    static class ExtNoteIndex extends NoteIndex {
        private File file;

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }
    }


    public ExtNoteIndex transfer(File file, Long parentId) {
        ExtNoteIndex info = new ExtNoteIndex();
        long fid = idWorker.nextId();
        String name = file.getName();
        int len = name.length();

        info.setName(name);
        info.setId(fid);
        info.setParentId(parentId);
        info.setUserId(1111L);
        info.setDel("0");
        info.setCreateTime(new Date());
        info.setFile(file);

        boolean directory = file.isDirectory();
        if (!directory) {
            int dot = name.lastIndexOf('.');
            if (dot > 0) {
                //获取文件后缀
                String fileType = name.substring(dot + 1, len);
                info.setType(fileType);
            }

            info.setIsile("1");
        } else {
            info.setIsile("0");
        }

        return info;
    }

    public boolean filter(File file) {
        String [] filter = {".git"};
        for (String ex : filter) {
            if (file.getName().equals(ex)) {
                return false;
            }
        }
        return true;
    }

    public List<NoteIndex> mockDataFromDir(String path) {
        if (StringUtils.isBlank(path)) return Collections.emptyList();
        File abFile = new File(path);
        if (!abFile.exists()) {
            System.out.println("目标不存在");
            return Collections.emptyList();
        }

        List<NoteIndex> res = new ArrayList<>();
        final Queue<ExtNoteIndex> queue = Arrays.stream(Objects.requireNonNull(abFile.listFiles())).filter(this::filter).map(file -> transfer(file, 0L)).collect(Collectors.toCollection(LinkedList::new));

        while (!queue.isEmpty()) {
            ExtNoteIndex item = queue.poll();
            if (item.getIsile().equals("0")) {
                //目录
                final Long parentId = item.getId();
                List<ExtNoteIndex> subList = Arrays.stream(Objects.requireNonNull(item.getFile().listFiles())).filter(this::filter).map(file -> transfer(file, parentId)).collect(Collectors.toCollection(LinkedList::new));
                queue.addAll(subList);
            }

            res.add(item);
        }


        return res;
    }


    @Test
    public void testMockSearchDir() throws Exception {
        String s1 = "G:\\Project\\Java\\JavaProject\\";
        mockDataFromDir(s1).forEach(System.out::println);
    }


    @Test
    public void test111() {
        String name = "哈哈哈.txt";
        int dot = name.lastIndexOf('.');
        System.out.println(name.substring(0,dot));
        System.out.println(name.substring(dot,name.length()));
    }


    @Test
    public void test333() {
        
    }


}
