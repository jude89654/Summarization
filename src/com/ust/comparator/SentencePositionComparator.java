package com.ust.comparator;

import com.model.Sentence;

import java.util.Comparator;

/**
 * Created by jude8 on 7/31/2016.
 */
public class SentencePositionComparator implements Comparator<Sentence> {
    @Override
    public int compare(Sentence o1, Sentence o2) {
        return(o1.getPosition()>o2.getPosition())? 1 : 0;
    }
}
