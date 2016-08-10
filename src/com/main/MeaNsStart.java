package com.main;

import com.ust.BM25Modified.BM25TextRankSummaryModified;
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
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


/**
 * @author Jude Bismonte
 *
 */
public class MeaNsStart {

    //lol
    private static String STOPWORDSPATH = "StopWords.txt";

    final static String outputFolderName="MeansSummary";

    static int numOfSentences=5;

    /**
     * eto na yung main method ng thesis namin.
     * @param args not used
     */
    public static void main(String args[]) {

        //initialize StopWords Class
        StopWords.initializeStopWords(STOPWORDSPATH);

        //path where the text files will be found.
        String folderPath = "testTokenize"; //getFolderPath();

        //Stop the program if no folder is selected
        if (folderPath.equals("")) stopProgram();

        //for tokenization
        tokenizeFiles(folderPath);
        File[] file = new File(folderPath).listFiles();

        //initialize dataset
        DataSet dataSet = new DataSet(folderPath);


        //COUNTER FOR DOCUMENT CONSOLE
        int fileIndex = 0;

        //CREATING SUMMARIZATION FOR EACH TOPIC
        for (Topic topic : dataSet.getTopics()) {
            System.out.println("NOW CREATING SUMMARY FOR:" + file[fileIndex].getName());

            //cluster the topics
            ArrayList<ArrayList<Sentence>> clusterList = clusterize(topic);

            ArrayList<ArrayList<Sentence>> summary =buildSummary(clusterList);//method(rank)


            try {
                    createSummaryFile(summary, file[fileIndex]);
            }catch (IOException ioException) {
                System.out.println("IOEXCEPTION-ERROR IN CREATING FILE:" + file[fileIndex].getName());
                ioException.printStackTrace();
            }
            fileIndex++;

        }

    }

    public static ArrayList<ArrayList<Sentence>> buildSummary(ArrayList<ArrayList<Sentence>> clusters){

        int sentencesPerCluster= numOfSentences/clusters.size();

        ArrayList<ArrayList<Sentence>> summary = new ArrayList<>();
        for(ArrayList<Sentence> cluster:clusters){
            ArrayList<Sentence> temp = new ArrayList(BM25TextRankSummaryModified.getTopSentenceList(cluster,sentencesPerCluster));

            summary.add(temp);
        }
        return summary;
    }

    /**
     * @param sentences the final summary created by the Bm25
     * @param file      the source text document where the summary came from
     * @throws IOException
     */
    public static void createSummaryFile(ArrayList<ArrayList<Sentence>> sentences, File file) throws IOException {
        File outputFolder= new File(outputFolderName);
        outputFolder.mkdir();
        String directory = outputFolderName + File.separator + file.getName();
        File outputFile= new File(directory);
        //outputFile.createNewFile();
        FileWriter fileWriter = new FileWriter(outputFile);
        String finalSummary = "";

        for(ArrayList<Sentence> arraylist : sentences){
            for(Sentence sentence: arraylist){
                finalSummary+=sentence.getRefSentence()+" ";
            }
            finalSummary+="\n";
        }
        fileWriter.write(finalSummary);
        fileWriter.close();

        System.out.println("CREATED SUMMARY FOR " + file.getName());
    }


    /**
     * method that will tokenize text to documents and sentences.
     * @param folder the folder that contains all the text documents.
     */
    public static void tokenizeFiles(String folder) {
        try {

            TextFileTokenizer.tokenizeFiles(folder);

        } catch (Exception e) {

            e.printStackTrace();
            System.out.println("MeanNsStart:ERROR IN TOKENIZING FILES=============");
            System.exit(0);
        }
    }

    /**
     * a method that will end the program immediately/
     */
    public static void stopProgram() {
        System.out.println("MEANS SUMMARIZATION PROGRAM ENDED");
        System.exit(0);
    }


