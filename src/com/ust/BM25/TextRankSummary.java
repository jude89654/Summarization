package com.ust.BM25;


import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.stopword.CoreStopWordDictionary;
import com.hankcs.hanlp.seg.common.Term;

import java.util.*;

/**
 * TextRank 自动摘要
 *
 * @author hankcs
 */
public class TextRankSummary {
    /**
     * DampingFactor
     */
    final double dampingFactor = 0.85f;
    /**
     * maximum number of iterations
     */
    final int maxIterations = 200;
    final double minimumDifference = 0.001f;

    /**
     * number of sentences in the document
     */
    int numberOfSentences;
    /**
     * Pagsplit kada sentence kada word
     */
    List<List<String>> docs;
    /**
     * Sentences and other sentences relevance
     */
    TreeMap<Double, Integer> top;

    /**
     * relevance ng mga sentences sa isa't isa
     */
    double[][] weight;
    /**
     * The sentences and other sentences relevance of
     */
    double[] weight_sum;
    /**
     * After magiterate, eto na ang total weighr
     */
    double[] vertex;

    /**
     * BM25相似度
     */
    BM25 bm25;

    public TextRankSummary(List<List<String>> docs) {
        this.docs = docs;
        bm25 = new BM25(docs);
        numberOfSentences = docs.size();
        weight = new double[numberOfSentences][numberOfSentences];
        weight_sum = new double[numberOfSentences];
        vertex = new double[numberOfSentences];
        top = new TreeMap<Double, Integer>(Collections.reverseOrder());
        solve();
    }

    private void solve() {
        int cnt = 0;
        for (List<String> sentence : docs) {
            double[] scores = bm25.simAll(sentence);
            System.out.println(Arrays.toString(scores));
            weight[cnt] = scores;
            weight_sum[cnt] = sum(scores) - scores[cnt]; // 减掉自己，自己跟自己肯定最相似
            vertex[cnt] = 1.0;
            ++cnt;
        }
        for (int index = 0; index < maxIterations; ++index) {
            double[] m = new double[numberOfSentences];
            double max_diff = 0;
            for (int i = 0; i < numberOfSentences; ++i) {
                m[i] = 1 - dampingFactor;
                for (int j = 0; j < numberOfSentences; ++j) {
                    if (j == i || weight_sum[j] == 0) continue;
                    m[i] += (dampingFactor * weight[j][i] / weight_sum[j] * vertex[j]);
                }
                double diff = Math.abs(m[i] - vertex[i]);
                if (diff > max_diff) {
                    max_diff = diff;
                }
            }
            vertex = m;
            if (max_diff <= minimumDifference) break;
        }
        // 我们来排个序吧
        for (int i = 0; i < numberOfSentences; ++i) {
            top.put(vertex[i], i);
        }
    }

    /**
     * 获取前几个关键句子
     *
     * @param size number of key sentences
     * @return top key sentences
     */
    public int[] getTopSentence(int size) {
        Collection<Integer> values = top.values();
        size = Math.min(size, values.size());
        int[] indexArray = new int[size];
        Iterator<Integer> it = values.iterator();
        for (int i = 0; i < size; ++i) {
            indexArray[i] = it.next();
        }
        return indexArray;
    }

    /**
     * sum
     *
     * @param array
     * @return
     */
    private static double sum(double[] array) {
        double total = 0;
        for (double v : array) {
            total += v;
        }
        return total;
    }


    public static void main(String[] args) {
        String document = "The point of a key file is that you have something to authenticate with (in contrast to master passwords, where you know something), for example a file on a USB stick. The key file content (i.e. the key data contained within the key file) needs to be kept secret. The point is not to keep the location of the key file secret – selecting a file out of thousands existing on your hard disk basically doesn't increase security at all, because it's very easy for malware/attackers to find out the correct file (for example by observing the last access times of files, the recently used files list of Windows, malware scanner logs, etc.). Trying to keep the key file location secret is security by obscurity, i.e. not really effective. ";
        System.out.println(TextRankSummary.getTopSentenceList(document, 2));
    }

    /**
     * 将文章分割为句子
     *
     * @param document
     * @return
     */
    static List<String> splitSentence(String document) {
        List<String> sentences = new ArrayList<String>();
        if (document == null) return sentences;
        for (String line : document.split("[\r\n]")) {
            line = line.trim();
            if (line.length() == 0) continue;
            for (String sent : line.split("[.!?]")) {
                sent = sent.trim();
                if (sent.length() == 0) continue;
                sentences.add(sent);
            }
        }

        return sentences;
    }

    /**
     * 是否应当将这个term纳入计算，词性属于名词、动词、副词、形容词
     *
     * @param term
     * @return 是否应当
     */
    public static boolean shouldInclude(Term term) {

        return CoreStopWordDictionary.shouldInclude(term);

    }

    /**
     * Word call interface daw
     *
     * @param document the target document
     * @param size     required number of sentences
     * @return List of top key sentences
     */
    public static List<String> getTopSentenceList(String document, int size) {
        List<String> sentenceList = splitSentence(document);
        List<List<String>> docs = new ArrayList<List<String>>();

        //
        for (String sentence : sentenceList) {
            List<Term> termList = HanLP.segment(sentence);
            List<String> wordList = new LinkedList<String>();
            for (Term term : termList) {
                if (shouldInclude(term)) {
                    wordList.add(term.word);
                }
            }
            docs.add(wordList);
        }
        TextRankSummary textRankSummary = new TextRankSummary(docs);
        int[] topSentence = textRankSummary.getTopSentence(size);
        List<String> resultList = new LinkedList<String>();
        for (int i : topSentence) {
            resultList.add(sentenceList.get(i));
        }
        return resultList;
    }
}
