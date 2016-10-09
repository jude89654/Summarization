package com.ust.gui;

import com.main.MEANSStart;
import com.ust.output.JTextAreaOutputStream;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Book;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by jude8 on 9/5/2016.
 * GUI INTERFACE FOR OUR SYSTEM
 */
public class  MenuGUI extends JFrame {
    public  JPanel panel1;
    public  JPanel ButtonsPanel;
    public  JTextArea logTextArea;
    public  JButton sourcePathBrowseButton;
    public  JTextField sourcePathTextField;
    public  JTextField outputPathTextField;
    public  JPanel statusPanel;
    public  JButton summarizeButton;
    private JButton resetButton;
    private JButton destPathBrowseButton;

    private static MenuGUI instance;

    public MenuGUI(){
        super("MEANS SUMMARIZER");
        super.setMaximumSize(new Dimension(640, 480));
        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        DefaultCaret caret = (DefaultCaret)logTextArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JTextAreaOutputStream outputStream = new JTextAreaOutputStream(logTextArea);
        System.setOut(new PrintStream(outputStream));


        summarizeButton.addActionListener(e -> {
            if(outputPathTextField.getText().trim().equals("")
                    &sourcePathTextField.getText().trim().equals("")){
                logTextArea.setText("INVALID OR EMPTY PATHS");
            }else{
                System.out.println("STARTING SYSTEM");
                String[] paths = new String[]{sourcePathTextField.getText(),outputPathTextField.getText()};
                start(paths[0],paths[1]);
                //MeansStart.main(paths);
            }
        });
        sourcePathBrowseButton.addActionListener(e -> sourcePathTextField.setText(getFolderPath()));

        destPathBrowseButton.addActionListener(e -> outputPathTextField.setText(getFolderPath()));
    }

    public static MenuGUI getInstance(){
        if(instance==null){
            instance  = new MenuGUI();
            return instance;
        } else {
            return instance;
        }
    }

    public static void main(String args[]) {
        instance = getInstance();
    }

    public void start(String input, String output){

        Thread thread = new Thread(){
            @Override
            public void run(){
                MEANSStart.summarize(input,output);
            }
        };
        thread.start();

    }


    /**
     * a method that will open a JFileChooser and will let the user pick and will
     * return a folder path where the text documents are.
     *
     * @return a String of the folder Directory chosen by the JFileChooser or null if nothing
     */
    public static String getFolderPath() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showDialog(new JFrame(), "SELECT FOLDER") == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getPath();
        } else {
            System.out.println("YOU MUST SELECT A FOLDER WHERE THE SOURCE DATA COMES FROM");
            return "";
        }
    }

}
