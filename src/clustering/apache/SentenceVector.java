package clustering.apache;

import com.model.Document;
import com.model.Sentence;
import org.apache.commons.math3.ml.clustering.Clusterable;

import java.util.ArrayList;

/**
 * Created by jude8 on 7/29/2016.
 */
public class SentenceVector implements Clusterable {

     Sentence sentence;
     //Document document;
    // ArrayList<String> global;
     double[] point;


    public SentenceVector(Sentence sentence, ArrayList<String> global, ArrayList<String[]> sentences){
        this.sentence = sentence;
        point=new double[global.size()];

        for(int x=0;x<global.size();x++){
            point[x]= termFrequency(sentence.getContent().toArray(new String[sentence.getContent().size()]),global.get(x))*inverseDocumentFrequency(sentences,global.get(x));
        }
    }
    public Sentence getSentence(){
        return sentence;
    }

    @Override
    public double[] getPoint() {
        return point;
    }

    static double inverseDocumentFrequency(ArrayList<String[]> sentences, String term) {
        double count = 0;
        double total = 0;

        for (String words[] : sentences)
            a:for (String word : words) {
                if (word.equalsIgnoreCase(term.toUpperCase())) {
                    count++;
                    break a;
                }
            }

        return Math.log(sentences.size() / count);
    }

    static double termFrequency(String[] words, String term) {
        double count = 0;
        double total = 0;

        for (String word : words) {
            if (word.equalsIgnoreCase(term)) {

                count++;
            }
        }
        return count / words.length;
    }


}
