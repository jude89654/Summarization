package com.ust.vector;

import com.model.Document;
import com.model.Sentence;
import com.model.Topic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by jude8 on 8/12/2016.
 */
public class SentenceVectorFactory {
    /**
     * SentenceVectorFactory for easy creation of SentenceVectors.
     *
     * @param topic     the topic where the document came from
     * @param document  the document where the sentence came from
     * @param sentence  the Sentence object where the sentence came from.
     * @param vectorSpace    the ArrayList which will be the vector Space for the creation of sentence vector.
     * @param sentences ArrayList of all the sentences in the given topic
     * @return the SentenceVector
     */
    public static SentenceVector createSentenceVector(Topic topic,
                                                      Document document,
                                                      Sentence sentence,
                                                      ArrayList<String> vectorSpace,
                                                      ArrayList<Sentence> sentences) {

        SentenceVector vector = new SentenceVector();
        vector.setSentence(sentence);
        double point[] = new double[vectorSpace.size()];

        System.out.println("["+sentence.getDocumentId()+","+sentence.getPosition()+"]");
        for (int x = 0; x < vectorSpace.size(); x++) {

            point[x] = termFrequency(document, vectorSpace.get(x)) * inverseDocumentFrequency(topic,vectorSpace.get(x));
        }
        System.out.println(Arrays.toString(point));

        vector.setPoint(point);
        return vector;
    }


    public static double sentenceTermFrequency(Sentence sentence, String term) {
        if (containsIgnoreCase(sentence.getContent(),term)) {
            System.out.println("STF:"+ ((double)sentence.getFreqMap().get(term) / sentence.getSentenceLength()));
            return ((double)sentence.getFreqMap().get(term) / sentence.getSentenceLength());
        }
        return 0;
    }

    public static double inverseSentenceFrequency(ArrayList<Sentence> sentences, String term) {
        double count = 0;

        for (Sentence sentence : sentences) {
            if (sentence.getContent().contains(term)) count+=1;
        }
       // System.out.println("COUNT:"+count);
        return (sentences.size()/count);
    }


    public static double termFrequency(Document document, String term) {
        double count = 0;
        double doclength = 0;
        for (Sentence sentence : document.getSentences()) {
            if (sentence.getFreqMap().containsKey(term)) count += sentence.getFreqMap().get(term);
            doclength += sentence.getSentenceLength();
        }
        return count / doclength;
    }

    public static double inverseDocumentFrequency(Topic topic, String term) {
        int count = 0;
        int documentCount = 0;

        for (Document document : topic.getDocuments()) {
            a:for (Sentence sentence : document.getSentences()) {
                if (sentence.getFreqMap().containsKey(term)) {
                    count++;
                    break;
                }
            }
            documentCount++;
        }

        //Normalization yung Log
        //System.out.printf("%i / %f \n",documentCount,count);
        return Math.log(documentCount/count);
    }

    public static boolean containsIgnoreCase(List<String> l, String s) {
        Iterator<String> it = l.iterator();
        while (it.hasNext()) {
            if (it.next().equalsIgnoreCase(s))
                return true;
        }
        return false;
    }
}