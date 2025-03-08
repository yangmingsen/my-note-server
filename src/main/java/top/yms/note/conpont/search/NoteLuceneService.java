package top.yms.note.conpont.search;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.wltea.analyzer.lucene.IKAnalyzer;
import top.yms.note.comm.NoteConstants;
import top.yms.note.conpont.NoteDataIndexService;
import top.yms.note.conpont.NoteLuceneDataService;
import top.yms.note.conpont.NoteSearchService;
import top.yms.note.dto.NoteSearchDto;
import top.yms.note.entity.NoteIndex;
import top.yms.note.entity.SearchLog;
import top.yms.note.mapper.NoteDataMapper;
import top.yms.note.mapper.NoteIndexMapper;
import top.yms.note.service.NoteIndexService;
import top.yms.note.service.NoteSearchLogService;
import top.yms.note.utils.IdWorker;
import top.yms.note.vo.NoteSearchResult;
import top.yms.note.vo.SearchResult;

import java.io.File;
import java.nio.file.Paths;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by yangmingsen on 2024/8/21.
 */
@Component(NoteConstants.noteLuceneSearch)
public class NoteLuceneService implements NoteSearchService, InitializingBean, NoteDataIndexService {
    private final static Logger logger = LoggerFactory.getLogger(NoteLuceneService.class);

    @Value("${system.lucence.index_path}")
    private String indexPath;

    @Autowired
    NoteSearchLogService noteSearchLogService;

    @Autowired
    IdWorker idWorker;

    @Autowired
    private NoteIndexMapper noteIndexMapper;

    @Autowired
    private NoteIndexService noteIndexService;

    @Autowired
    private NoteDataMapper noteDataMapper;

    @Autowired
    protected MongoTemplate mongoTemplate;


    @Qualifier(NoteConstants.noteLuceneDataServiceImpl)
    @Autowired
    private NoteLuceneDataService noteLuceneDataService;

    private static final Object syncObj = new Object();

    static String nbsp = "&nbsp;";
    private String getViewValue(String value) {
        StringBuilder tmpStr = new StringBuilder();
        for (int i=0; i<5; i++) {
            tmpStr.append(nbsp);
        }
        tmpStr.append("<span>");
        tmpStr.append(value);
        tmpStr.append("</span>");

        return tmpStr.toString();
    }

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

