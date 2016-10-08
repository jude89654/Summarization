package com.main;

import com.model.DataSet;
import com.model.Document;
import com.model.Sentence;
import com.model.Topic;
import com.ust.BM25Modified.BM25TextRank;
import com.ust.similarity.CosineSimilarity;
import com.ust.tokenizer.TextFileTokenizer;
import com.ust.vector.SentenceVector;
import com.ust.vector.SentenceVectorFactory;
import com.util.StopWords;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.SynchronizedRandomGenerator;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;


/**
 * @author Jude Bismonte
 */
public class MEANSStart {

    static String systemName = "MEANS";

    /**
     * Path of the text file containing the stopwords
     */
    static String STOPWORDSPATH = "StopWords.txt";

    /**
     * String that will be the name of the output directory of the summaries
     */
    static String outputFolderName = "MeansSummary";

    /**
     * the number of sentences selected by the user
     */
    static int numOfSentences = 4;

    /**
     * setting this to true will remove short sentence according to the minimum sentence length.
     */
    static boolean removeShortSentences =true;

    /**
     * if removeShortSentences is true, it will remove sentences with less than minimumSentenceLengh
     */
    static int minimumSentenceLength = 7;

    /**
     * eto na yung main method ng thesis namin.
     *
     * @param args not used
     */
    public static void main(String args[]) {

        //initialize StopWords Class
        StopWords.initializeStopWords(STOPWORDSPATH);

        //get the path
        String folderPath = getFolderPath();
        //if (folderPath.equals("")) stopProgram();

        //numOfSentences
        //numOfSentences = inputNumberOfSentences();

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
                createSummaryFile(reorderedSentences, new File(fileName + "_" + systemName + ".txt"));
            } catch (IOException ioException) {
                System.out.println("IOEXCEPTION-ERROR IN CREATING FILE:" + fileName);
                ioException.printStackTrace();
            }


        }
        stopProgram();

    }


    /**
     * method used to reorder sentences based on the ranking used by the bm25 algorithm
     *
     * @param topSentences the top sentences extracted
     * @return reordered sentences
     */
    public static ArrayList<Sentence> reRankTopSentences(ArrayList<Sentence> topSentences) {
        return new ArrayList<Sentence>(BM25TextRank.getTopSentenceList(topSentences, numOfSentences));
    }


    /**
     * method to let the user pick a numberOfSentences that will be extracted to create a summary
     *
     * @return an int of what the user has chosen. default is 5 if none is chosen.
     */
    public static int inputNumberOfSentences() {

        String[] noOfSentences = {"5", "6", "7", "8", "9", "10"};
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
     *
     * @param clusters the clusters.
     * @return topSentences extracted using the bm25 algorithm
     */
    public static ArrayList<Sentence> getTopSentencesFromEachCluster(ArrayList<ArrayList<Sentence>> clusters) {

        ArrayList<Sentence> topSentenceFromAllClusters = new ArrayList<>();

        while (true) {

            for (ArrayList<Sentence> cluster : clusters) {
                //if the cluster is empty, skip
                if (cluster.isEmpty()) continue;

                //else get the top sentence from each cluster a
                ArrayList<Sentence> tempList = new ArrayList<>(BM25TextRank
                        .getTopSentenceList(cluster, numOfSentences));

                for (Sentence tempSentence : tempList) {
                    topSentenceFromAllClusters.add(tempSentence);
                    cluster.remove(tempSentence);
                }

            }
            if (topSentenceFromAllClusters.size() > numOfSentences) break;
        }

        sortSentences(topSentenceFromAllClusters);

        return topSentenceFromAllClusters;
    }


    /**
     * method to create the summary using the top sentences in the parameter
     *
     * @param topSentences the final summary created by the Bm25
     * @param file         the source text document where the summary came from
     * @throws IOException if there is no permission.
     */
    public static void createSummaryFile(ArrayList<Sentence> topSentences, File file) throws IOException {

        System.out.println("CREATING FILE:" + file.getName());

        File outputFolder = new File(outputFolderName);
        //making sure that directory exists.
        outputFolder.mkdir();

        File outputFileDirectory = new File(outputFolder + File.separator + file.getName());

        FileWriter fileWriter = new FileWriter(outputFileDirectory, false);

        String finalSummary = "";

        for (Sentence sentence : topSentences) {
            finalSummary += sentence.getRefSentence() + "\n";
        }
        System.out.println("FINAL SUMMARY:\n" + finalSummary);
        fileWriter.write(finalSummary);
        fileWriter.close();
        System.out.println("FILE CREATED:" + file.getName());
    }


    /**
     * method that will tokenize text to documents and sentences,
     * and the tokenized and segmented sentences will be put at
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
     * a method that will open a JFileChooser and will let the user pick and will
     * return a folder path where the text documents are.
     *
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
            stopProgram();
            return "";
        }
    }


    /**
     * method that will clusterize sentences and will return k number of clusters
     *
     * @param topic the topic that contains documents
     * @return
     */
    public static ArrayList<ArrayList<Sentence>> clusterize(Topic topic) {

        //all of the unique stemmed words
        ArrayList<String> vectorSpace
                = new ArrayList<>();

        //all of the sentences among the documents
        ArrayList<Sentence> sentences = new ArrayList<>();

        preProcess(topic, sentences, vectorSpace);

        ArrayList<SentenceVector> sentenceVectors = createSentenceVectorList(topic, vectorSpace, sentences);

        //Rule of thumb for k
        int k = (int) (Math.sqrt(sentences.size() / 2));


        System.out.println("NUMBER OF CLUSTERS:" + k);
        //Similarity measure used for distance in the clusterer
        CosineSimilarity cosineSimilarity = new CosineSimilarity();

        SynchronizedRandomGenerator s = new SynchronizedRandomGenerator(new JDKRandomGenerator());

        //initializing the clusterer
        KMeansPlusPlusClusterer<SentenceVector> kMeansClusterer
                = new KMeansPlusPlusClusterer<SentenceVector>(k,
                500, cosineSimilarity, s, KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_VARIANCE);

        ArrayList<ArrayList<Sentence>> clusterList = new ArrayList<>();

        int clusterCount = 0;

        // if(kMeansClusterer.)
        for (CentroidCluster<SentenceVector> centroid : kMeansClusterer.cluster(sentenceVectors)) {
            //System.out.println(centroid.getCenter());
            ArrayList<Sentence> cluster = new ArrayList<>();

            System.out.println("================CLUSTER NO." + (++clusterCount) + "================");
            System.out.println("CLUSTER SIZE:" + (centroid.getPoints().size()));
            for (SentenceVector vector : centroid.getPoints()) {
                cluster.add(vector.getSentence());
                // System.out.println(vector.getSentence().getRefSentence());
            }

            //sort the sentences by their positions
            sortSentences(cluster);

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
            , ArrayList<Sentence> sentences) {

        ArrayList<SentenceVector> sentenceVectors = new ArrayList<>();


        for (Document document : topic.getDocuments()) {
            System.out.println("CREATING SENTENCE VECTORS FOR DOCUMENT NO." + document.getDocumentId());

            System.out.print("CREATING VECTOR FOR SENTENCE NO.");
            for (Sentence sentence : document.getSentences()) {
                if (removeShortSentences) {
                    if (sentence.getContent().size() < minimumSentenceLength) continue;
                }
                System.out.print(sentence.getId() + ", ");
                //creating the Sentence vector for the current Sentence.
                SentenceVector currentSentenceVector
                        = SentenceVectorFactory.createSentenceVector(topic,
                        document,
                        sentence,
                        global,
                        sentences);

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
     * @param sentences the ArrayList that contains a List that contains the
     *                  contents of the sentences that will be updated with the topics document
     * @param global    the ArrayList that would be the list containing all the unique stemmed words
     */
    public static void preProcess(Topic topic, ArrayList<Sentence> sentences, ArrayList<String> global) {

        //for each document
        for (Document document : topic.getDocuments()) {
            //for each sentence
            for (Sentence sentence : document.getSentences()) {

                if (removeShortSentences) {
                    //if the number of words is less than the minimum sentence length.
                    if (sentence.getContent().size() < minimumSentenceLength) continue;
                }
                sentences.add(sentence);
                //for each word
                for (String word : sentence.getFreqMap().keySet()) {
                    if (!global.contains(word.toLowerCase()) & !StopWords.isStopWord(word)) {
                        //   System.out.println(word);
                        global.add(word);
                    }
                }
            }
        }
    }

    /**
     * Sort the sentences by their positions.
     *
     * @param sentences
     */
    public static void sortSentences(ArrayList<Sentence> sentences) {
        Collections.sort(sentences, (a, b) -> Long.compare(a.getPosition(), b.getPosition()));
    }
}
