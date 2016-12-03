package com.ust.vector;

import com.model.Document;
import com.model.Sentence;
import com.model.Topic;
import org.apache.commons.math3.ml.clustering.Clusterable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jude8 on 7/29/2016.
 */
public class SentenceVector implements Clusterable {

    Sentence sentence;
    double[] point;



    public void setSentence(Sentence sentence) {
        this.sentence = sentence;
    }

    /**
     *
     * @param point
     */
    public void setPoint(double[] point) {
        this.point = point;
    }



    /**
     * empty constructor para sa SentenceVector Factory
     */
    public SentenceVector(){

    }


    /**
     * method that returns the sentence
     * @return returns the Sentence
     */
    public Sentence getSentence(){
        return sentence;
    }


    /**
     *implemented abstract method for clustering.
     *@return the point
     */
    @Override
    public double[] getPoint() {
        return point;
    }



}


