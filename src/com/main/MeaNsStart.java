package com.main;

import clustering.apache.SentenceVector;
import clustering.jonathanzong.kmeansClusterer;
import com.model.DataSet;
import com.model.Document;
import com.model.Sentence;
import com.model.Topic;
import com.util.StopWords;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import sun.nio.ch.DatagramSocketAdaptor;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jude8 on 7/28/2016.
 */
public class MeaNsStart {
    private static String STOPWORDSPATH = "StopWords.txt";



    public static void main(String args[]) {

        StopWords.initializeStopWords(STOPWORDSPATH);

        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showDialog(new JFrame(), "SELECT FOLDER") == JFileChooser.APPROVE_OPTION) {
            DataSet dataSet = new DataSet(chooser.getSelectedFile().getPath());
            for (Topic currentTopic : dataSet.getTopics()) {
                ArrayList<ArrayList<Sentence>> clusters = clusterize(currentTopic);
                
                /*INSERT TEXTRANK ALGO HERE*/


            }
        } else {
            System.out.println("YOU MUST SELECT A FOLDER WHERE THE SOURCE DATA COMES FROM");
            JOptionPane.showConfirmDialog(new JFrame(), "YOU MUST SELECT A FOLDER");
        }

    }

/*
 *method that will return a list of Clusters based on the K-Means Clustering Algorithm.
 * @param topic the topic that will have its documents clustered by sentences.
 */
    public static ArrayList<ArrayList<Sentence>> clusterize(Topic topic) {



        //all of the unique stemmed words
        ArrayList<String> global = new ArrayList<>();

        //all of the sentences among the documents
        ArrayList<List<String>> sentences = new ArrayList<>();

        preProcess(topic, sentences, global);

        ArrayList<SentenceVector> sentenceVectors = createSentenceVectorList(topic,global,sentences);


        //Rule of thumb ng k
        int k = (int) (Math.sqrt(sentences.size()/2));

        KMeansPlusPlusClusterer<SentenceVector> kMeansClusterer = new KMeansPlusPlusClusterer<SentenceVector>(k,1000);

        ArrayList<ArrayList<Sentence>>clusterList= new ArrayList<>();
        for(CentroidCluster<SentenceVector> centroid : kMeansClusterer.cluster(sentenceVectors)){
            ArrayList<Sentence> cluster = new ArrayList<>();
            for (SentenceVector vector : centroid.getPoints()){
                cluster.add(vector.getSentence());
            }
            clusterList.add(cluster);
        }

        return clusterList;
    }


    /*
     * A function that will return an ArrayList of SentenceVectors based on a given Topic
     * @param topic
     * @param global
     * @param sentences
     * @return an ArrayList of SentenceVectors based on a given topic
     */
    public static ArrayList<SentenceVector> createSentenceVectorList(Topic topic,ArrayList<String> global,ArrayList<String[]> sentences){
        ArrayList<SentenceVector> sentenceVectors = new ArrayList<>();

        for(Document document:topic.getDocuments()){
            for(Sentence sentence : document.getSentences()){
                sentenceVectors.add(new SentenceVector(sentence,global,sentences));
            }
        }
        return sentenceVectors;
    }


    /*
     * The subroutine that will populate the sentence list and global list.
     * @param topic the topic containing the related documents to be processed
     * @param sentences the ArrayList that contains a List that contains the contents of the sentences that will be updated with the topics document
     * @param global the ArrayList that would be the list containing all the unique stemmed words
     * @param sentenceVectors
     */
    public static void preProcess(Topic topic, ArrayList<List<String>> sentences, ArrayList<String> global) {
        for (Document document :
                topic.getDocuments()) {

            for (Sentence sentence :
                    document.getSentences()) {

                sentences.add(sentence.getContent());

                for(String word :
                        sentence.getContent()){

                    if(!global.contains(word)){
                        global.add(word);
                    }

                }

            }

        }

    }

}
