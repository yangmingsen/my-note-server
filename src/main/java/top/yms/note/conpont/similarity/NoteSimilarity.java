package top.yms.note.conpont.similarity;

public interface NoteSimilarity {
    double computeSimilarity(String text1, String text2) throws Exception;
}
