package top.yms.note.conpont.similarity;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class CosineSimilarity implements NoteSimilarity{
    @Override
    public double computeSimilarity(String text1, String text2) throws Exception{
        try {
            // 1. 先用 IKAnalyzer 进行分词
            List<List<String>> tokenizedVersions = new ArrayList<>();
            tokenizedVersions.add(tokenizeChinese(text1));
            tokenizedVersions.add(tokenizeChinese(text2));
            // 2. 计算 TF-IDF 向量
            Map<Integer, Map<String, Double>> tfidfVectors = computeTFIDFVectors(tokenizedVersions);
            return cosineSimilarity(tfidfVectors.get(0), tfidfVectors.get(1));
        } catch (Exception e) {
            throw e;
        }
    }

    // IKAnalyzer 分词
    public List<String> tokenizeChinese(String text) throws IOException {
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
