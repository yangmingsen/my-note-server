package top.yms.note.conpont.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import top.yms.note.comm.Constants;
import top.yms.note.conpont.NoteDataIndexService;
import top.yms.note.conpont.NoteQueue;
import top.yms.note.conpont.NoteSearch;
import top.yms.note.dto.NoteDataIndex;
import top.yms.note.dto.NoteLuceneIndex;
import top.yms.note.dto.NoteSearchDto;
import top.yms.note.entity.NoteData;
import top.yms.note.entity.NoteIndex;
import top.yms.note.entity.SearchLog;
import top.yms.note.mapper.NoteDataMapper;
import top.yms.note.mapper.NoteIndexMapper;
import top.yms.note.service.NoteSearchLogService;
import top.yms.note.utils.IdWorker;
import top.yms.note.vo.NoteSearchResult;
import top.yms.note.vo.SearchResult;


import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.util.StringUtils;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.nio.file.Paths;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yangmingsen on 2024/8/21.
 */
@Component(Constants.noteLuceneSearch)
public class NoteLuceneService implements NoteSearch, InitializingBean, NoteDataIndexService {
    private final static Logger logger = LoggerFactory.getLogger(NoteLuceneService.class);

//    public final static String indexPath = "E:\\tmp\\note-search-index\\";
    public final static String indexPath = "E:\\PersonalSoft\\Server\\note\\index\\";

    @Autowired
    NoteSearchLogService noteSearchLogService;

    @Autowired
    IdWorker idWorker;

    @Autowired
    private NoteIndexMapper noteIndexMapper;

    @Autowired
    private NoteDataMapper noteDataMapper;

    @Qualifier(Constants.noteLuceneIndexMemoryQueue)
    @Autowired
    private NoteQueue noteQueue;

    private Thread updateIndexTask ;

