package com.ust.vector;

import com.model.Document;
import com.model.Sentence;
import com.model.Topic;
import org.apache.commons.math3.ml.clustering.Clusterable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jude8 on 7/29/2016.
 */
public class SentenceVector implements Clusterable {

    public void setSentence(Sentence sentence) {
        this.sentence = sentence;
    }

    /**
     *
     * @param point
     */
    public void setPoint(double[] point) {
        this.point = point;
    }

    Sentence sentence;
     double[] point;


    /**
     * empty constructor para sa SentenceVector Factory
     */
    public SentenceVector(){}
    /**
     * method to initialize the SentenceVector
     * @param sentence the Sentence
     * @param global  ArrayList of Stingtotal unique words without stop words in all of the documents.
     * @param topic The topic of where the sentence came from
     * @param document The Document where the sentence came from
     * @param sentences The ArrayList of all sentences.
     */
    public SentenceVector(Sentence sentence, ArrayList<String> global, Topic topic, Document document, ArrayList<List<String>> sentences){

        this.sentence = sentence;

        //initializing point
        point=new double[global.size()];

        //Populating the vector values based on tf-idf
        for(int x=0;x<global.size();x++){
            point[x]= termFrequency(sentence.getContent(),global.get(x))
                    * inverseDocumentFrequency(sentences,global.get(x));
        }

    }


    /**
     * method that returns the sentence
     * @return returns the Sentence
     */
    public Sentence getSentence(){
        return sentence;
    }


    /**
     *implemented abstract method for clustering.
     *@return the point
     */
    @Override
    public double[] getPoint() {
        return point;
    }

    /**
     * method that calculates the inverse document frequency based on a term given by the vector space
     * @param sentences The list of all sentences of the topic
     * @param term The term that will be used to compute the inverse - document frequency
     * @return the idf between the sentences and term
     */
    static double inverseDocumentFrequency(ArrayList<List<String>> sentences, String term) {
        double count = 0;
        double total = 0;

        for (List<String> words : sentences)
            a:for (String word : words) {
                if (word.equalsIgnoreCase(term.toUpperCase())) {
                    count++;
                    break a;
                }
            }

        //for normalization of scores.
        return Math.log(sentences.size() / count);
    }

    static double inverseDocumentFrequency(Topic topic,String term){
        int count=0;
        for(Document document:topic.getDocuments()){
           z: for(Sentence sentence: document.getSentences()){
                for(String word:sentence.getContent()){
                    if(word.equals(term)) {
                        count++;
                        break z;
                    }
                }
            }
        }
        return Math.log(topic.getDocuments().size()/count);
    }


    /**
     *method that will compute the term frequency among the document
     *@param sentenceContent the List that contains the stemmed words of the sentences
     *@param term, the term that will compute the term frequency
     *@return for normalization it will return a double that is equal to freuqncy divided by the length of the sentence.
     */

    static double termFrequency(List<String> sentenceContent, String term) {
        double count = 0;
        double total = 0;for (String word : sentenceContent) {
            if (word.equalsIgnoreCase(term)) {

                count++;
            }
        }
        return count / sentenceContent.size();
    }

    static double termFrequency(Document document,String term){
        int count =0;
        int docLength=0;
        for(Sentence sentence:document.getSentences()){
            for(String word:sentence.getContent()){
                if(word.equalsIgnoreCase(term)){
                   count++;
                }
                docLength++;
            }
        }
        return count/docLength;
    }



}


