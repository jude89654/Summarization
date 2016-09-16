package com.ust.vector;

import com.model.Document;
import com.model.Sentence;
import com.model.Topic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A module for creating Sentence vectors that will be used in the clustering sented
 * Created by jude8 on 8/12/2016.
 */
public class SentenceVectorFactory {
    /**
     * SentenceVectorFactory for easy creation of SentenceVectors.
     * @param topic the topic where the document came from
     * @param document the document where the sentence came from
     * @param sentence the Sentence object where the sentence came from.
     * @param global the ArrayList which will be the vector Space of
     * @param sentences
     * @return the SentenceVector
     */
    public static SentenceVector createSentenceVector(Topic topic,
                                                      Document document,
                                                      Sentence sentence,
                                                      ArrayList<String> global,
                                                      ArrayList<List<String>> sentences){
        SentenceVector vector = new SentenceVector();
        vector.setSentence(sentence);
        double point[] = new double[global.size()];

        for(int x=0;x<global.size();x++){
            point[x]= termFrequency(document,global.get(x))
                    * inverseDocumentFrequency(topic,global.get(x));
        }
        //System.out.println("SENTENCE NO."+sentence.getId()+":"+Arrays.toString(point));
        vector.setPoint(point);
        return vector;
    }

    public static double termFrequency(Document document, String term){
        double count = 0;
        double doclength=0;
        for (Sentence sentence : document.getSentences()) {
            if(sentence.getFreqMap().containsKey(term)) count += sentence.getFreqMap().get(term);
            doclength+=sentence.getSentenceLength();
        }
        return count/doclength;
    }

    public static double inverseDocumentFrequency(Topic topic, String term){
        int count =0;

        for(Document document:topic.getDocuments()) {
            for (Sentence sentence : document.getSentences()) {
                if(sentence.getFreqMap().containsKey(term)){
                    count++;
                    break;
                }
            }
        }
        return count;
    }
}
