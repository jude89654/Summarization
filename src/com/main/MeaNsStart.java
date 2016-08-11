package com.main;

import com.model.DataSet;
import com.model.Document;
import com.model.Sentence;
import com.model.Topic;
import com.ust.BM25Modified.BM25TextRankSummaryModified;
import com.ust.similarity.CosineSimilarity;
import com.ust.tokenizer.TextFileTokenizer;
import com.ust.vector.SentenceVector;
import com.util.StopWords;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * @author Jude Bismonte
 *
 */
public class MeaNsStart {

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
        String folderPath = getFolderPath();

        //numOfSentences
        numOfSentences=inputNumberOfSentences();

        //Stop the program if no folder is selected
        if (folderPath.equals("")) stopProgram();

        //for tokenization
        tokenizeFiles(folderPath);
        File[] file = new File(folderPath).listFiles();

        //initialize dataset
        DataSet dataSet = new DataSet(folderPath);



        //CREATING SUMMARIZATION FOR EACH TOPIC
        for (Topic topic : dataSet.getTopics()) {

            String fileName=dataSet.getTopicName(topic.getTopicId());

            System.out.println("NOW CREATING SUMMARY FOR:" + fileName);

            //cluster the topics
            ArrayList<ArrayList<Sentence>> clusterList = clusterize(topic);

            ArrayList<ArrayList<Sentence>> summary =buildSummary(clusterList);//method(rank)

            // TODO: 8/11/2016  kailangan mo irank ang mga top sentences kada cluster :D

            try {
                System.out.println("CREATING FILE " +fileName);
                createSummaryFile(summary,new File(fileName+".txt") );

                System.out.println("CREATED FILE:"+fileName+"\n\n");
            }catch (IOException ioException) {
                System.out.println("IOEXCEPTION-ERROR IN CREATING FILE:" + fileName);
                ioException.printStackTrace();
            }


        }
        stopProgram();

    }


    /**
     * method to let the user pick a numberOfSentences that will be extracted to create a summary
     * @return an int
     */
    public static int inputNumberOfSentences(){

        String[] noOfSentences = {"5","6","7","8","9","10"};
        JFrame jFrame = new JFrame("MEANS SUMMARIZER");

        String number=""+JOptionPane.showInputDialog(jFrame,"PICK NUMBER OF SENTENCES"
                ,"NO. OF SENTENCES"
                ,JOptionPane.QUESTION_MESSAGE
                ,null
                ,noOfSentences
                ,noOfSentences[0]);

        return Integer.parseInt(number);
    }

    /**
     * method used to get the top Sentences in each cluster.
     * @param clusters ArrayList that contains the sentences.
     * @return An ArrayList of ArrayList of sentences.
     */
    public static ArrayList<ArrayList<Sentence>> buildSummary(ArrayList<ArrayList<Sentence>> clusters){


        int computedNumber =(int)Math.ceil(numOfSentences/clusters.size());
        int sentencesPerCluster=(computedNumber==0)?1:computedNumber;

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

        System.out.println("TOP SENTENCES:");
        for(ArrayList<Sentence> arraylist : sentences){
            for(Sentence sentence: arraylist){
                System.out.println("DOC ID: "+sentence.getDocumentId()+" SENTENCE ID:"+sentence.getId());
                System.out.println("SENTENCE:"+sentence.getRefSentence()+"\n");
                finalSummary+=sentence.getRefSentence()+" ";
            }
            finalSummary+="\n";
        }
       // System.out.println("FINAL SUMMARY\n"+finalSummary);
        fileWriter.write(finalSummary);
        fileWriter.close();

        System.out.println("CREATED SUMMARY FOR " + file.getName());
    }

    /**
     * method for creating a summary file for 0\\
     * @param sentences
     * @param file
     * @throws IOException
     */
    public static void createSummaryFile(ArrayList<ArrayList<Sentence>> sentences, String file) throws IOException{
        createSummaryFile(sentences,new File(file));
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
            System.out.println("MeanNsStart:ERROR IN TOKENIZING FILES");
            stopProgram();
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
        Random rand= new Random();
        //initializing the clusterer
        KMeansPlusPlusClusterer<SentenceVector> kMeansClusterer = new KMeansPlusPlusClusterer<SentenceVector>(k, 1500, sim);

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