    @Override
    public List<SearchResult> doSearch(NoteSearchDto noteSearchDto) {
        Long userId = noteSearchDto.getUserId();
        String keyword = noteSearchDto.getKeyword();
        List<SearchResult> searchResults = new LinkedList<>();

        //add search log
        SearchLog searchLog = new SearchLog();
        searchLog.setId(idWorker.nextId());
        searchLog.setSearchContent(noteSearchDto.getKeyword());
        searchLog.setCreateTime(new Date());
        searchLog.setUserId(userId);
        noteSearchLogService.add(searchLog);

        try {
            Directory directory = FSDirectory.open(Paths.get(indexPath));
            IndexReader indexReader = DirectoryReader.open(directory);
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);

            Query userIdQuery = LongPoint.newExactQuery("userId", userId);
            IKAnalyzer ikAnalyzer = new IKAnalyzer();
            QueryParser titleParser = new QueryParser("title", ikAnalyzer);
            Query titleQuery = titleParser.parse(keyword);

            QueryParser queryParser = new QueryParser("content", ikAnalyzer);
            Query contentQuery = queryParser.parse(keyword);

            BooleanQuery.Builder orBuilder = new BooleanQuery.Builder();
            orBuilder.add(titleQuery, BooleanClause.Occur.SHOULD);
            orBuilder.add(contentQuery, BooleanClause.Occur.SHOULD);
            Query keywordQuery = orBuilder.build();

            // 创建最终的查询，必须同时满足用户 ID 和标题或内容的查询
            BooleanQuery.Builder finalQueryBuilder = new BooleanQuery.Builder();
            finalQueryBuilder.add(userIdQuery, BooleanClause.Occur.MUST);
            finalQueryBuilder.add(keywordQuery, BooleanClause.Occur.MUST);
            Query finalQuery = finalQueryBuilder.build();


            TopDocs topDocs = indexSearcher.search(finalQuery, 10);
            logger.info("本次搜索共找到" + topDocs.totalHits.value + "条数据");

            SimpleHTMLFormatter  formatter = new SimpleHTMLFormatter("<span style=\"color:red; font-weight:bold;\">", "</span>");
            QueryScorer queryScorer = new QueryScorer(finalQuery);
            Highlighter highlighter = new Highlighter(formatter, queryScorer);
            highlighter.setTextFragmenter(new SimpleSpanFragmenter(queryScorer, 20));

            ScoreDoc[] hits  = topDocs.scoreDocs;

            for (ScoreDoc hit : hits) {
                Document hitDoc  = indexSearcher.doc(hit.doc);

                NoteSearchResult searchResult = new NoteSearchResult();
                long id = hitDoc.getField("id").numericValue().longValue();
                long parentId = hitDoc.getField("parentId").numericValue().longValue();
                String type = hitDoc.getField("type").stringValue();
                String isFile = hitDoc.getField("isFile").stringValue();
                searchResult.setId(id);
                searchResult.setParentId(parentId);
                searchResult.setType(type);
                searchResult.setIsile(isFile);

                String title = hitDoc.get("title");
                if (!StringUtils.isEmpty(title)) {
                    // 获取高亮的文本片段
                    TokenStream titleTokenStream = TokenSources.getTokenStream("title", indexReader.getTermVectors(hit.doc), title, ikAnalyzer, -1);
                    String titleFragment = highlighter.getBestFragment(titleTokenStream, title);

                    searchResult.setResType(SearchResult.Note_Title_Type);
                    searchResult.setResult(titleFragment);
                }

                String content = hitDoc.get("content");
                if (!StringUtils.isEmpty(content)) {
                    // 获取高亮的文本片段
                    TokenStream contentTokenStream = TokenSources.getTokenStream("content", indexReader.getTermVectors(hit.doc), content, ikAnalyzer, -1);
                    String contentFragment = highlighter.getBestFragment(contentTokenStream, content);

                    searchResult.setResType(SearchResult.Note_Content_Type);
                    searchResult.setResult(contentFragment);
                }

                searchResults.add(searchResult);
            }
            indexReader.close();
            directory.close();
        }catch (Exception e) {
            logger.error("搜索发生错误: ", e);
        }
        return searchResults;
    }

    @Override
    public void delete(Long id) {
        try {
            Directory directory = FSDirectory.open(Paths.get(indexPath));
            IndexWriterConfig config = new IndexWriterConfig(new IKAnalyzer());
            IndexWriter indexWriter = new IndexWriter(directory, config);

            Term idTerm = new Term("id", Long.toString(id));
            indexWriter.deleteDocuments(idTerm);

            indexWriter.commit();
            indexWriter.close();
            directory.close();

        } catch (Exception e) {
            logger.error("删除索引失败", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(List<Long> ids) {
        try {
            Directory directory = FSDirectory.open(Paths.get(indexPath));
            IndexWriterConfig config = new IndexWriterConfig(new IKAnalyzer());
            IndexWriter indexWriter = new IndexWriter(directory, config);

            for (Long id : ids) {
                Term idTerm = new Term("id", Long.toString(id));
                indexWriter.deleteDocuments(idTerm);
            }

            indexWriter.commit();
            indexWriter.close();
            directory.close();
        } catch (Exception e) {
            logger.error("删除索引失败", e);
            throw new RuntimeException(e);
        }
    }

    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            // 递归删除目录中的所有文件和子目录
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        // 删除当前文件或空目录
        dir.delete();
    }
    @Override
    public void rebuildIndex() {
        try {//delete old index
            File rootDir = new File(indexPath);
            deleteDirectory(rootDir);

            Directory directory = FSDirectory.open(Paths.get(indexPath));
            IndexWriterConfig config = new IndexWriterConfig(new IKAnalyzer());
            IndexWriter indexWriter = new IndexWriter(directory, config);

            List<NoteIndex> noteIndexList = noteIndexMapper.findAll();
            for (NoteIndex noteIndex : noteIndexList) {
                String title = noteIndex.getName();
                Document document = new Document();
                document.add(new StoredField("id", noteIndex.getId()));
                document.add(new LongPoint("userId", noteIndex.getUserId()));
                document.add(new StoredField("userId", noteIndex.getUserId()));
                document.add(new StoredField("parentId", noteIndex.getParentId()));
                if (!StringUtils.isEmpty(title)) {
                    document.add(new TextField("title", title, Field.Store.YES));
                }
                if (noteIndex.getStoreSite().equals("mysql")) {
                    NoteData noteData = noteDataMapper.selectByPrimaryKey(noteIndex.getId());
                    if (!StringUtils.isEmpty(noteData.getContent())) {
                        document.add(new TextField("content", noteData.getContent(), Field.Store.YES));
                    }
                }
                document.add(new StoredField("type", noteIndex.getType()));
                document.add(new StoredField("isFile", noteIndex.getIsile()));
                document.add(new LongPoint("createDate", noteIndex.getCreateTime().getTime()));
                document.add(new StoredField("createDate", noteIndex.getCreateTime().getTime()));

                //添加文档
                indexWriter.addDocument(document);
            }

            indexWriter.commit();
            indexWriter.close();
            directory.close();
            logger.info("rebuild index success");
        } catch (Exception e) {
            logger.error("重新建立index失败", e);
            throw new RuntimeException(e);
        }

    }

    public void update(NoteDataIndex noteDatandex) {
        NoteLuceneIndex noteLuceneIndex = (NoteLuceneIndex)noteDatandex;
        try {
            Long id = noteLuceneIndex.getId();
            Long userId = noteLuceneIndex.getUserId();
            Long parentId = noteLuceneIndex.getParentId();
            String title = noteLuceneIndex.getTitle();
            String content = noteLuceneIndex.getContent();
            String type = noteLuceneIndex.getType();
            String isFile = noteLuceneIndex.getIsFile();
            Date createDate = noteLuceneIndex.getCreateDate();

            Directory directory = FSDirectory.open(Paths.get(indexPath));
            IndexWriterConfig config = new IndexWriterConfig(new IKAnalyzer());
            IndexWriter indexWriter = new IndexWriter(directory, config);

            Term idTerm = new Term("id", Long.toString(id));
            Document document = new Document();
            document.add(new StoredField("id", id));
            document.add(new LongPoint("userId", userId));
            document.add(new StoredField("userId", userId));
            document.add(new StoredField("parentId", parentId));
            if (!StringUtils.isEmpty(title)) {
                document.add(new TextField("title", title, Field.Store.YES));
            }
            if (!StringUtils.isEmpty(content)) {
                document.add(new TextField("content", content, Field.Store.YES));
            }

            document.add(new StoredField("type", type));
            document.add(new StoredField("isFile", isFile));
            document.add(new LongPoint("createDate", createDate.getTime()));
            document.add(new StoredField("createDate", createDate.getTime()));
            //提交更改并关闭IndexWriter
            indexWriter.updateDocument(idTerm, document);

            indexWriter.commit();
            indexWriter.close();
            directory.close();
            logger.info("更新完成...{}", id);
        } catch (Exception e) {
            logger.error("更新lucene索引失败：", e);
            throw new RuntimeException(e);
        }
    }

    public void updateIndex(Long id) {
        try {
            Directory directory = FSDirectory.open(Paths.get(indexPath));
            IndexWriterConfig config = new IndexWriterConfig(new IKAnalyzer());
            IndexWriter indexWriter = new IndexWriter(directory, config);

            Term idTerm = new Term("id", Long.toString(id));

            NoteIndex noteIndex = noteIndexMapper.selectByPrimaryKey(id);
            String title = noteIndex.getName();
            if (StringUtils.isEmpty(noteIndex.getName())) {
                title = " ";
            }
            Document document = new Document();
            document.add(new StoredField("id", noteIndex.getId()));
            document.add(new LongPoint("userId", noteIndex.getUserId()));
            document.add(new StoredField("userId", noteIndex.getUserId()));
            document.add(new StoredField("parentId", noteIndex.getParentId()));
            document.add(new TextField("title", title, Field.Store.YES));
            if (noteIndex.getStoreSite().equals("mysql")) {
                NoteData noteData = noteDataMapper.selectByPrimaryKey(noteIndex.getId());
                if (!StringUtils.isEmpty(noteData.getContent())) {
                    document.add(new TextField("content", noteData.getContent(), Field.Store.YES));
                }
            }
            document.add(new StoredField("type", noteIndex.getType()));
            document.add(new StoredField("isFile", noteIndex.getIsile()));
            document.add(new LongPoint("createDate", noteIndex.getCreateTime().getTime()));
            document.add(new StoredField("createDate", noteIndex.getCreateTime().getTime()));
            //提交更改并关闭IndexWriter
            indexWriter.updateDocument(idTerm, document);

            indexWriter.commit();
            indexWriter.close();
            directory.close();
            logger.info("更新完成...{}", id);
        } catch (Exception e) {
            logger.error("updateIndex Error:", e);
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {
//        updateIndexTask = new Thread(() -> {
//            logger.info("启动更新lucene索引线程成功....");
//            while (true) {
//                try {
//                    NoteLuceneIndex noteLuceneIndex = (NoteLuceneIndex)noteQueue.take();
//                    updateIndex(noteLuceneIndex);
//                } catch (Exception e) {
//                    logger.error("更新lucene索引处理错误: ", e);
//                }
//            }
//        });
//        updateIndexTask.start();
    }
}
