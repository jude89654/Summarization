package com.main;

import com.ust.similarity.CosineSimilarity;
import com.ust.vector.SentenceVector;
import com.model.DataSet;
import com.model.Document;
import com.model.Sentence;
import com.model.Topic;
import com.util.StopWords;
import com.ust.tokenizer.TextFileTokenizer;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;


import javax.swing.*;
import java.util.*;

/**
 * Created by jude8 on 7/28/2016.
 */
public class MeaNsStart {
    private static String STOPWORDSPATH = "StopWords.txt";


    public static void main(String args[]) {

        //initialize StopWords Class
        StopWords.initializeStopWords(STOPWORDSPATH);

        //path where the text files will be found.
        String folderPath ="testTokenize"; //getFolderPath();

        //if no folder is selected.
        if (folderPath.equals("")) stopProgram();

        //for tokenization
        tokenizeFiles(folderPath);

        //initialize dataset
        DataSet dataSet = new DataSet(folderPath);


        for (Topic topic : dataSet.getTopics()) {
            //Map<String, Double> importantTerms = getImportantTerms(topic);
            ArrayList<ArrayList<Sentence>> clusterList = clusterize(topic);


            //TEXTRANK ALGORITHM HERE
        }


    }



    public static void tokenizeFiles(String folder) {
        try {

            TextFileTokenizer.tokenizeFiles(folder);

        } catch (Exception e) {

            e.printStackTrace();
            System.out.println("MeanNsStart:ERROR IN TOKENIZING FILES=============");
            System.exit(0);
        }
    }

    public static void stopProgram() {
        System.out.println("MEANS SUMMARIZATION PROGRAM ENDED");
        System.exit(0);
    }


    /*
     *
     */
    public static String getFolderPath() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showDialog(new JFrame(), "SELECT FOLDER") == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getPath();
        } else {
            System.out.println("YOU MUST SELECT A FOLDER WHERE THE SOURCE DATA COMES FROM");
            JOptionPane.showConfirmDialog(new JFrame(), "YOU MUST SELECT A FOLDER");
            return "";
        }
    }


    /*
     *method that will return a list of Clusters based on the K-Means Clustering Algorithm.
     * @param topic the topic that will have its documents clustered by sentences.
     * @returns a list that contains k clusters including the sentences.
     */
    public static ArrayList<ArrayList<Sentence>> clusterize(Topic topic) {

        //all of the unique stemmed words
        ArrayList<String> global = new ArrayList<>();

        //all of the sentences among the documents
        ArrayList<List<String>> sentences = new ArrayList<>();

        preProcess(topic, sentences, global);

        ArrayList<SentenceVector> sentenceVectors = createSentenceVectorList(topic, global, sentences);

        //Rule of thumb for k
        int k = (int) (Math.sqrt(sentences.size() /2));
        System.out.println("NUMBER OF K:"+k);

        //Similarity measure used for distance in the clusterer
        CosineSimilarity sim=new CosineSimilarity();
        //initializing the clusterer
        KMeansPlusPlusClusterer<SentenceVector> kMeansClusterer = new KMeansPlusPlusClusterer<SentenceVector>(k,900,sim);

        ArrayList<ArrayList<Sentence>> clusterList = new ArrayList<>();



        int clusterCount=0;
        for (CentroidCluster<SentenceVector> centroid : kMeansClusterer.cluster(sentenceVectors)) {
            //System.out.println(centroid.getCenter());
            ArrayList<Sentence> cluster = new ArrayList<>();

            System.out.println("================CLUSTER NO."+(++clusterCount)+"================");
            System.out.println("CLUSTER SIZE:"+(centroid.getPoints().size()));
            for (SentenceVector vector : centroid.getPoints()) {
                cluster.add(vector.getSentence());
               // System.out.println(vector.getSentence().getRefSentence());
            }
            //kung gusto isort by position
            cluster.sort((a,b)->a.getPosition()>b.getPosition()?1:0);

            clusterList.add(cluster);
        }
        //clusterList.sort(new SentencePositionComparator<>);
        return clusterList;
    }


    /*
     * A function that will return an ArrayList of SentenceVectors based on a given Topic
     * @param topic
     * @param global
     * @param sentences
     * @return an ArrayList of SentenceVectors based on a given topic
     */
    public static ArrayList<SentenceVector> createSentenceVectorList(Topic topic, ArrayList<String> global
            , ArrayList<List<String>> sentences) {

        ArrayList<SentenceVector> sentenceVectors = new ArrayList<>();

        ///int docNo=0;
        for (Document document : topic.getDocuments()) {
            System.out.println("CREATING SENTENCE VECTORS FOR DOCUMENT NO."+document.getDocumentId());
           // int sentenceNumber=0;
            System.out.print("CREATING VECTOR FOR SENTENCE NO.");
            for (Sentence sentence : document.getSentences()) {
                System.out.print(sentence.getId()+", ");
                sentenceVectors.add(new SentenceVector(sentence, global,topic,document, sentences));
            }
            System.out.println("");
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

                for (String word :
                        sentence.getContent()) {

                    if (!global.contains(word)) {
                        global.add(word);
                    }

                }

            }

        }

    }

}
