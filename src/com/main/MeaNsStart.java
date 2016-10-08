package com.main;

import com.model.DataSet;
import com.model.Document;
import com.model.Sentence;
import com.model.Topic;
import com.ust.BM25Modified.BM25TextRank;
import com.ust.gui.MenuGUI;
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
    final static String STOPWORDSPATH = "StopWords.txt";

    /**
     * String that will be the name of the output directory of the summaries
     */
     static String outputFolderName = "MeansSummary";

    /**
     * the number of sentences selected by the user
     */
    final static int numOfSentences = 4;

    /**
     * setting this to true will remove short sentence according to the minimum sentence length.
     */
    final static boolean removeShortSentences = true;

    /**
     * if removeShortSentences is true, it will remove sentences with less than minimumSentenceLengh
     */
    static int minimumSentenceLength = 7;

    /**
     * eto na yung main method ng thesis namin.
     *
     * @param args not used
     */

    public static MenuGUI instance;


    public static String inputfolderPath;

    public static void main(String args[]){
        summarize("","");
    }


    public static void summarize(String inputDirectory,String outputDirectory){
         instance = MenuGUI.getInstance();

        //initialize StopWords Class
        StopWords.initializeStopWords(STOPWORDSPATH);

        //get the path
        if(inputDirectory.trim().equals("")) {
            inputfolderPath = "FOR PROFESSORS";
        }else{
            inputfolderPath =inputDirectory;
        }

        if(outputDirectory.trim().equals("")){
            outputFolderName = outputDirectory+File.separator+outputFolderName;
            File output = new File(outputFolderName);
            output.mkdirs();
        }

        //if (inputfolderPath.equals("")) stopProgram();

        //numOfSentences
        //numOfSentences = inputNumberOfSentences();

        //Stop the program if no folder is selected


        //for tokenization
        instance.doLog("TOKENIZATION");
        tokenizeFiles(inputfolderPath);


        //initialize dataset
        DataSet dataSet = new DataSet(inputfolderPath);


        //CREATING SUMMARIZATION FOR EACH TOPIC
        for (Topic topic : dataSet.getTopics()) {

            String fileName = dataSet.getTopicName(topic.getTopicId());

            instance.doLog("NOW CREATING SUMMARY FOR:" + fileName);

            //cluster the topics
            ArrayList<ArrayList<Sentence>> clusterList = clusterize(topic);

            //get top Sentence from each cluster
            ArrayList<Sentence> topSentenceFromEachCluster = getTopSentencesFromEachCluster(clusterList);

            ArrayList<Sentence> reorderedSentences = reRankTopSentences(topSentenceFromEachCluster);

            // sortSentences(reorderedSentences);
            try {
                createSummaryFile(reorderedSentences, new File(outputDirectory+File.separator+fileName + "_" + systemName + ".txt"));
            } catch (IOException ioException) {
                instance.doLog("IOEXCEPTION-ERROR IN CREATING FILE:" + fileName);
                //instance.doLog("IOEXCEPTION-ERROR IN CREATING FILE:" + fileName);
                ioException.printStackTrace();
            }


        }
        //stopProgram();

    }


    /**
     * method used to reorder sentences based on the ranking used by the bm25 algorithm
     *
     * @param topSentences the top sentences extracted
     * @return reordered sentences
     */
    public static ArrayList<Sentence> reRankTopSentences(ArrayList<Sentence> topSentences) {
        ArrayList topSentencesArrayList = new ArrayList(BM25TextRank.getTopSentenceList(topSentences, numOfSentences));

        return topSentencesArrayList;
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

            //for each clusters
            for (ArrayList<Sentence> cluster : clusters) {

                //if the cluster is empty, skip
                if (cluster.isEmpty()) continue;

                //else get the top sentence from each cluster
                ArrayList<Sentence> tempList = new ArrayList<>(BM25TextRank
                                                                    .getTopSentenceList(cluster
                                                                                            ,numOfSentences));


                //remove the sentences from the cluster
                for (Sentence tempSentence : tempList) {
                    topSentenceFromAllClusters.add(tempSentence);
                    cluster.remove(tempSentence);
                }

            }
            //break the loop if the number of sentences is over the limit.
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

        instance.doLog("CREATING FILE:"+file.getName());
        //instance.doLog("CREATING FILE:" + file.getName());

        File outputFolder = new File(outputFolderName);
        //making sure that directory exists.
        outputFolder.mkdir();

        File outputFileDirectory = new File(outputFolder + File.separator + file.getName());

        FileWriter fileWriter = new FileWriter(outputFileDirectory, false);

        StringBuilder finalSummaryStringBuilder = new StringBuilder();

        //transfer the final summary to a string.
        for (Sentence sentence : topSentences) {
            finalSummaryStringBuilder.append(sentence.getRefSentence() + "\n");
        }

        instance.doLog("FINAL SUMMARY:\n" + finalSummaryStringBuilder.toString());
        //instance.doLog();
        //add the final Summary to the file :D
        fileWriter.write(finalSummaryStringBuilder.toString());
        fileWriter.close();

        instance.doLog("FILE CREATED:" + file.getName());
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
            instance.doLog("ERROR IN TOKENIZING FILES");
            stopProgram();
        }
    }

    /**
     * a method that will end the program immediately/
     */
    public static void stopProgram() {
        instance.doLog("MEANS SUMMARIZATION PROGRAM ENDED");
        System.exit(0);
    }


    /**
     * a method that will open a JFileChooser and will let the user pick and will
     * return a folder path where the text documents are.
     *
     * @return a String of the folder Directory chosen by the JFileChooser or null if nothing
     */
    public static String getInputfolderPath() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showDialog(new JFrame(), "SELECT FOLDER") == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getPath();
        } else {
            instance.doLog("YOU MUST SELECT A FOLDER WHERE THE SOURCE DATA COMES FROM");
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


        instance.doLog("NUMBER OF CLUSTERS:" + k);
        //Similarity measure used for distance in the clusterer
        CosineSimilarity cosineSimilarity = new CosineSimilarity();

        SynchronizedRandomGenerator randomGenerator = new SynchronizedRandomGenerator(new JDKRandomGenerator());

        //initializing the clusterer
        KMeansPlusPlusClusterer<SentenceVector> kMeansClusterer
                = new KMeansPlusPlusClusterer<SentenceVector>(k
                , 500
                , cosineSimilarity
                , randomGenerator
                , KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_VARIANCE);

        ArrayList<ArrayList<Sentence>> clusterList = new ArrayList<>();

        int clusterCount = 0;

        // if(kMeansClusterer.)
        for (CentroidCluster<SentenceVector> sentenceClusters : kMeansClusterer.cluster(sentenceVectors)) {
            //instance.doLog(centroid.getCenter());
            ArrayList<Sentence> cluster = new ArrayList<>();

            instance.doLog("================CLUSTER NO." + (++clusterCount) + "================");
            instance.doLog("CLUSTER SIZE:" + (sentenceClusters.getPoints().size()));

            for (SentenceVector vector : sentenceClusters.getPoints()) {
                cluster.add(vector.getSentence());
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
            instance.doLog("CREATING SENTENCE VECTORS FOR DOCUMENT NO." + document.getDocumentId());

            instance.append("CREATING VECTOR FOR SENTENCE NO.");
            for (Sentence sentence : document.getSentences()) {
                if (removeShortSentences) {
                    if (sentence.getContent().size() < minimumSentenceLength) continue;
                }
                instance.append(sentence.getId() + ", ");
                //creating the Sentence vector for the current Sentence.
                SentenceVector currentSentenceVector
                        = SentenceVectorFactory.createSentenceVector(topic,
                        document,
                        sentence,
                        global,
                        sentences);

                sentenceVectors.add(currentSentenceVector);
            }
            instance.doLog("");
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

        instance.doLog("CREATING VECTOR SPACE");

        //for each document
        for (Document document : topic.getDocuments()) {
            //for each sentence
            for (Sentence sentence : document.getSentences()) {

                if (removeShortSentences) {
                    //if the number of words is less than the minimum sentence length.
                    if (sentence.getContent().size() < minimumSentenceLength) continue;
                } else {
                    //put the sentence in the sentence list.
                    sentences.add(sentence);


                    //for each word in the sentence
                    for (String word : sentence.getFreqMap().keySet()) {
                        // add the word in the vector space if the word is not a stopword
                        if (!global.contains(word.toLowerCase()) & !StopWords.isStopWord(word)) {
                            global.add(word);
                        }
                    }
                }
            }
        }
        instance.doLog("VECTOR SPACE LENGTH: "+global.size());
    }

    /**
     * Sort the sentences by their positions.
     *
     * @param sentences
     */
    public static void sortSentences(ArrayList<Sentence> sentences) {

        Collections.sort(sentences, (a, b) -> Double.compare(a.getPosition(), b.getPosition()));
        //  Collections.
    }
}
