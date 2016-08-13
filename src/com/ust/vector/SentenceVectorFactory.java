package com.ust.vector;

import com.model.Document;
import com.model.Sentence;
import java.util.ArrayList;
import java.util.List;

import static com.ust.vector.SentenceVector.inverseDocumentFrequency;
import static com.ust.vector.SentenceVector.termFrequency;

/**
 * Created by jude8 on 8/12/2016.
 */
public class SentenceVectorFactory {
    /**
     * SentenceVectorFactory for easy creation of SentenceVectors.
     * @param document the document where the sentence came from
     * @param sentence the Sentence object where the sentence came from.
     * @param global the ArrayList which will be the vector Space of
     * @param sentences
     * @return the SentenceVector
     */
    public static SentenceVector createSentenceVector(Document document, Sentence sentence, ArrayList<String> global, ArrayList<List<String>> sentences){
        SentenceVector vector = new SentenceVector();
        vector.setSentence(sentence);
        double point[] = new double[global.size()];
        for(int x=0;x<global.size();x++){
            point[x]= termFrequency(sentence.getContent(),global.get(x))
                    * inverseDocumentFrequency(sentences,global.get(x));
        }
        vector.setPoint(point);

        return vector;
    }
}
