package top.yms.note.conpont.note;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.yms.note.config.SpringContext;
import top.yms.note.conpont.AnyFile;
import top.yms.note.dto.INoteData;
import top.yms.note.entity.AntTreeNode;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteIndex;
import top.yms.note.exception.BusinessException;
import top.yms.note.exception.NoteSystemException;
import top.yms.note.msgcd.CommonErrorCode;
import top.yms.note.msgcd.NoteSystemErrorCode;
import top.yms.note.utils.IdWorker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class ArchivePreview extends AbstractNote{

    private final static Logger log = LoggerFactory.getLogger(ArchivePreview.class);

    private final List<ArchiveTreeParser> archiveTreeParserList =  new LinkedList<>();

    private List<ArchiveTreeParser> getArchiveTreeParserList() {
        if (archiveTreeParserList.isEmpty()) {
            archiveTreeParserList.add(new DefaultArchiveTreeParser());
            archiveTreeParserList.add(new TarGzArchiveTreeParser());
            archiveTreeParserList.add(new SevenZArchiveTreeParse());
            archiveTreeParserList.add(new RarArchiveTreeParser());
            archiveTreeParserList.add(new TarArchiveTreeParser());
        }
        return archiveTreeParserList;
    }

    private static class ArchiveMeta {
        private NoteIndex notMeta;
        private AnyFile anyFile;
        private AntTreeNode antTreeNode;
        private Map<String, AntTreeNode> pathMap;
        private AntTreeNode root;

        public NoteIndex getNotMeta() {
            return notMeta;
        }

        public void setNotMeta(NoteIndex notMeta) {
            this.notMeta = notMeta;
        }

        public AnyFile getAnyFile() {
            return anyFile;
        }

        public void setAnyFile(AnyFile anyFile) {
            this.anyFile = anyFile;
        }

        public AntTreeNode getAntTreeNode() {
            return antTreeNode;
        }

        public void setAntTreeNode(AntTreeNode antTreeNode) {
            this.antTreeNode = antTreeNode;
        }

        public Map<String, AntTreeNode> getPathMap() {
            return pathMap;
        }

        public void setPathMap(Map<String, AntTreeNode> pathMap) {
            this.pathMap = pathMap;
        }

        public AntTreeNode getRoot() {
            return root;
        }

        public void setRoot(AntTreeNode root) {
            this.root = root;
        }
    }

    private interface ArchiveTreeParser {

        AntTreeNode parse(ArchiveMeta archiveMeta) throws Exception;

        default boolean support(ArchiveMeta archiveMeta) {return false;}

        boolean support(String type);
    }

    private static abstract class AbstractArchiveTreeParser implements ArchiveTreeParser {

        protected IdWorker getIdWorker() {
            return SpringContext.getBean(IdWorker.class);
        }

        public AntTreeNode parse(ArchiveMeta archiveMeta) throws Exception {
            if (!beforeParse(archiveMeta)) {
                return null;
            }
            AntTreeNode root = new AntTreeNode(archiveMeta.getNotMeta().getName(), getIdWorker().nextId()+"");
            Map<String, AntTreeNode> pathMap = new HashMap<>();
            pathMap.put("", root);
            archiveMeta.setRoot(root);
            archiveMeta.setPathMap(pathMap);
            AntTreeNode antTreeNode = doParse(archiveMeta);
            afterParse(archiveMeta);
            return antTreeNode;
        }

        protected boolean beforeParse(ArchiveMeta archiveMeta) {
            return true;
        }

        abstract AntTreeNode doParse(ArchiveMeta archiveMeta) throws Exception;

        protected void afterParse(ArchiveMeta archiveMeta) {

        }

        public boolean support(String type) {
            for (String sp : getSupportList()) {
                if (StringUtils.equals(type, sp)) {
                    return true;
                }
            }
            return false;
        }

        public boolean support(ArchiveMeta archiveMeta) {
            for (String sp : getSupportList()) {
                if (StringUtils.equals(archiveMeta.getNotMeta().getType(), sp)) {
                    return true;
                }
            }
            return false;
        }

        abstract String[] getSupportList();

        protected  void addToTree(AntTreeNode root, Map<String, AntTreeNode> pathMap, String name) {
            String[] parts = name.split("/");
            StringBuilder pathBuilder = new StringBuilder();
            AntTreeNode parent = root;
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                pathBuilder.append(part);
                String currentPath = pathBuilder.toString();
                if (!pathMap.containsKey(currentPath)) {
                    AntTreeNode node = new AntTreeNode(part, getIdWorker().nextId()+"");
                    parent.addChild(node);
                    pathMap.put(currentPath, node);
                }
                parent = pathMap.get(currentPath);
                pathBuilder.append("/");
            }
        }
    }

    private static class DefaultArchiveTreeParser extends AbstractArchiveTreeParser {

        private final String [] supportParseList = {"zip", "jar", "war" };

        @Override
        String[] getSupportList() {
            return supportParseList;
        }

        @Override
        AntTreeNode doParse(ArchiveMeta archiveMeta) throws Exception {
            AntTreeNode root = archiveMeta.getRoot();
            try (InputStream is = archiveMeta.getAnyFile().getInputStream();
                    ZipInputStream zis = new ZipInputStream(is)) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    String entryName = entry.getName();
                    addToTree(root, archiveMeta.getPathMap(), entryName);
                }
            }catch (Exception e) {
                throw e;
            }
            return root;
        }
    }

    private static class TarGzArchiveTreeParser extends AbstractArchiveTreeParser {

        private final String [] supportParseList = {"gz", "tar.gz", "tgz"};

        @Override
        String[] getSupportList() {
            return supportParseList;
        }

        @Override
        AntTreeNode doParse(ArchiveMeta archiveMeta) throws Exception {
            AntTreeNode root = archiveMeta.getRoot();
            try (  InputStream is = archiveMeta.getAnyFile().getInputStream();
                    InputStream gis = new GzipCompressorInputStream(is);
                 TarArchiveInputStream tais = new TarArchiveInputStream(gis)){
                TarArchiveEntry entry;
                while ((entry = tais.getNextTarEntry()) != null) {
                    String entryName = entry.getName();
                    addToTree(root, archiveMeta.getPathMap(), entryName);
                }
            } catch (Exception e) {
                throw e;
            }
            return root;
        }
    }

    private static class SevenZArchiveTreeParse extends AbstractArchiveTreeParser {

        private final String [] supportParseList = { "7z" };

        private byte[] toByteArray(InputStream in) throws IOException {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] tmp = new byte[8192];
            int n;
            while ((n = in.read(tmp)) != -1) {
                buffer.write(tmp, 0, n);
            }
            in.close();
            return buffer.toByteArray();
        }

        @Override
        AntTreeNode doParse(ArchiveMeta archiveMeta) throws Exception {
            AntTreeNode root = archiveMeta.getRoot();
            InputStream is = archiveMeta.getAnyFile().getInputStream();
            byte[] bytes = toByteArray(is);
            try (SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel(bytes);
                 SevenZFile sevenZFile = new SevenZFile(channel)) {
                SevenZArchiveEntry entry;
                while ((entry = sevenZFile.getNextEntry()) != null) {
                    String entryName = entry.getName();
                    addToTree(root, archiveMeta.getPathMap(), entryName);
                }
            } finally {
                is.close();
            }
            return root;
        }

        @Override
        String[] getSupportList() {
            return supportParseList;
        }
    }

    private static class RarArchiveTreeParser extends AbstractArchiveTreeParser {

        private final String [] supportParseList = { "rar" };

        @Override
        AntTreeNode doParse(ArchiveMeta archiveMeta) throws Exception {
            AntTreeNode root = archiveMeta.getRoot();
            Archive archive = null;
            try {
                archive = new Archive(archiveMeta.getAnyFile().getInputStream());
                List<FileHeader> headers = archive.getFileHeaders();
                for (FileHeader header : headers) {
                    if (header.isDirectory()) continue;
                    String entryName = header.getFileNameString().replace("\\", "/");
                    addToTree(root, archiveMeta.getPathMap(), entryName);
                }
            } catch (Exception e) {
                throw new IOException("Failed to parse rar file", e);
            } finally {
                if (archive != null) archive.close();
            }
            return root;
        }

        @Override
        String[] getSupportList() {
            return supportParseList;
        }
    }

    private static class TarArchiveTreeParser extends AbstractArchiveTreeParser {

        private final String [] supportParseList = { "tar" };

        @Override
        AntTreeNode doParse(ArchiveMeta archiveMeta) throws Exception {
            AntTreeNode root = archiveMeta.getRoot();
            try (InputStream is = archiveMeta.getAnyFile().getInputStream();
                    TarArchiveInputStream tais = new TarArchiveInputStream(is)) {
                TarArchiveEntry entry;
                while ((entry = tais.getNextTarEntry()) != null) {
                    String entryName = entry.getName();
                    addToTree(root, archiveMeta.getPathMap(), entryName);
                }
            }
            return root;
        }

        @Override
        String[] getSupportList() {
            return supportParseList;
        }
    }



    @Override
    void doSave(INoteData iNoteData) throws BusinessException {
        throw new BusinessException(CommonErrorCode.E_200214);
    }

    @Override
    public boolean support(String type) {
        return checkSupportParse(type);
    }


    public INoteData getContent(Long id) {
        NoteIndex noteMeta = noteIndexMapper.selectByPrimaryKey(id);
        AnyFile anyFile = fileStoreService.loadFile(noteMeta.getSiteId());
        ArchiveMeta archiveMeta = new ArchiveMeta();
        archiveMeta.setNotMeta(noteMeta);
        archiveMeta.setAnyFile(anyFile);
        ObjectMapper mapper = new ObjectMapper();
        String jsonStr = null;
        try {
            AntTreeNode antTreeNode = parseArchiveTree(archiveMeta);
            jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(antTreeNode);
        } catch (Exception ex) {
            log.error("parse archive error", ex);
            if (ex instanceof BusinessException) {
                throw (BusinessException) ex;
            }
            throw new NoteSystemException(NoteSystemErrorCode.E_400007);
        }
        NoteData resVal = new NoteData();
        resVal.setId(id);
        resVal.setContent(jsonStr);
        return resVal;
    }

    private AntTreeNode parseArchiveTree(ArchiveMeta archiveMeta) throws Exception{
        for (ArchiveTreeParser archiveTreeParser : getArchiveTreeParserList()) {
            if (archiveTreeParser.support(archiveMeta)) {
                return archiveTreeParser.parse(archiveMeta);
            }
        }
        throw new BusinessException(CommonErrorCode.E_200214);
    }

    private boolean checkSupportParse(String type) {
        for (ArchiveTreeParser archiveTreeParser : getArchiveTreeParserList()) {
            if (archiveTreeParser.support(type)) {
                return true;
            }
        }
        return false;
    }

}
