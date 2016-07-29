package clustering.apache;

import com.model.DataSet;
import com.model.Document;
import com.model.Sentence;
import com.model.Topic;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jude8 on 7/22/2016.
 */
public class JudesClusterer {



    public static void main(String args[]){

        DataSet dataSet = new DataSet("testTokenize");


        for(Topic topic:dataSet.getTopics()){

            ArrayList<SentenceVector> clusterList = new ArrayList<>();
            ArrayList<String> global = new ArrayList<>();
            ArrayList<String[]> sentences = new ArrayList<>();

            initGlobal(topic,global,sentences);
            preProcess(topic,clusterList,global,sentences);


            KMeansPlusPlusClusterer<SentenceVector> judesClusterKMeansPlusPlusClusterer=new KMeansPlusPlusClusterer<SentenceVector>((int)Math.sqrt(sentences.size()/2),500);


            List<CentroidCluster<SentenceVector>> list=judesClusterKMeansPlusPlusClusterer.cluster(clusterList);
            for(CentroidCluster<SentenceVector> cluster : list){
                for(SentenceVector test :cluster.getPoints()) {
                    System.out.println(test.sentence.getDocumentId() + ":" + test.sentence.getRefSentence());

                }
                System.out.println("CLUSTER");
            }
            System.out.println("CLUSTER STOPPED");
        }



    }

    public static void initGlobal(Topic topic,ArrayList<String> global,ArrayList<String[]> sentences){

        for(Document document:topic.getDocuments()){
            for (Sentence sentence:
                 document.getSentences()) {
                sentences.add(sentence.getContent().toArray(new String[sentence.getContent().size()]));
                for(String word:sentence.getContent()){
                    if(!global.contains(word)){
                        global.add(word);
                    }
                }
            }
        }

    }


    public static void preProcess(Topic topic, ArrayList<SentenceVector> clusterables, ArrayList<String> global, ArrayList<String[]>sentences){

        for (Document document:topic.getDocuments()){
            for (Sentence sentence:
                    document.getSentences()) {
                SentenceVector clusterable = new SentenceVector(sentence,global,sentences);
                clusterables.add(clusterable);
            }
        }

    }


}
