package com.main;

import clustering.jonathanzong.kmeansClusterer;
import com.model.DataSet;
import com.model.Sentence;
import com.model.Topic;
import com.util.StopWords;
import sun.nio.ch.DatagramSocketAdaptor;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Created by jude8 on 7/28/2016.
 */
public class MeaNsStart {
    private static String STOPWORDSPATH="StopWords.txt";

    public static void main(String args[]){

        StopWords.initializeStopWords(STOPWORDSPATH);

        String folderpath;

        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if(chooser.showDialog(new JFrame(),"SELECT FOLDER")==JFileChooser.APPROVE_OPTION){
            DataSet dataSet = new DataSet(chooser.getSelectedFile().getPath());
            for(Topic currentTopic: dataSet.getTopics()){
                System.out.println("NOW PROCESSING TOPIC:"+currentTopic.getTopicId());
                ArrayList<ArrayList<Sentence>> clusters = new ArrayList<>();
                clusters = kmeansClusterer.cluster(currentTopic);

                /*INSERT TEXT CLUSTERING ALGO HERE*/

            }
        }else{
            System.out.println("YOU MUST SELECT A FOLDER WHERE THE SOURCE DATA COMES FROM");
            JOptionPane.showConfirmDialog(new JFrame(),"YOU MUST SELECT A FOLDER");
        }






    }
}
