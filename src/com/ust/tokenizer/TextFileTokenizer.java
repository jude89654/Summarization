package com.ust.tokenizer;

import com.processor.SentenceProcessor;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import javax.swing.*;
import java.io.*;
import java.text.Normalizer;
import java.util.Iterator;


/**
 * Created by JUDE on 5/31/2016.
 */


public class TextFileTokenizer {

    //
    static final String outputFolder = "del";
    static final String modelSentenceFile="models"+File.separator+"en-sent.bin";
    static final String modelTokensFile = "models"+File.separator+"en-token.bin";
    /**
     * method to create a tokenization of words that will be put on the /del directory
     * @param filename the name o the text document to be tokenized
     * @throws IOException if the file does not exist or the file cannot be used
     */
    static void tokenize(String filename) throws IOException {

        new File(outputFolder).mkdir();

        InputStream inputStream = new FileInputStream(modelSentenceFile);

        //the model for the basis of sentence detection
        SentenceModel model = new SentenceModel(inputStream);

        //Eto na yung para sa sentence detector :D, siya na yung maghahati ng paragraph into sentences.
        SentenceDetectorME sdetector = new SentenceDetectorME(model);


        String flattenedText = flattenText(filename);


        //mga pinaghahatian ng mga sentences

        String[] sentences = sdetector.sentDetect(flattenedText);


        File folder=new File(outputFolder);
        folder.mkdir();

        //kung saan pumupunta ang mga tokenized na sentences at words
        File outputFile = new File(outputFolder+ File.separator + new File(filename).getName());

        FileWriter fileWriter = new FileWriter(outputFile);

        //basis kung saan pumupunta ang mga words
        InputStream is = new FileInputStream(modelTokensFile);

        TokenizerModel modelforToken = new TokenizerModel(is);

        Tokenizer tokenizer = new TokenizerME(modelforToken);

        //int sentenceNumber =1;
        for (String sentence : sentences) {

            fileWriter.append("Sentence:\n");
            String[] tokens= tokenizer.tokenize(sentence);

            for(String token : tokens){

                //if the token is terminal.
                if(token.equals(SentenceProcessor.TERMINAL)){
                    fileWriter.append("\tS: "+ SentenceProcessor.TERMINAL+"\n");
                    fileWriter.append("Sentence:\n");
                }
                else{
                    fileWriter.append("\tS: "+token+"\n");
                }
            }


        }
        //sentenceNumber=1;
        fileWriter.close();
    }


    /**
     * a method that will be used to create the text document into a STring
     * @param filename the text file.
     * @return a String that is the contents of the text document
     * @throws IOException
     */

    static String flattenText(String filename) throws IOException {
        //String flattenedText="";
        FileInputStream fileInputStream = new FileInputStream(filename);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while((line=bufferedReader.readLine())!=null){
            stringBuilder.append(line+" ");
        }

        return Normalizer.normalize(stringBuilder.toString()
                .replace("“","\"")
                .replace("”","\"")
                .replace("Mt.", "Mount")
                .replace("Metro Manila (CNN Philippines) — ","")
                .replace("MANILA, Philippines - ","")
                .replace("MANILA, Philippines — ","")
                .replace("MANILA Philippines - ","")
                .replace("MANILA Philippines — ","")
                .replace(".com","dot com")
                .replace(".\"",",\"")
                .replace("Mt.","Mount")
                , Normalizer.Form.NFC);
        //return Normalizer.normalize(flattenedText, Normalizer.Form.NFD);
         //flattenedText;
    }

    /**
     * a method that will tokenize text documents at the selected directory
     * @param folder the folder that contains the text documents
     * @throws IOException
     */
    public static void tokenizeFiles(String folder)throws IOException{
        File[] mgaDocuments = new File(folder).listFiles((dir,name)->name.endsWith(".txt"));

        for(File file: mgaDocuments){
            System.out.println("NOW TOKENIZING FILE: "+file.getName());

            tokenize(file.getPath());

        }

    }

    /**
     * test method
     * @param args not used
     */
    public static void main(String args[]) {
        try {

            System.out.println("PLEASE PICK A FOLDER");
            String folderName= JOptionPane.showInputDialog("INPUT FOLDER NAME");
            tokenizeFiles(folderName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
