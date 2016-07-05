package com.main;

import java.util.ArrayList;
import java.util.List;

import com.decoding.stackdecoder.StackDecoder;
import com.model.DataSet;
import com.model.Topic;
import com.util.StopWords;
import com.util.Tokenizers;

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
            Tokenizers.tokenizeFiles(folderPath);

            //Test dataset - Stack Decoder
            DataSet testDataSet = new DataSet(folderPath);

            testDataSet.calculateImportanceScores(getWeights());

            System.out.println("Start:main:: Running stack decoder .. ");

            //initial time
            long in = System.currentTimeMillis();

            for (Topic t : testDataSet.getTopics()) {
                System.out.println("TOPIC ID:"+t.getTopicId());
                StackDecoder sd = new StackDecoder(t.getDocuments());
                sd.runStackDecoder();
                sd.printStack(100);
                String path = "summaries/" + testDataSet.getTopicName(t.getTopicId()).toUpperCase() + ".txt";
                sd.dumpBestSummary(path);
            }

            //final time
            long out = System.currentTimeMillis();
            System.out.println("Start:main:: Time taken by Stack decoder (s): " + ((out - in) / 1000));
        }

    }

    private static void usage() {
        System.out.println("Usage: java <main> <path to data>");
        System.out.println("Note: 'data' folder contains the sample input files.");
    }

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
