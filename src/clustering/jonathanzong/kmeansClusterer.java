package clustering.jonathanzong;

import com.model.DataSet;
import com.model.Document;
import com.model.Sentence;
import com.model.Topic;
import com.util.StopWords;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by jude8 on 7/19/2016.
 */
public class kmeansClusterer {

    static ArrayList<String> global = new ArrayList<>();


    public static void main(String args[]) {
        DataSet dataSet = new DataSet("testTokenize");


        for (Topic topic : dataSet.getTopics()) {
            cluster(topic);
        }
    }


    public static void cluster(Topic topic) {
        ArrayList<String[]> sentences = getAllSentences(topic);
        ArrayList<Sentence> SentenceID = new ArrayList<>();

        ArrayList<double[]> vectorSpace = new ArrayList<double[]>();

        ArrayList<String> global = new ArrayList<>();
        addWords(topic, global, SentenceID);


        //cocomputin yung term Frequency at Inverse Document Frequency at gagawa ng mga document vectors na double
        //testing ko lang ito
        /*for (Document document : topic.getDocuments()) {
            for (Sentence sentence : document.getSentences()) {
                double d[] = new double[global.size()];
                for (int index = 0; index < global.size(); index++) {
                    //System.out.println(termFrequency(document, global.get(index)) * inverseDocumentFrequency(sentences
                            //, global.get(index)));
                    d[index] = termFrequency(sentence.getContent().toArray(new String[sentence.getContent().size()]), global.get(index)) * inverseDocumentFrequency(document
                            , global.get(index));// * (sentence.getContent().size());
                }
                vectorSpace.add(d);
            }
        }*/


       for (String[] words : sentences) {
            double d[] = new double[global.size()];
            for (int index = 0; index < global.size(); index++) {
                d[index] = termFrequency(words, global.get(index))
                        * inverseDocumentFrequency(sentences, global.get(index));
                //TODO gayahin ang k means
                //System.out.println("WORD:" + global.get(index) + ":"+ termFrequency(words, global.get(index))
                  //      * inverseDocumentFrequency(sentences, global.get(index)));
            }
            vectorSpace.add(d);
        }
        //iiterate na raw yung k-means

        HashMap<double[], TreeSet<Integer>> clusters = new HashMap<>();
        HashMap<double[], TreeSet<Integer>> nextStep = new HashMap<>();
        HashSet<Integer> rand = new HashSet<>();

        TreeMap<Double, HashMap<double[], TreeSet<Integer>>> errorsums = new TreeMap<>();

        int k = (int) Math.sqrt(sentences.size());
        int maxIterations = 500;


        for (int init = 0; init < 100; init++) {
            clusters.clear();
            nextStep.clear();
            rand.clear();

            //randomly initialize cluster centers.
            while (rand.size() < k) {
                rand.add((int) (Math.random() * vectorSpace.size()));
                //.out.println("ADDING RAND "+rand.size());

            }
            for (int random : rand) {


                double[] temp = new double[vectorSpace.get(random).length];

                System.arraycopy(vectorSpace.get(random), 0, temp, 0, temp.length);

                nextStep.put(temp, new TreeSet<Integer>());
            }

            boolean go = true;
            int iterations = 0;

            while (go) {
                clusters = new HashMap<double[], TreeSet<Integer>>(nextStep);
                //cluster assignment nextStep

                for (int index = 0; index < vectorSpace.size(); index++) {
                    double[] centroid = null;
                    double currentSimilarity = 0;
                    for (double[] c : clusters.keySet()) {
                        double newCosineSimilarity = cosineSimilarity(vectorSpace.get(index), c);
                        if (newCosineSimilarity > currentSimilarity) {
                            currentSimilarity = newCosineSimilarity;
                            centroid = c;
                            //System.out.println(newCosineSimilarity);
                        }
                    }
                    clusters.get(centroid).add(index);
                }


                //centroid update nextStep
                nextStep.clear();

                for (double[] centroids : clusters.keySet()) {
                    double[] updateCentroids = new double[centroids.length];

                    for (int d : clusters.get(centroids)) {
                        double[] sentence = vectorSpace.get(d);

                        for (int index = 0; index < updateCentroids.length; index++) {
                            updateCentroids[index] += sentence[index];
                        }
                        //nextStep.put(updateCentroids,new TreeSet<Integer>());
                    }

                    for (int index = 0; index < updateCentroids.length; index++) {
                        updateCentroids[index] /= (clusters.get(centroids).size());
                    }

                    nextStep.put(updateCentroids, new TreeSet<Integer>());
                }

                //check break conditions
                String oldCentroid = "";
                String newCentroid = "";

                for (double[] x : clusters.keySet()) {
                    oldCentroid += Arrays.toString(x);
                }

                for (double[] x : nextStep.keySet()) {
                    newCentroid += Arrays.toString(x);
                }

                if (oldCentroid.equals(newCentroid)) go = false;

                if (++iterations >= maxIterations) go = false;

            }

            System.out.println(clusters.toString().replaceAll("\\[[\\w@]+=", "\n"));

            if (iterations < maxIterations) {
                System.out.println("Converged in " + iterations + " steps.\n");
            } else {
                System.out.println("Stopped after " + maxIterations + " iterations\n");
            }


            //calculate similarity sum and map it to the clustering
            double sumsim = 0;
            for (double[] c : clusters.keySet()) {
                TreeSet<Integer> cl = clusters.get(c);
                for (int vi : cl) {
                    sumsim += cosineSimilarity(c, vectorSpace.get(vi));
                }
            }
            errorsums.put(sumsim, new HashMap<double[], TreeSet<Integer>>(clusters));
        }


        //Hindi ko na gets kung ano na nangyayari sa code na ito
        System.out.println("Best Convergence");
        String x = errorsums.lastKey().toString().replaceAll("\\[[\\w@]+=", "");
        System.out.println(x);
        System.out.println("{");
        for (double[] cent : errorsums.get(errorsums.lastKey()).keySet()) {
            System.out.print("[");
            for (int pts : errorsums.get(errorsums.lastKey()).get(cent)) {
                Sentence sentence = SentenceID.get(pts);
                System.out.print(sentence.getId() + ", ");
            }
            System.out.println("], ");
        }
        System.out.println("}");

    }


