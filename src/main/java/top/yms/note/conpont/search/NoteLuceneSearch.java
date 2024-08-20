package top.yms.note.conpont.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.yms.note.comm.Constants;
import top.yms.note.conpont.NoteSearch;
import top.yms.note.dto.NoteSearchDto;
import top.yms.note.entity.SearchLog;
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

import java.nio.file.Paths;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yangmingsen on 2024/8/21.
 */
@Component(Constants.noteLuceneSearch)
public class NoteLuceneSearch implements NoteSearch {
    private final static Logger logger = LoggerFactory.getLogger(NoteLuceneSearch.class);

    public final static String indexPath = "E:\\tmp\\note-search-index\\";

    @Autowired
    NoteSearchLogService noteSearchLogService;

    @Autowired
    IdWorker idWorker;

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
}
