package com.util;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import javax.swing.*;
import java.io.*;
import java.util.Iterator;


/**
 * Created by JUDE on 5/31/2016.
 */


public class Tokenizers {

    //for tokenizing the paragraphs into words
    static void tokenize(String filename) throws IOException {

        InputStream inputStream = new FileInputStream("en-sent.bin");

        //the model for the basis of sentence detection
        SentenceModel model = new SentenceModel(inputStream);

        //Eto na yung para sa sentence detector :D, siya na yung maghahati ng paragraph into sentences.
        SentenceDetectorME sdetector = new SentenceDetectorME(model);


        String flattenedText = flattenText(filename);


        //mga pinaghahatian ng mga sentences
        String[] sentences = sdetector.sentDetect(flattenedText);


        File folder=new File("testTokenize");
        folder.mkdir();

        //kung saan pumupunta ang mga tokenized na sentences at words
        File outputFile = new File("del/" + new File(filename).getName());

        FileWriter fileWriter = new FileWriter(outputFile);

        //basis kung saan pumupunta ang mga words
        InputStream is = new FileInputStream("en-token.bin");

        TokenizerModel modelforToken = new TokenizerModel(is);

        Tokenizer tokenizer = new TokenizerME(modelforToken);

        
        int sentenceNumber =1;

        for (String sentence : sentences) {

            fileWriter.append("Sentence: ");
            String[] tokens= tokenizer.tokenize(sentence);


            for(String token : tokens){

                if(token.equals("#")){
                    fileWriter.append("\tS: #\n\tSentence: \n");
                }
                else{
                    fileWriter.append("\tS: "+token+"\n");
                }
            }


        }
        //sentenceNumber=1;
        fileWriter.close();
    }


    //convert a text file into a long String
    static String flattenText(String filename) throws IOException {
        String flattenedText = "";
        FileInputStream fileInputStream = new FileInputStream(filename);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            flattenedText += line + "\n";
        }

        return flattenedText;

    }
    //for tokenizing files of each folder
    public static void tokenizeFiles(String folder)throws IOException{
        File[] mgaDocuments = new File(folder).listFiles();

        for(File file: mgaDocuments){

            System.out.println("NOW TOKENIZING FILE: "+file.getName());
            tokenize(file.getPath());

        }

    }
    //test
    public static void main(String args[]) {
        try {

            String folderName= JOptionPane.showInputDialog("INPUT FOLDER NAME");
            tokenizeFiles(folderName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
