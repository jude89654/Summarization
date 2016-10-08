package com.ust.gui;

import com.main.MEANSStart;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * Created by jude8 on 9/5/2016.
 * GUI INTERFACE FOR OUR SYSTEM
 */
public class  MenuGUI extends JFrame {
    public  JPanel panel1;
    public  JPanel ButtonsPanel;
    public  JTextArea logTextArea;
    public  JButton browseInputDirectoryButton;
    public  JTextField sourcePathTextField;
    public  JTextField outputPathTextField;
    public  JPanel statusPanel;
    public  JButton summarizeButton;
    private JButton resetButton;
    private JButton outputPathBrowseButton;

    private static MenuGUI instance;

    /**
     * Method to get an instance of the this class. This method ensures that
     * MenuGUI is a Singleton using a doule checked locking mechanism.
     * @return An instance of SwingAppenderUI
     */
    public static MenuGUI getInstance() {
    	System.out.println("getting UI Instance");
        if (instance == null) {
        	synchronized(MenuGUI.class) {
        		if(instance == null) {
        			instance = new MenuGUI();
        		}
        	}
        }
        return instance;
    }


    public MenuGUI(){
        super("MEANS SUMMARIZER");
        super.setMaximumSize(new Dimension(640, 480));
        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);


        //action listener for the reset button.
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logTextArea.setText("");
                outputPathTextField.setText("");
                sourcePathTextField.setText("");
            }
        });


        summarizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MEANSStart.summarize(sourcePathTextField.getText(),outputPathTextField.getText());
            }
        });
        /**
         * asas
         */
        browseInputDirectoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sourcePathTextField.setText(getInputFolderPath());
            }
        });
        outputPathBrowseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                outputPathTextField.setText(getInputFolderPath());
            }
        });
    }


    public static void main(String args[]) {
        instance = getInstance();
    }

    public synchronized void doLog(String log){
        //System.out.println(log);
        logTextArea.append(log+"\n");

    }

    public synchronized void append(String append){
       // System.out.print(append);
        logTextArea.append(append);
    }

    public  String getInputFolderPath(){
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jFileChooser.setAcceptAllFileFilterUsed(false);
        if(jFileChooser.showDialog(this,"SELECT FOLDER")==JFileChooser.APPROVE_OPTION){
            File file = jFileChooser.getSelectedFile();
            return file.getAbsolutePath();
        }
        else{
            logTextArea.setText("YOU MUST SELECT A FOLDER");
            return "";
        }

    }


}