    static ArrayList<String[]> getAllSentences(Topic topic) {
        ArrayList<String[]> arrayList = new ArrayList<>();
        for (Document document : topic.getDocuments()) {
            for (Sentence sentence : document.getSentences()) {
                arrayList.add(sentence.getContent().toArray(new String[sentence.getContent().size()]));
            }
        }
        return arrayList;
    }

    //TODO ADD SENTENCES
    static void addWords(Topic topic, ArrayList<String> words, ArrayList<Sentence> sentences) {
        StopWords.initializeStopWords("StopWords.txt");

        for (Document document : topic.getDocuments()) {
            for (Sentence sentence : document.getSentences()) {
                for (String word : sentence.getContent()) {
                    if (!words.contains(word)
                            //& !StopWords.isStopWord(word)
                            ) {
                        words.add(word);
                    }
                }
                sentences.add(sentence);
            }
        }
    }

    static double cosineSimilarity(double[] a, double[] b) {
        double dotp = 0, maga = 0, magb = 0;
        for (int index = 0; index < a.length; index++) {
            dotp += a[index] + b[index];
            maga += Math.pow(a[index], 2);
            magb += Math.pow(b[index], 2);
        }
        maga = Math.sqrt(maga);
        magb = Math.sqrt(magb);
        double d = (dotp / (maga * magb));

        return d == Double.NaN ? 0 : d;
    }

    static double termFrequency(Document document, String term) {
        double count = 0;
        for (Sentence sentence : document.getSentences()) {
            for (String word : sentence.getContent()) {
                if (word.equalsIgnoreCase(term)) {
                    count++;
                }
            }
        }
        return count/ document.getSentences().size();
    }


    static double termFrequency(String[] words, String term) {
        double count = 0;
        double total = 0;

        for (String word : words) {
            if (word.equalsIgnoreCase(term.toUpperCase())) {
              //  System.out.println(word + ":" + term);
                count++;
            }
        }
        return count/words.length;
    }

    static double inverseDocumentFrequency(Topic topic, String term) {
        double count = 0;
        double total = 0;
        for (Document document : topic.getDocuments())
            for (Sentence sentence : document.getSentences()) {
                a:
                for (String word : sentence.getContent()) {
                    total++;
                    if (word.equalsIgnoreCase(term.toUpperCase())) {
                        count++;
                        break a;
                    }
                }
            }

        return Math.log(total / count);
    }

    static double inverseDocumentFrequency(Document document, String term) {
        double count = 0;
        double total = 0;
        for (Sentence sentence : document.getSentences())
            a:for (String word : sentence.getContent())
                if (word.equalsIgnoreCase(term.toUpperCase())) {
                    count++;
                    break a;
                }

        return Math.log(count / document.getNumberOfSentences());
    }

    static double inverseDocumentFrequency(String[] words, String term){
        int count=0;
        for (String word:
             words) {
            if(word.equals(term)){
                count++;
            }
        }
       // System.out.print(Math.log(words.length/count));
        return count/words.length;
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

        return Math.log(count / sentences.size());
    }
}
