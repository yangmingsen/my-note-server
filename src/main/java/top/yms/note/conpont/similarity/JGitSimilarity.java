package top.yms.note.conpont.similarity;

import org.eclipse.jgit.diff.*;

import java.nio.charset.StandardCharsets;

public class JGitSimilarity implements NoteSimilarity{
    @Override
    public double computeSimilarity(String text1, String text2) throws Exception {
        return calculateTextDifference(text1,text2);
    }

    // 计算两个文本的变更比例

    /**
     * 值的范围：
     *
     * 0.0 → 两个文本完全相同
     *
     * 1.0 → 两个文本完全不一样
     *
     * 0.1 ~ 0.5 → 两个文本部分修改
     *
     * > 0.5 → 改动较大
     * @param oldText
     * @param newText
     * @return
     */
    public static double calculateTextDifference(String oldText, String newText) {
        RawText rawOld = new RawText(oldText.getBytes(StandardCharsets.UTF_8));
        RawText rawNew = new RawText(newText.getBytes(StandardCharsets.UTF_8));
        HistogramDiff diffAlgorithm = new HistogramDiff();
        EditList editList = diffAlgorithm.diff(RawTextComparator.DEFAULT, rawOld, rawNew);
        int totalChanges = 0;
        for (Edit edit : editList) {
            totalChanges += edit.getLengthA() + edit.getLengthB(); // 统计改动的字符数
        }
        //其中：
        //
        //totalChanges：表示 新增 + 删除 的字符总数
        //
        //totalLength：是 旧文本和新文本的总字符数
        int totalLength = oldText.length() + newText.length();
        if (totalLength == 0) return 0; // 避免除零错误
        return (double) totalChanges / totalLength;
    }

}
