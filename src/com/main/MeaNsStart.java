package com.main;

import com.model.DataSet;
import com.model.Document;
import com.model.Sentence;
import com.model.Topic;
import com.ust.BM25Modified.BM25TextRankSummaryModified;
import com.ust.similarity.CosineSimilarity;
import com.ust.tokenizer.TextFileTokenizer;
import com.ust.vector.SentenceVector;
import com.ust.vector.SentenceVectorFactory;
import com.util.StopWords;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.random.JDKRandomGenerator;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


/**
 * @author Jude Bismonte
 */
public class MEANSStart {

    /**
     * Path of the text file containing the stopwords
     */
    private static String STOPWORDSPATH = "StopWords.txt";

    /**
     * String that will be the name of the output directory of the summaries
     */
    final static String outputFolderName = "MeansSummary";

    /**
     * the number of sentences selected by the user
     */
    static int numOfSentences;

    /**
     * eto na yung main method ng thesis namin.
     * @param args not used
     */
    public static void main(String args[]) {

        //initialize StopWords Class
        StopWords.initializeStopWords(STOPWORDSPATH);

        //get the path
        String folderPath = getFolderPath();
        if(folderPath.equals(""))stopProgram();

        //numOfSentences
        numOfSentences = inputNumberOfSentences();

        //Stop the program if no folder is selected


        //for tokenization
        tokenizeFiles(folderPath);


        //initialize dataset
        DataSet dataSet = new DataSet(folderPath);


        //CREATING SUMMARIZATION FOR EACH TOPIC
        for (Topic topic : dataSet.getTopics()) {

            String fileName = dataSet.getTopicName(topic.getTopicId());

            System.out.println("NOW CREATING SUMMARY FOR:" + fileName);

            //cluster the topics
            ArrayList<ArrayList<Sentence>> clusterList = clusterize(topic);

            //get top Sentence from each cluster
            ArrayList<Sentence> topSentenceFromEachCluster = getTopSentencesFromEachCluster(clusterList);

            ArrayList<Sentence> reorderedSentences = reRankTopSentences(topSentenceFromEachCluster);

            try {
                System.out.println("CREATING FILE: Means_ " + fileName);

                createSummaryFile(reorderedSentences, new File(fileName + "_Means.txt"));

                System.out.println("CREATED FILE: Means_" + fileName + "\n\n");
            } catch (IOException ioException) {
                System.out.println("IOEXCEPTION-ERROR IN CREATING FILE:" + fileName);
                ioException.printStackTrace();
            }


        }
        stopProgram();

    }


    /**
     * method used to reorder sentences based on the ranking used by the bm25 algorithm
     * @param topSentences the top sentences extracted
     * @return reordered sentences
     */
    public static ArrayList<Sentence> reRankTopSentences(ArrayList<Sentence> topSentences){
        return new ArrayList<Sentence>(BM25TextRankSummaryModified.getTopSentenceList(new ArrayList(topSentences),numOfSentences));
    }


    /**
     * method to let the user pick a numberOfSentences that will be extracted to create a summary
     *
     * @return an int of what the user has chosen. default is 5 if none is chosen.
     */
    public static int inputNumberOfSentences() {

        String[] noOfSentences = { "5", "6", "7", "8", "9", "10"};
        JFrame jFrame = new JFrame("MEANS SUMMARIZER");


        String number = "5";

        number = "" + JOptionPane.showInputDialog(jFrame, "PICK NUMBER OF SENTENCES"
                , "NO. OF SENTENCES"
                , JOptionPane.QUESTION_MESSAGE
                , null
                , noOfSentences
                , noOfSentences[0]);

        return Integer.parseInt(number);
    }


