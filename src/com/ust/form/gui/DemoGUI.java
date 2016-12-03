package com.ust.form.gui;

import com.model.Document;
import com.model.Sentence;
import com.model.Topic;
import com.ust.output.JTextAreaOutputStream;
import com.util.StopWords;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultCaret;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;

import static com.ust.main.MEANSStart.clusterize;
import static com.ust.main.MEANSStart.getTopSentencesFromEachCluster;
import static com.ust.main.MEANSStart.reRankTopSentences;


/**
 * Created by pc1 on 05/11/2016.
 */
public class DemoGUI extends JFrame {
    private JPanel panel1;
    private JTextField textField1;
    private JButton browseButton;
    private JButton summarizeButton;
    private JTextArea document1TextArea;
    private JTextArea logsTextArea;
    private JTextArea SummaryTextArea;
    private JTextArea document3TextArea;
    private JTextArea document2TextArea;
    private Topic topic;
    //private ArrayList<JTextArea> jTextAreas = new ArrayList<>(new JTextArea[]{document1TextArea,document2TextArea,document3TextArea});

    /**
     * default constructor
     * method used to show the gui of The demo
     */
    public DemoGUI() {
        super("DEMO");
        StopWords.initializeStopWords("StopWords.txt");
        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        DefaultCaret caret = (DefaultCaret) logsTextArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JTextAreaOutputStream outputStream = new JTextAreaOutputStream(logsTextArea);
        System.setOut(new PrintStream(outputStream));

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                HomeForm homeForm = new HomeForm();
            }
        });
        browseButton.addActionListener(e -> {
            try {
                File file = getSelectedFile();
                topic = new Topic(file.getPath(), 0);
                setDocuments(topic);
            } catch (Exception e1) {
                e1.printStackTrace();
                logsTextArea.append("YOU MUST SELECT A TEXT FILE");
            }
        });
        summarizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startSummarizeThread(topic);
            }
        });
    }

    public File getSelectedFile() throws Exception {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setAcceptAllFileFilterUsed(false);
        jFileChooser.setMultiSelectionEnabled(false);
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (jFileChooser.showDialog(
                new JFrame()
                , "SELECT THE TEXT FILE TO SUMMARIZE") == JFileChooser.APPROVE_OPTION) {
            return jFileChooser.getSelectedFile();
        } else {
            throw new Exception("NO FILE SELECTED");
        }
    }


    /**
     * method for adding text to the three document textArea.
     *
     * @param topic
     */
    public void setDocuments(Topic topic) {
        ArrayList<JTextArea> jTextAreas = new ArrayList<>();
        //ArrayList<Integer> integers = new ArrayList<>(Collections.)
        jTextAreas.add(document1TextArea);
        jTextAreas.add(document2TextArea);
        jTextAreas.add(document3TextArea);


        //for each jtextareas, add each articles.
        for (int index = 0; index < jTextAreas.size(); index++) {
            Document doc = topic.getDocuments().get(index);
            String content = doc.toString();
            jTextAreas.get(index).setText(content);

        }
    }

    public void startSummarizeThread(Topic topic) {

        Thread summarizeThread = new Thread() {

            public void run() {

                ArrayList<ArrayList<Sentence>> clusterList = clusterize(topic);

                //get top Sentence from each cluster
                ArrayList<Sentence> topSentenceFromEachCluster = getTopSentencesFromEachCluster(clusterList);

                ArrayList<Sentence> reorderedSentences = reRankTopSentences(topSentenceFromEachCluster);

                    SummaryTextArea.setText("");
                   String summary= createSummary(reorderedSentences);
                    SummaryTextArea.append(summary);


            }

        };

        summarizeThread.start();

    }


    public static String createSummary(ArrayList<Sentence> sentences) {

        String finalSummary = "";


        int summaryLength = 0;
        for (Sentence sentence : sentences) {
            if (summaryLength + sentence.getSentenceLength() <= 100) {
                summaryLength += sentence.getSentenceLength();
                finalSummary += sentence.getRefSentence() + "\n";
            } else {
                break;
            }
        }
        return finalSummary;
    }


}

