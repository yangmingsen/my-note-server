package top.yms.note.conpont.content;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.wltea.analyzer.lucene.IKAnalyzer;
import top.yms.note.comm.CommonErrorCode;
import top.yms.note.comm.NoteConstants;
import top.yms.note.comm.NoteSystemException;
import top.yms.note.conpont.NoteContentOptimizeService;
import top.yms.note.entity.NoteDataVersion;
import top.yms.note.exception.BusinessException;
import top.yms.note.service.NoteDataService;
import top.yms.note.service.NoteIndexService;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

@Component(NoteConstants.noteContentOptimizeServiceImpl)
public class NoteContentOptimizeServiceImpl implements NoteContentOptimizeService {

    private static final Logger log = LoggerFactory.getLogger(NoteContentOptimizeServiceImpl.class);

    @Resource
    private NoteIndexService noteIndexService;

    @Resource
    private NoteDataService noteDataService;

    @Override
    public List<Long> removeOneUnnecessaryVersion(Long id) {
        List<NoteDataVersion> dataVersionList = noteDataService.findDataVersionList(id);
        if (dataVersionList.isEmpty()) {
            log.info("id={}, dataVersion is empty", id);
            return Collections.emptyList();
        }
        List<Long> ids = Collections.emptyList();
        try {
            ids = getRemoveVersionIds(dataVersionList);
            log.info("noteId={}, delList={}", id, ids);
            for (Long ndvId : ids) {
                //noteDataService.deleteDataVersion(ndvId);
            }
        } catch (Exception e) {
            log.error("removeOneUnnecessaryVersion 发生异常", e);
        }
        return ids;
    }

    @Override
    public void removeAllUnnecessaryVersion() {
        throw new NoteSystemException(CommonErrorCode.E_200211);
    }

    /**
     * 提高合并阈值（比如从 0.2 提高到 0.3 或 0.4）：减少保留的版本数量，进一步优化存储空间。
     *
     * 降低合并阈值（比如从 0.2 降低到 0.1）：保留更多的细微改动，防止过度合并影响版本追踪。
     * <p>
     *     在文本处理中，每个文本被转换为 词向量，然后计算它们的夹角：
     *
     * 相似度 = 1 → 两个文本完全相同
     *
     * 相似度接近 1 → 说明两个文本内容高度相似
     *
     * 相似度接近 0 → 说明两个文本几乎没有相似之处
     *
     * 相似度 = 0 → 说明两个文本完全不同
     * </p>
     */
    private double SIMILARITY_THRESHOLD = 0.95; // 设定相似度阈值

    public List<Long> getRemoveVersionIds(List<NoteDataVersion> noteDataVersions) throws IOException {
        // 1. 先用 IKAnalyzer 进行分词
        List<List<String>> tokenizedVersions = new ArrayList<>();
        for (NoteDataVersion version : noteDataVersions) {
            tokenizedVersions.add(tokenizeChinese(version.getContent()));
        }
        // 2. 计算 TF-IDF 向量
        Map<Integer, Map<String, Double>> tfidfVectors = computeTFIDFVectors(tokenizedVersions);
        // 3. 计算相似度并优化版本
        List<Long> optimizedIds = new ArrayList<>();
//        for (int i = 0; i < noteDataVersions.size() - 1; i++) {
//            NoteDataVersion noteDataVersion = noteDataVersions.get(i);
//            NoteDataVersion noteDataVersion2 = noteDataVersions.get(i+1);
//            double similarity = cosineSimilarity(tfidfVectors.get(i), tfidfVectors.get(i + 1));
//            log.info("noteId={}, 版本={} 和版本={} 相似度={}",
//                    noteDataVersion.getNoteId(), noteDataVersion.getId(), noteDataVersion2.getId(), similarity);
//            if (similarity > SIMILARITY_THRESHOLD) {
//                optimizedIds.add(noteDataVersion.getId());
//            }
//        }
        int len = noteDataVersions.size();
        int e = 1;
        if (len > 3) {
            for (int i = len-1; i > e; ) {
                Map<String, Double> imap = tfidfVectors.get(i);
                NoteDataVersion indv = noteDataVersions.get(i);
                for (i=i-1; i > e ; i--) {
                    Map<String, Double> jmap = tfidfVectors.get(i);
                    NoteDataVersion jndv = noteDataVersions.get(i);
                    double similarity = cosineSimilarity(imap, jmap);
                    log.info("noteId={}, 版本={} 和版本={} 相似度={}",
                            indv.getNoteId(), indv.getId(), jndv.getId(), similarity);
                    if (similarity > SIMILARITY_THRESHOLD) {
                        optimizedIds.add(jndv.getId());
                    } else {
                        break;
                    }
                }
            }
        }
        return optimizedIds;
    }

    // IKAnalyzer 分词
    public  List<String> tokenizeChinese(String text) throws IOException {
        List<String> result = new ArrayList<>();
        Analyzer analyzer = new IKAnalyzer(true);
        TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(text));
        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            CharTermAttribute attr = tokenStream.getAttribute(CharTermAttribute.class);
            result.add(attr.toString());
        }
        tokenStream.close();
        analyzer.close();
        return result;
    }

    public  Map<Integer, Map<String, Double>> computeTFIDFVectors(List<List<String>> tokenizedDocs) {
        Map<String, Integer> dfMap = new HashMap<>();
        int numDocs = tokenizedDocs.size();
        // 计算 DF（文档频率）
        for (List<String> tokens : tokenizedDocs) {
            Set<String> uniqueTerms = new HashSet<>(tokens);
            for (String term : uniqueTerms) {
                dfMap.put(term, dfMap.getOrDefault(term, 0) + 1);
            }
        }
        // 计算 TF-IDF 向量
        Map<Integer, Map<String, Double>> tfidfVectors = new HashMap<>();
        for (int docId = 0; docId < numDocs; docId++) {
            Map<String, Double> tfidfVector = new HashMap<>();
            Map<String, Integer> termCount = new HashMap<>();
            for (String term : tokenizedDocs.get(docId)) {
                termCount.put(term, termCount.getOrDefault(term, 0) + 1);
            }
            int docLength = tokenizedDocs.get(docId).size();
            for (String term : termCount.keySet()) {
                double tf = (double) termCount.get(term) / docLength;
                double idf = Math.log(1 + (double) numDocs / (dfMap.get(term) + 1));
                tfidfVector.put(term, tf * idf);
            }
            tfidfVectors.put(docId, tfidfVector);
        }
        return tfidfVectors;
    }

    public  double cosineSimilarity(Map<String, Double> vectorA, Map<String, Double> vectorB) {
        Set<String> allTerms = new HashSet<>(vectorA.keySet());
        allTerms.addAll(vectorB.keySet());
        double[] vecA = new double[allTerms.size()];
        double[] vecB = new double[allTerms.size()];
        int index = 0;
        for (String term : allTerms) {
            vecA[index] = vectorA.getOrDefault(term, 0.0);
            vecB[index] = vectorB.getOrDefault(term, 0.0);
            index++;
        }
        RealVector realVecA = new ArrayRealVector(vecA);
        RealVector realVecB = new ArrayRealVector(vecB);
        return realVecA.cosine(realVecB);
    }
}