        Directory directory = null;
        IndexReader indexReader = null;
        try {
            directory = FSDirectory.open(Paths.get(indexPath));
            indexReader = DirectoryReader.open(directory);
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

                long id = Long.parseLong(hitDoc.getField("id").stringValue());
                long parentId = hitDoc.getField("parentId").numericValue().longValue();
                if (hitDoc.getField("type") != null) {
                    String type = hitDoc.getField("type").stringValue();
                    searchResult.setType(type);
                }
                String isFile = hitDoc.getField("isFile").stringValue();
                String encrypted = hitDoc.getField("encrypted").stringValue();
                searchResult.setId(id);
                searchResult.setParentId(parentId);

                searchResult.setIsFile(isFile);
                searchResult.setEncrypted(encrypted);


                String notePath = hitDoc.getField("notePath").stringValue();
                String title = hitDoc.get("title");
                if (!StringUtils.isEmpty(title)) {
                    // 获取高亮的文本片段
                    TokenStream titleTokenStream = TokenSources.getTokenStream("title", indexReader.getTermVectors(hit.doc), title, ikAnalyzer, -1);
                    String titleFragment = highlighter.getBestFragment(titleTokenStream, title);

                    searchResult.setResType(SearchResult.Note_Title_Type);
                    searchResult.setResult(titleFragment+getViewValue(notePath));
                }

                String content = hitDoc.get("content");
                if (!StringUtils.isEmpty(content)) {
                    // 获取高亮的文本片段
                    TokenStream contentTokenStream = TokenSources.getTokenStream("content", indexReader.getTermVectors(hit.doc), content, ikAnalyzer, -1);
                    String contentFragment = highlighter.getBestFragment(contentTokenStream, content);

                    searchResult.setResType(SearchResult.Note_Content_Type);
                    searchResult.setResult(contentFragment+getViewValue(notePath));
                }

                searchResults.add(searchResult);
            }
        }catch (Exception e) {
            logger.error("搜索发生错误: ", e);
        } finally {
            tryClose(indexReader, directory);
        }
        return searchResults;
    }

    @Override
    public void delete(Long id) {
        synchronized (syncObj) {
            Directory directory = null;
            IndexWriter indexWriter = null;

            try {
                directory = FSDirectory.open(Paths.get(indexPath));
                IndexWriterConfig config = new IndexWriterConfig(new IKAnalyzer());
                indexWriter = new IndexWriter(directory, config);

                Term idTerm = new Term("id", Long.toString(id));
                indexWriter.deleteDocuments(idTerm);

                indexWriter.commit();
            } catch (Exception e) {
                logger.error("删除索引失败", e);
                throw new RuntimeException(e);
            } finally {
                tryClose( indexWriter, directory);
            }
        }
    }

    @Override
    public void delete(List<Long> ids) {
        synchronized (syncObj) {
            Directory directory = null;
            IndexWriter indexWriter = null;
            try {
                directory = FSDirectory.open(Paths.get(indexPath));
                IndexWriterConfig config = new IndexWriterConfig(new IKAnalyzer());
                indexWriter = new IndexWriter(directory, config);

                for (Long id : ids) {
                    Term idTerm = new Term("id", Long.toString(id));
                    indexWriter.deleteDocuments(idTerm);
                }

                indexWriter.commit();
            } catch (Exception e) {
                logger.error("删除索引失败", e);
                throw new RuntimeException(e);
            } finally {
                tryClose(indexWriter, directory);
            }
        }
    }

    private void tryClose(Object ...objs) {
        for (Object obj : objs) {
            try {
                if (obj instanceof Directory) {
                    ((Directory)obj).close();
                }
                if (obj instanceof IndexWriter) {
                    ((IndexWriter)obj).close();
                }
                if (obj instanceof IndexReader) {
                    ((IndexReader)obj).close();
                }
            } catch (Exception ex) {
                logger.error("tryClose Error", ex);
            }
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

        synchronized (syncObj) {
            Directory directory = null;
            IndexWriterConfig config;
            IndexWriter indexWriter = null;
            try {//delete old index
                File rootDir = new File(indexPath);
                deleteDirectory(rootDir);

                directory = FSDirectory.open(Paths.get(indexPath));
                config = new IndexWriterConfig(new IKAnalyzer());
                indexWriter = new IndexWriter(directory, config);

                List<NoteIndex> noteIndexList = noteIndexMapper.findAll();
                for (NoteIndex noteIndex : noteIndexList) {
                    NoteLuceneIndex noteLuceneIndex = getLuceneIndexFromNoteId(noteIndex.getId());
                    Document document = packDocument(noteLuceneIndex);
                    //添加文档
                    indexWriter.addDocument(document);
                }
//            for (NoteIndex noteIndex : noteIndexList) {
//                String title = noteIndex.getName();
//                Document document = new Document();
//                document.add(new StringField("id", noteIndex.getId().toString(), Field.Store.YES));
//                document.add(new LongPoint("userId", noteIndex.getUserId()));
//                document.add(new StoredField("userId", noteIndex.getUserId()));
//                document.add(new StoredField("parentId", noteIndex.getParentId()));
//                if (!StringUtils.isEmpty(title)) {
//                    document.add(new TextField("title", title, Field.Store.YES));
//                }
//                if ("mysql".equals(noteIndex.getStoreSite())) {
//                    NoteData noteData = noteDataMapper.selectByPrimaryKey(noteIndex.getId());
//                    if (!StringUtils.isEmpty(noteData.getContent())) {
//                        if ("wer".equals(noteIndex.getType())) {
//                            //从mongo中获取
//                            Long noteId = noteIndex.getId();
//                            org.bson.Document mongoDoc = mongoTemplate
//                                    .findOne(org.springframework.data.mongodb.core.query.Query.query(
//                                            org.springframework.data.mongodb.core.query.Criteria.where(NoteConstants.id).is(noteId)),
//                                            org.bson.Document.class,
//                                            NoteConstants.noteWerTextContent);
//                            if (mongoDoc == null) {
//                                logger.warn("根据id: {} 从mongo获取Wer数据为空", noteId);
//                                continue;
//                            }
//                            String textContent = (String)mongoDoc.get(NoteConstants.textContent);
//                            if (!StringUtils.isEmpty(textContent)) {
//                                document.add(new TextField("content", textContent, Field.Store.YES));
//                            } else {
//                                document.add(new TextField("content", noteData.getContent(), Field.Store.YES));
//                            }
//
//                        } else {
//                            document.add(new TextField("content", noteData.getContent(), Field.Store.YES));
//                        }
//                    }
//                }
//                if (!StringUtils.isEmpty(noteIndex.getType()))
//                    document.add(new StoredField("type", noteIndex.getType()));
//                document.add(new StoredField("isFile", noteIndex.getIsFile()));
//                document.add(new LongPoint("createDate", noteIndex.getCreateTime().getTime()));
//                document.add(new StoredField("createDate", noteIndex.getCreateTime().getTime()));
//                document.add(new StoredField("encrypted", noteIndex.getEncrypted()));
//                //do path add
//                String breadcrumbStr = noteIndexService.findBreadcrumbForSearch(noteIndex.getParentId());
//                if (!StringUtils.isEmpty(breadcrumbStr)) {
//                    document.add(new StoredField("notePath", breadcrumbStr));
//                }
//
//                //添加文档
//                indexWriter.addDocument(document);
//            }

                indexWriter.commit();
                logger.info("rebuild index success");
            } catch (Exception e) {
                logger.error("重新建立index失败", e);
                throw new RuntimeException(e);
            } finally {
                tryClose(indexWriter, directory);
            }
        }

    }

    /**
     * 组装Document
     * @param noteLuceneIndex
     * @return
     */
    private Document packDocument(NoteLuceneIndex noteLuceneIndex) {
        Long id = noteLuceneIndex.getId();
        Long userId = noteLuceneIndex.getUserId();
        Long parentId = noteLuceneIndex.getParentId();
        String title = noteLuceneIndex.getTitle();
        String content = noteLuceneIndex.getContent();
        String type = noteLuceneIndex.getType();
        String isFile = noteLuceneIndex.getIsFile();
        Date createDate = noteLuceneIndex.getCreateDate();
        String encrypted = noteLuceneIndex.getEncrypted();

        Document document = new Document();
        document.add(new StringField("id", Long.toString(id), Field.Store.YES));
        document.add(new LongPoint("userId", userId));
        document.add(new StoredField("userId", userId));
        document.add(new StoredField("parentId", parentId));
        if (!StringUtils.isEmpty(title)) {
            document.add(new TextField("title", title, Field.Store.YES));
        }
        if (!StringUtils.isEmpty(content)) {
            document.add(new TextField("content", content, Field.Store.YES));
        }
        if (StringUtils.isNotBlank(type)) {
            document.add(new StoredField("type", type));
        }

        document.add(new StoredField("isFile", isFile));
        document.add(new LongPoint("createDate", createDate.getTime()));
        document.add(new StoredField("createDate", createDate.getTime()));
        document.add(new StoredField("encrypted", encrypted));

        String breadcrumbStr = noteIndexService.findBreadcrumbForSearch(parentId);
        if (!StringUtils.isEmpty(breadcrumbStr)) {
            document.add(new StoredField("notePath", breadcrumbStr));
        }

        return document;
    }


    private void doListUpdate(List<NoteLuceneIndex> noteLuceneIndexList) {
        Directory directory = null;
        IndexWriter indexWriter = null;
        try {
            directory = FSDirectory.open(Paths.get(indexPath));
            IndexWriterConfig config = new IndexWriterConfig(new IKAnalyzer());
            indexWriter = new IndexWriter(directory, config);
            for (NoteLuceneIndex noteLuceneIndex : noteLuceneIndexList) {
                Long id = noteLuceneIndex.getId();

                Term idTerm = new Term("id", Long.toString(id));
                Document document = packDocument(noteLuceneIndex);

                //提交更改并关闭IndexWriter
                indexWriter.updateDocument(idTerm, document);
            }

            indexWriter.commit();
            indexWriter.flush();
            logger.info("index更新完成....");
        } catch (Exception e) {
            logger.error("updateByIds#更新lucene索引失败：", e);
            throw new RuntimeException(e);
        } finally {
            tryClose(indexWriter, directory);
        }
    }


    public void updateByIds(List<Long> ids) {
        List<NoteLuceneIndex> noteLuceneIndexList = ids.stream().map(this::getLuceneIndexFromNoteId).collect(Collectors.toList());
        synchronized (syncObj) {
            doListUpdate(noteLuceneIndexList);
        }
    }

    private NoteLuceneIndex getLuceneIndexFromNoteId(Long id) {
        return noteLuceneDataService.findNoteLuceneDataOne(id);
    }

    public void update(List<NoteLuceneIndex> noteLuceneIndexList) {
        synchronized (syncObj) {
            doListUpdate(noteLuceneIndexList);
        }
    }

    public void update(NoteDataIndex noteDatandex) {
        synchronized (syncObj) {
            NoteLuceneIndex noteLuceneIndex = (NoteLuceneIndex)noteDatandex;
            Directory directory = null;
            IndexWriter indexWriter = null;
            try {
                directory = FSDirectory.open(Paths.get(indexPath));
                IndexWriterConfig config = new IndexWriterConfig(new IKAnalyzer());
                indexWriter = new IndexWriter(directory, config);

                Long id = noteLuceneIndex.getId();
                Term idTerm = new Term("id", Long.toString(id));
                Document document = packDocument(noteLuceneIndex);

                //提交更改并关闭IndexWriter
                indexWriter.updateDocument(idTerm, document);
                indexWriter.commit();
                logger.info("更新完成...{}", id);
            } catch (Exception e) {
                logger.error("更新lucene索引失败：", e);
                throw new RuntimeException(e);
            } finally {
                tryClose(indexWriter, directory);
            }
        }
    }


    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
