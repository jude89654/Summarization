package clustering;
import opennlp.tools.tokenize.Tokenizer;

import java.util.ArrayList;


public class KMeans {
    public static void main(String args[]){
        System.out.println("GG NA SER!");
    }

    public static double termfrequency(String [] doc,String term){
        int count=0;
        for(String word:doc){
            if(word.equalsIgnoreCase(term)){
                count++;
            }
        }
        return count/doc.length;
    }

    public static double inverseDocumentFrequency(ArrayList<String []> docs, String term){
        int count=0;
        for(String[] doc: docs){
            for(String word : doc){
                if(word.equalsIgnoreCase(term)){
                    count++;
                }
            }
        }

    }
}
