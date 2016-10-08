package com.ust.BM25Modified;

import com.hankcs.hanlp.dictionary.stopword.CoreStopWordDictionary;
import com.hankcs.hanlp.seg.common.Term;
import com.model.DataSet;
import com.model.Document;
import com.model.Sentence;
import com.model.Topic;
import com.util.StopWords;


import java.util.*;

/**
 * Created by jude8 on 8/1/2016.
 */
public class BM25TextRank {

    final double dampingFactor = 0.85f;

    final int maxIterations = 200;

    final double minimumDifference = 0.1f;

    int numberOfSentences;

    List<Sentence> document;

    TreeMap<Double, Integer> top;

    double[][] weight;

    double[] weightSum;

    double[] vertex;

    BM25Modified bm25Modified;

    public BM25TextRank(List<Sentence> document) {
        this.document = document;
        bm25Modified = new BM25Modified(document);
        numberOfSentences = document.size();
        weight = new double[numberOfSentences][numberOfSentences];
        weightSum = new double[numberOfSentences];
        vertex = new double[numberOfSentences];

        //top = new TreeMap<Double, Integer>();

       top = new TreeMap<Double, Integer>(Collections.reverseOrder());
        solve();
    }

    private void solve() {
        int sentenceIndex = 0;
        for (Sentence sentence : document) {
            double[] scores = bm25Modified.simAll(sentence);
            //System.out.println(Arrays.toString(scores));

            weight[sentenceIndex] = scores;

            weightSum[sentenceIndex] = sum(scores) - scores[sentenceIndex];

            vertex[sentenceIndex] = 1.0;

            ++sentenceIndex;
        }

        for (int index = 0; index < maxIterations; index++) {
            double[] m = new double[numberOfSentences];
            double maxDifference = 0;

            for (int innerLoopIndex = 0; innerLoopIndex < numberOfSentences; innerLoopIndex++) {
                m[innerLoopIndex] = 1 - dampingFactor;

                for (int innerInnerLoopIndex = 0; innerInnerLoopIndex < numberOfSentences; innerInnerLoopIndex++) {
                    if (innerLoopIndex == innerInnerLoopIndex || weightSum[innerInnerLoopIndex] == 0) continue;

                    m[innerInnerLoopIndex] += (dampingFactor * weight[innerInnerLoopIndex][innerLoopIndex]
                            / weightSum[innerInnerLoopIndex] * vertex[innerInnerLoopIndex]);
                }


                double currentDifference = Math.abs(m[innerLoopIndex] - vertex[innerLoopIndex]);
                //update difference to the current max difference
                if (currentDifference > maxDifference) {
                    maxDifference = currentDifference;
                }
            }
            vertex = m;
            if (maxDifference <= minimumDifference) break;
        }

        for (int index = 0; index < numberOfSentences; index++) {
            top.put(vertex[index], index);
        }
    }

    public int[] getTopSentence(int size) {
        Collection<Integer> values = top.values();
        //pipiliin kung maliit ang size na int o yung number ng sentences
        size = Math.min(size, values.size());

        int[] array = new int[size];

        Iterator<Integer> iterator = values.iterator();

        for (int index = 0; index < size; index++) {
            array[index] = iterator.next();
        }

        return array;
    }


    private static double sum(double[] array) {
        double total = 0;
        for (double number : array) {
            total += number;
        }
        return total;
    }

    /**
     * method used to get the top sentences in the list of sentences.
     * @param document
     * @param size
     * @return
     */

    public static void main(String args[]){
        StopWords.initializeStopWords("StopWords.txt");
       // try {
          //  TextFileTokenizer.tokenizeFiles("FOR PROFESSORS");
       // } catch (IOException e) {
       //     e.printStackTrace();
       // }
        DataSet dataSet = new DataSet("FOR PROFESSORS");

        for (Topic topic:dataSet.getTopics()){
            Document document = topic.getDocuments().get(0);
            List<Sentence> top = getTopSentenceList(document.getSentences(),5);
            System.out.println("SUMMARY:");
            for (Sentence topS:
                 top) {

                System.out.println(topS.getRefSentence());

            }
        }

    }

    public static List<Sentence> getTopSentenceList(List<Sentence> document, int size) {

        BM25TextRank textRankSummaryModified = new BM25TextRank(document);
        int[] topSentence = textRankSummaryModified.getTopSentence(size);
        //System.out.println(Arrays.toString(topSentence));
        List<Sentence> finalResults = new LinkedList();
        for (int index : topSentence) {
            finalResults.add(document.get(index));
        }
        return finalResults;

    }


    public static boolean shouldInclude(Term term) {
        return CoreStopWordDictionary.shouldInclude(term);

    }

}