    /**
     * a method that will open a JFileChooser and will let the user pick and will return a folder path where the text documents are.
     * @return a String of the folder Directory chosen by the JFileChooser or null if nothing
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


    /**
     * method that will clusterize sentences and will return k clusters with size k
     *
     * @param topic the topic that contains documents
     * @return
     */
    public static ArrayList<ArrayList<Sentence>> clusterize(Topic topic) {

        //all of the unique stemmed words
        ArrayList<String> global = new ArrayList<>();

        //all of the sentences among the documents
        ArrayList<List<String>> sentences = new ArrayList<>();

        preProcess(topic
                , sentences
                , global);

        ArrayList<SentenceVector> sentenceVectors = createSentenceVectorList(topic, global, sentences);

        //Rule of thumb for k
        int k = (int) (Math.sqrt(sentences.size() / 2));

        System.out.println("NUMBER OF K:" + k);

        //Similarity measure used for distance in the clusterer
        CosineSimilarity sim = new CosineSimilarity();
        //initializing the clusterer
        KMeansPlusPlusClusterer<SentenceVector> kMeansClusterer = new KMeansPlusPlusClusterer<SentenceVector>(k, 900, sim);

        ArrayList<ArrayList<Sentence>> clusterList = new ArrayList<>();

        int clusterCount = 0;
        for (CentroidCluster<SentenceVector> centroid : kMeansClusterer.cluster(sentenceVectors)) {
            //System.out.println(centroid.getCenter());
            ArrayList<Sentence> cluster = new ArrayList<>();

            System.out.println("================CLUSTER NO." + (++clusterCount) + "================");
            System.out.println("CLUSTER SIZE:" + (centroid.getPoints().size()));
            for (SentenceVector vector : centroid.getPoints()) {
                cluster.add(vector.getSentence());
                // System.out.println(vector.getSentence().getRefSentence());
            }
            //kung gusto isort by position
            //cluster.sort((a, b) -> a.getPosition() > b.getPosition() ? 1 : 0);
            clusterList.add(cluster);
        }
        //clusterList.sort(new SentencePositionComparator<>);
        return clusterList;
    }


    /**
     * A function that will return an ArrayList of SentenceVectors based on a given Topic
     *
     * @param topic the topic that will be used to create an populate the vectors
     * @param global The ArrayList of words that will be the Sentences.
     * @param sentences this will be the ArrayList that will be populated
     * @return an ArrayList of SentenceVectors based on a given topic
     */
    public static ArrayList<SentenceVector> createSentenceVectorList(Topic topic, ArrayList<String> global
            , ArrayList<List<String>> sentences) {

        ArrayList<SentenceVector> sentenceVectors = new ArrayList<>();

        ///int docNo=0;
        for (Document document : topic.getDocuments()) {
            System.out.println("CREATING SENTENCE VECTORS FOR DOCUMENT NO." + document.getDocumentId());
            // int sentenceNumber=0;
            System.out.print("CREATING VECTOR FOR SENTENCE NO.");
            for (Sentence sentence : document.getSentences()) {
                System.out.print(sentence.getId() + ", ");
                sentenceVectors.add(new SentenceVector(sentence, global, topic, document, sentences));
            }
            System.out.println("");
        }
        return sentenceVectors;
    }


    /**
     * The subroutine that will populate the sentence list and global list.
     *
     * @param topic           the topic containing the related documents to be processed
     * @param sentences       the ArrayList that contains a List that contains the contents of the sentences that will be updated with the topics document
     * @param global          the ArrayList that would be the list containing all the unique stemmed words
     */
    public static void preProcess(Topic topic, ArrayList<List<String>> sentences, ArrayList<String> global) {

        //for each document
        for (Document document : topic.getDocuments()) {
            //for each sentence
            for (Sentence sentence : document.getSentences()) {
                sentences.add(sentence.getContent());
                //for each word
                for (String word : sentence.getContent()) {
                    //if the word is not a stop word and is not within the global
                    if (!global.contains(word) & !StopWords.isStopWord(word)) {
                        global.add(word);
                    }

                }
            }
        }
    }


}
