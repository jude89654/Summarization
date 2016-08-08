package com.ust.BM25Modified;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.stopword.CoreStopWordDictionary;
import com.hankcs.hanlp.seg.common.Term;
import com.model.Sentence;


import java.util.*;

/**
 * Created by jude8 on 8/1/2016.
 */
public class BM25TextRankSummaryModified {

    final double dampingFactor = 0.85f;

    final int maxIterations = 200;
    final double minimumDifference = 0.001f;

    int numberOfSentences;

    List<Sentence> document;

    TreeMap<Double, Integer> top;

    double[][] weight;

    double[] weightSum;

    double[] vertex;

    BM25Modified bm25Modified;

    public BM25TextRankSummaryModified(List<Sentence> document){
        this.document=document;
        bm25Modified = new BM25Modified(document);
        numberOfSentences = document.size();
        weight = new double[numberOfSentences][numberOfSentences];
        weightSum= new double[numberOfSentences];
        vertex = new double[numberOfSentences];
        top = new TreeMap<Double,Integer>(Collections.reverseOrder());
        solve();
    }


    private void solve(){
        int sentenceIndex=0;
        for(Sentence sentence : document){
            double[] scores = bm25Modified.similarityOfAllSentences(sentence);
            System.out.println(Arrays.toString(scores));

            weight[sentenceIndex] = scores;

            weightSum[sentenceIndex] = sum(scores)-scores[sentenceIndex];

            vertex[sentenceIndex]=1.0;

            sentenceIndex++;
        }

        for(int index =0; index<maxIterations; index++ ){
            double[]m = new double[numberOfSentences];
            double maxDifference = 0;

            for(int innerLoopIndex=0; innerLoopIndex<numberOfSentences;innerLoopIndex++){
                m[index]= 1-dampingFactor;

                for(int innerInnerLoopIndex =0 ; innerInnerLoopIndex<numberOfSentences;innerInnerLoopIndex++){
                    if(innerLoopIndex==innerInnerLoopIndex||weightSum[innerInnerLoopIndex]==0)continue;
                    m[innerInnerLoopIndex]+=(dampingFactor*weight[innerInnerLoopIndex][innerLoopIndex]
                            /weightSum[innerInnerLoopIndex]*vertex[innerInnerLoopIndex]);
                }


                double currentDifference=Math.abs(m[innerLoopIndex]-vertex[innerLoopIndex]);
                //update difference to the current max difference
                if (currentDifference>maxDifference){
                    maxDifference= currentDifference;
                }
            }
            vertex = m;
            if(maxDifference<=minimumDifference)break;
        }

        for(int index=0; index<numberOfSentences;index++){
            top.put(vertex[index],index);
        }
    }

    public int[] getTopSentence(int size){
        Collection<Integer> values = top.values();
        //pipiliin kung maliit ang size na int o yung number ng sentences
        size=Math.min(size,values.size());

        int[] array= new int [size];

        Iterator<Integer> iterator = values.iterator();

        for(int index =0; index<size ; index++){
            array[index]= iterator.next();
        }

        return array;
    }


    private static double sum(double[] array){
        double total =0;
        for(double number: array){
            total+= number;
        }
        return total;
    }

    public static List<Sentence> getTopSentenceList(List<Sentence> document, int size){

        BM25TextRankSummaryModified textRankSummaryModified = new BM25TextRankSummaryModified(document);
        int[] topSentence = textRankSummaryModified.getTopSentence(size);
        List<Sentence> finalResults = new LinkedList();
        for(int index:topSentence){
            finalResults.add(document.get(index));
        }
        return finalResults;

    }


    public static boolean shouldInclude(Term term) {

        return CoreStopWordDictionary.shouldInclude(term);

    }


}