    /**
     * Method used to extract top sentences from the clusters.
     * @param clusters the clusters.
     * @return topSentences extracted using the bm25 algorithm
     */
    public static ArrayList<Sentence> getTopSentencesFromEachCluster(ArrayList<ArrayList<Sentence>> clusters) {

        ArrayList<Sentence> topSentenceFromAllClusters = new ArrayList<>();

        while (true) {

            for (ArrayList<Sentence> cluster : clusters) {
                //if the cluster is empty, skip
                if(cluster.isEmpty())continue;

                //else get the top sentence from each cluster a
                ArrayList<Sentence> tempList  =new ArrayList<>(BM25TextRankSummaryModified.getTopSentenceList(cluster, 2));

                Sentence tempSentence = tempList.get(0);
                Sentence tempSentence2;
                if(tempList.size()==2) {
                    tempSentence2 = tempList.get(1);
                    topSentenceFromAllClusters.add(tempSentence2);
                    cluster.remove(tempSentence2);
                }

                    //System.out.println("D:"+tempSentence.getDocumentId()+"S:"+tempSentence.getId()+": "+tempSentence.getRefSentence());

                    //add to the final list of sentences
                    topSentenceFromAllClusters.add(tempSentence);

                    //and remove it from the cluster.
                    cluster.remove(tempSentence);

                //end the extraction if the number of extracted is equal to the number of sentences specified by the user
                //if (topSentenceFromAllClusters.size() >= numOfSentences) break;
            }
            if(topSentenceFromAllClusters.size()>numOfSentences)break;

        }

        return topSentenceFromAllClusters;
    }




    /**
     * method to create the summary using the top sentences in the parameter
     * @param topSentences the final summary created by the Bm25
     * @param file         the source text document where the summary came from
     * @throws IOException if there is no permission.
     */
    public static void createSummaryFile(ArrayList<Sentence> topSentences, File file) throws IOException {

        File outputFolder = new File(outputFolderName);
        //making sure that directory exists.
        outputFolder.mkdir();

        File outputFileDirectory = new File(outputFolder + File.separator +"Means_"+ file.getName());

        FileWriter fileWriter = new FileWriter(outputFileDirectory, false);

        String finalSummary = "";

        for (Sentence sentence : topSentences) {
            finalSummary += sentence.getRefSentence() + "\n";
        }
        System.out.println("FINAL SUMMARY:\n"+finalSummary);
        fileWriter.write(finalSummary);
        fileWriter.close();
    }


    /**
     * method that will tokenize text to documents and sentences, and the tokenized and segmented sentences will be put at
     * the /del directory for processing
     *
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
     * method that will clusterize sentences and will return k number of clusters
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


        JDKRandomGenerator jdkRandomGenerator = new JDKRandomGenerator();

        //initializing the clusterer
        KMeansPlusPlusClusterer<SentenceVector> kMeansClusterer = new KMeansPlusPlusClusterer<SentenceVector>(k, 1500, sim,jdkRandomGenerator, KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_POINTS_NUMBER);

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

            //sort the sentences by their position
            Collections.sort(cluster,(a,b)->Long.compare(a.getPosition(),b.getPosition()));

            clusterList.add(cluster);
        }
        return clusterList;
    }


    /**
     * A function that will return an ArrayList of SentenceVectors based on a given Topic
     *
     * @param topic     the topic that will be used to create an populate the vectors
     * @param global    The ArrayList of words that will be the Sentences.
     * @param sentences this will be the ArrayList that will be populated
     * @return an ArrayList of SentenceVectors based on a given topic
     */
    public static ArrayList<SentenceVector> createSentenceVectorList(Topic topic, ArrayList<String> global
            , ArrayList<List<String>> sentences) {

        ArrayList<SentenceVector> sentenceVectors = new ArrayList<>();


        for (Document document : topic.getDocuments()) {
            System.out.println("CREATING SENTENCE VECTORS FOR DOCUMENT NO." + document.getDocumentId());

            System.out.print("CREATING VECTOR FOR SENTENCE NO.");
            for (Sentence sentence : document.getSentences()) {
                System.out.print(sentence.getId() + ", ");
                //creating the currrent SentenceVector from the current sentence vector.
                SentenceVector currentSentenceVector = SentenceVectorFactory.createSentenceVector(document, sentence, global, sentences);

                sentenceVectors.add(currentSentenceVector);
            }
            System.out.println("");
        }
        return sentenceVectors;
    }


    /**
     * The subroutine that will populate the sentence list and global list.
     *
     * @param topic     the topic containing the related documents to be processed
     * @param sentences the ArrayList that contains a List that contains the contents of the sentences that will be updated with the topics document
     * @param global    the ArrayList that would be the list containing all the unique stemmed words
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
