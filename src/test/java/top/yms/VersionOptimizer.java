package top.yms;

import org.apache.commons.math3.linear.*;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import org.apache.lucene.analysis.Analyzer;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
public class VersionOptimizer {
    private static final double SIMILARITY_THRESHOLD = 0.9; // 设定相似度阈值

    public static void fun1() throws Exception{
        List<String> versions = Arrays.asList(
                "今天是个好天气，我很开心。",
                "今天的天气真不错，我非常开心。",
                "我去公园散步，感觉很不错。",
                "下午去公园散步，感受到大自然的美好。",
                "晚上回家了，今天的一天结束了。"
        );

        List<String> optimizedVersions = optimizeVersions(versions);
        System.out.println("优化后的版本列表：" + optimizedVersions);
    }

    public static List<String> optimizeVersions(List<String> versions) throws IOException {
        // 1. 先用 IKAnalyzer 进行分词
        List<List<String>> tokenizedVersions = new ArrayList<>();
        for (String version : versions) {
            tokenizedVersions.add(tokenizeChinese(version));
        }
        // 2. 计算 TF-IDF 向量
        Map<Integer, Map<String, Double>> tfidfVectors = computeTFIDFVectors(tokenizedVersions);
        // 3. 计算相似度并优化版本
        List<String> optimized = new ArrayList<>();
        for (int i = 0; i < versions.size() - 1; i++) {
            double similarity = cosineSimilarity(tfidfVectors.get(i), tfidfVectors.get(i + 1));
            System.out.println("版本 " + (i + 1) + " 和版本 " + (i + 2) + " 相似度：" + similarity);

            if (similarity < SIMILARITY_THRESHOLD) {
                optimized.add(versions.get(i));
            }
        }
        optimized.add(versions.get(versions.size() - 1)); // 保留最后一个版本
        return optimized;
    }

    // IKAnalyzer 分词
    public static List<String> tokenizeChinese(String text) throws IOException {
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

    public static Map<Integer, Map<String, Double>> computeTFIDFVectors(List<List<String>> tokenizedDocs) {
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

    public static double cosineSimilarity(Map<String, Double> vectorA, Map<String, Double> vectorB) {
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