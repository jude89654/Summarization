package com.ust.vector;

import com.model.DataSet;
import com.model.Document;
import com.model.Sentence;
import com.model.Topic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
     * @param topic       the topic where the document came from
     * @param document    the document where the sentence came from
     * @param sentence    the Sentence object where the sentence came from.
     * @param vectorSpace the ArrayList which will be the vector Space for the creation of sentence vector.
     * @param sentences   ArrayList of all the sentences in the given topic
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


        System.out.println("[" + sentence.getDocumentId() + "," + sentence.getPosition() + "]");
        for (int termIndex = 0; termIndex < vectorSpace.size(); termIndex++) {

            //point[termIndex] = sentenceTermFrequency(sentence, vectorSpace.get(termIndex)) * inverseSentenceFrequency(sentences,vectorSpace.get(termIndex));
            point[termIndex] = termFrequency(document, vectorSpace.get(termIndex)) * inverseDocumentFrequency(topic, vectorSpace.get(termIndex));
        }

        vector.setPoint(point);
        System.out.println(Arrays.toString(point));

        return vector;
    }

    /**
     * function used to compute the frequency of terms in the document,<br/>
     * after computing the frequency, the frequency is normalized by dividing <br />
     * it with the length of the document.
     * @param document the document Object
     * @param term the term
     * @return the tf value within the document.
     */
    public static double termFrequency(Document document, String term) {
        double count = 0;
        double doclength = 0;
        for (Sentence sentence : document.getSentences()) {
            if (sentence.getFreqMap().containsKey(term)) count += sentence.getFreqMap().get(term);
            doclength += sentence.getSentenceLength();
        }
        return count / doclength;
    }

    /**
     * a method that will compute the inverse document frequency of the term in the given topic.<br />
     * The formula for idf is <b> log( NumberOfDocuments / NumberOfDocumentsWhereTermExists)</b>
     * @param topic
     * @param term
     * @return
     */
    public static double inverseDocumentFrequency(Topic topic, String term) {
        int count = 0;
        int documentCount = 0;

        for (Document document : topic.getDocuments()) {

            for (Sentence sentence : document.getSentences()) {
                //if the term exists in that sentence, means it exist in the document.
                if (sentence.getFreqMap().containsKey(term)) {
                    count++;
                    //pag nakita na yung term, break na agad.
                    break;
                }
            }
            documentCount++;
        }

        //Normalization yung Log
        //System.out.printf("%i / %f \n",documentCount,count);
        return Math.log(documentCount / count);
    }

    public static boolean containsIgnoreCase(List<String> l, String s) {
        Iterator<String> it = l.iterator();
        while (it.hasNext()) {
            if (it.next().equalsIgnoreCase(s))
                return true;
        }
        return false;
    }

    public static Double[] createtfidfVector(Topic topic, Document document, Sentence sentence, ArrayList<String> vectorSpace) {
        ArrayList<Double> vector = new ArrayList<>();

        //for each term in the vector space.
        for (String term : vectorSpace) {
            double point = termFrequency(document, term) * inverseDocumentFrequency(topic, term);
            vector.add(point);
        }
        return vector.toArray(new Double[vector.size()]);

    }

    public static double sentenceTermFrequency(Sentence sentence, String term) {
        if (containsIgnoreCase(sentence.getContent(), term)) {
            return ((double) sentence.getFreqMap().get(term) / sentence.getSentenceLength());
        }
        return 0;
    }

    public static double inverseSentenceFrequency(ArrayList<Sentence> sentences, String term) {
        double count = 0;

        for (Sentence sentence : sentences) {
            if (sentence.getFreqMap().containsKey(term)) count += 1;
        }
        // System.out.println("COUNT:"+count);
        return Math.log(sentences.size() / count);
    }


}