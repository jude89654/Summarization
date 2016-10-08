package com.ust.BM25Modified;

import com.model.Sentence;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by jude8 on 7/31/2016.
 */
public class BM25Modified {
    int numberOfSentences;

    double averageLengthOfSentences;

    List<Sentence> document;


    Map<String, Integer>[] frequency;

    Map<String, Integer> documentFrequency;

    Map<String,Double> inverseDocumentFrequency;

    /**
     * REGULATORS
     */
    final static float k1=1.5f;

    /**
     * REGULATORS
     */
    final static float b=0.75f;

    public BM25Modified(List<Sentence> document){
        this.document = document;
        numberOfSentences = document.size();

        //getting the average of words in the document
        for(Sentence sentence: document){
            averageLengthOfSentences+=sentence.getContent().size();
        }
        averageLengthOfSentences/=numberOfSentences;

        //create a frequency map per sentence(term, frequency)
        frequency  = new Map[numberOfSentences];

        documentFrequency = new TreeMap<String, Integer>();
        inverseDocumentFrequency = new TreeMap<String,Double>();

        initialize();
    }

    private void initialize(){
        int index=0;

        for(Sentence sentence: document) {
            Map<String, Integer> termFrequency = new TreeMap();


            //update the frequency of the word by sentence
            for (String word : sentence.getContent()) {
                Integer freq = termFrequency.get(word);
                freq = (freq == null ? 0 : freq) + 1;
                termFrequency.put(word, freq);
            }
            //lagay sa frequency table yung map ng sentence
            frequency[index] = termFrequency;

            //frequency naman per document?
            for (Map.Entry<String, Integer> entry : termFrequency.entrySet()) {
                String word = entry.getKey();
                Integer frequency = documentFrequency.get(word);
                frequency = (frequency == null ? 0 : frequency) + 1;
                documentFrequency.put(word, frequency);
            }
            ++index;
        }
        //computation for the inverse document frequency
        for (Map.Entry<String, Integer> entry : documentFrequency.entrySet()) {
            String word = entry.getKey();
            Integer freq = entry.getValue();
            inverseDocumentFrequency.put(word, Math.log(numberOfSentences - freq + 0.5) - Math.log(freq + 0.5));
        }
    }

    public double sim(Sentence sentence, int index){
        double score=0;

        for(String word:sentence.getContent()){
            if(!frequency[index].containsKey(word)){
                continue;
            }
            int documentSize = document.get(index).getSentenceLength();

            Integer wordFrequency = frequency[index].get(word);

            score+=(inverseDocumentFrequency.get(word)*wordFrequency*(k1+1)
            /(wordFrequency+k1*((1-b)+(b*documentSize)/averageLengthOfSentences)));
        }

        return score;
    }

    public double[] simAll(Sentence sentence){
        double[] scores = new double[numberOfSentences];

        for(int index = 0; index < numberOfSentences ; ++index){
            scores[index] = sim(sentence,index);
        }
        return scores;
    }

}
