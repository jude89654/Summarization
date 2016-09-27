package com.ust.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.print.Book;

/**
 * Created by jude8 on 9/5/2016.
 * GUI INTERFACE FOR OUR SYSTEM
 */
public class  MenuGUI extends JFrame {
    public  JPanel panel1;
    public  JPanel ButtonsPanel;
    public  JTextArea logTextArea;
    public  JButton browseButton;
    public  JTextField sourcePathTextField;
    public  JTextField outputPathTextField;
    public  JPanel statusPanel;
    public  JButton summarizeButton;
    private JButton resetButton;


    public MenuGUI(){
        super("MEANS SUMMARIZER");
        super.setMaximumSize(new Dimension(640, 480));
        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }


    public static void main(String args[]) {

        MenuGUI gui = new MenuGUI();
    }


    public void logMessage(String message){
        logTextArea.append(message+"\n");
    }
}
