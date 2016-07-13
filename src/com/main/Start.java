package com.main;

import java.util.ArrayList;
import java.util.List;

import com.decoding.stackdecoder.StackDecoder;
import com.model.DataSet;
import com.model.Topic;
import com.util.StopWords;
import com.util.TextFileTokenizer;

import javax.swing.*;

public class Start {
    private static String STOP_WORDS_FILE_PATH = "StopWords.txt";

    JFileChooser chooser = new JFileChooser();


    public static void main(String[] args) throws Exception {

        //kukunin yung mga stop words na file path
        StopWords.initializeStopWords(STOP_WORDS_FILE_PATH);

        //para sa pagpili ng folder na may lamang mga isusummarize
        String folder;
        JFileChooser chooser  = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);


        if (!(chooser.showDialog(new JFrame(),"SELECT FOLDER")==JFileChooser.APPROVE_OPTION)) usage();
        else {
            //kukunin yung nakuhang folder path
            String folderPath = chooser.getSelectedFile().getAbsolutePath();

            //tokenization
            TextFileTokenizer.tokenizeFiles(folderPath);

            //Test dataset - Stack Decoder
            DataSet testDataSet = new DataSet(folderPath);

            testDataSet.calculateImportanceScores(getWeights());

            System.out.println("Start:main:: Running stack decoder .. ");

            //initial time
            long initialTime = System.currentTimeMillis();

            //for each topic
            for (Topic currentTopic : testDataSet.getTopics()) {

                System.out.println("TOPIC ID:"+currentTopic.getTopicId());

                //run yung stack decoder
                StackDecoder stackDecoder = new StackDecoder(currentTopic.getDocuments());
                stackDecoder.runStackDecoder();
                stackDecoder.printStack(100);

                //output yung summary
                String summaryPath = "summaries/" + testDataSet.getTopicName(currentTopic.getTopicId()).toUpperCase() + ".txt";
                stackDecoder.dumpBestSummary(summaryPath);

            }


            //final time
            long finalTime = System.currentTimeMillis();
            System.out.println("Start:main:: Time taken by Stack decoder (s): " + ((finalTime - initialTime) / 1000));
        }

    }

    /*
     *para pag walang pinili na folder
     */
    private static void usage() {
        System.out.println("Usage: java <main> <path to data>");
        System.out.println("Note: 'data' folder contains the sample input files.");
    }

    /*
     * eto raw yung mga scores na nakuha sa training, may ML pala dito
     */

    public static List<Double> getWeights() {
        //Obtained from training
        List<Double> res = new ArrayList<Double>();
        //Note: Set theta_0 in importance module
        //TFIDFSum,SentLength,SentPost,NumLiteralsCalculator,UpperCaseCalculator
        res.add(0.197971);
        res.add(0.283136);
        res.add(-0.300287);
        res.add(0.1664);
        res.add(0.160681);
        return res;
    }
}
