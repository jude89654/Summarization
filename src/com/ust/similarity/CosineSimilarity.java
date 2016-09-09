package com.ust.similarity;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.ml.distance.DistanceMeasure;

/**
 * Created by jude8 on 7/31/2016.
 */


public class CosineSimilarity implements DistanceMeasure{


    /**
     * a method implemented to create a Cosine Similarity measure in the K-Means clusterer.
     * @param a , the first vector
     * @param b , the second vector
     * @return a double that is the distance between the two vectors.
     */
    @Override
    public double compute(double[] a, double[] b) throws DimensionMismatchException {
        return cosSim(a,b);
    }

    /**
     * method to compute the cosine sim of the two vectors.
     * @param a first vector
     * @param b the second vector
     * @return returns a double that will have the cosine sim
     */
    static double cosSim(double[] a, double[] b) {
        double dotp = 0, maga = 0, magb = 0;
        for (int i = 0; i < a.length; i++) {
            dotp += a[i] * b[i];
            maga += Math.pow(a[i], 2);
            magb += Math.pow(b[i], 2);
        }
        maga = Math.sqrt(maga);
        magb = Math.sqrt(magb);
        double d = dotp / (maga * magb);
        return d == Double.NaN ? 0 : d;
    }
}
