package com.ust.BM25;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 搜索相关性评分算法
 *
 * @author hankcs
 */
public class BM25 {
    /**
     * The number of document sentences
     */
    int numberOfSentences;

    /**
     * Average length of sentences
     */
    double averageLengthOfSentences;

    /**
     * Split into [Sentence[Word]] yung mga Documents
     */
    List<List<String>> documents;

    /**
     * Sentences and each frequency of a word
     */
    Map<String, Integer>[] frequency;

    /**
     * Lahat ng words ng document na lumilitaw sa ibang mga sentences
     */
    Map<String, Integer> documentFrequency;

    /**
     * IDF
     */
    Map<String, Double> inversedocumentfrequency;

    /**
     * regulators
     */
    final static float k1 = 1.5f;

    /**
     * regulators
     */
    final static float b = 0.75f;

    public BM25(List<List<String>> documents) {
        this.documents = documents;
        numberOfSentences = documents.size();
        for (List<String> sentence : documents) {
            averageLengthOfSentences += sentence.size();
        }
        averageLengthOfSentences /= numberOfSentences;
        frequency = new Map[numberOfSentences];
        documentFrequency = new TreeMap<String, Integer>();
        inversedocumentfrequency = new TreeMap<String, Double>();
        init();
    }

    /**
     * Initialize all parameters at the time of their construction
     */
    private void init() {
        int index = 0;
        for (List<String> sentence : documents) {
            Map<String, Integer> termFrequency = new TreeMap<String, Integer>();
            for (String word : sentence) {
                Integer freq = termFrequency.get(word);
                freq = (freq == null ? 0 : freq) + 1;
                termFrequency.put(word, freq);
            }
            frequency[index] = termFrequency;
            for (Map.Entry<String, Integer> entry : termFrequency.entrySet()) {
                String word = entry.getKey();
                Integer freq = documentFrequency.get(word);
                freq = (freq == null ? 0 : freq) + 1;
                documentFrequency.put(word, freq);
            }
            ++index;
        }
        for (Map.Entry<String, Integer> entry : documentFrequency.entrySet()) {
            String word = entry.getKey();
            Integer freq = entry.getValue();
            inversedocumentfrequency.put(word, Math.log(numberOfSentences - freq + 0.5) - Math.log(freq + 0.5));
        }
    }

    public double similarity(List<String> sentence, int index) {
        double score = 0;
        for (String word : sentence) {
            if (!frequency[index].containsKey(word)) continue;
            int documentSize = documents.get(index).size();
            Integer wordFrequency = frequency[index].get(word);
            score += (inversedocumentfrequency.get(word) * wordFrequency * (k1 + 1)
                    / (wordFrequency + k1 * (1 - b + b * documentSize
                    / averageLengthOfSentences)));
        }

        return score;
    }

    public double[] simAll(List<String> sentence) {
        double[] scores = new double[numberOfSentences];
        for (int index = 0; index < numberOfSentences; ++index) {
            scores[index] = similarity(sentence, index);
        }
        return scores;
    }
}
