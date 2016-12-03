package com.ust.gui;

import com.ust.main.MEANSStart;
import com.ust.output.JTextAreaOutputStream;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.PrintStream;

/**
 * Created by jude8 on 9/5/2016.
 * GUI INTERFACE FOR OUR SYSTEM
 */
public class MenuGUI extends JFrame {
    public JPanel panel1;
    public JPanel ButtonsPanel;
    public JTextArea logTextArea;
    public JButton sourcePathBrowseButton;
    public JTextField sourcePathTextField;
    public JTextField outputPathTextField;
    public JPanel statusPanel;
    public JButton summarizeButton;
    private JButton resetButton;
    private JButton destPathBrowseButton;

    private static MenuGUI instance;

    public MenuGUI() {
        super("MEANS SUMMARIZER");
        super.setMaximumSize(new Dimension(640, 480));
        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        DefaultCaret caret = (DefaultCaret) logTextArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JTextAreaOutputStream outputStream = new JTextAreaOutputStream(logTextArea);
        System.setOut(new PrintStream(outputStream));


        summarizeButton.addActionListener(e -> {
            if (outputPathTextField.getText().trim().equals("")
                    & sourcePathTextField.getText().trim().equals("")) {
                logTextArea.setText("INVALID OR EMPTY PATHS");
            } else {
                System.out.println("STARTING SYSTEM");
                String[] paths = new String[]{sourcePathTextField.getText(), outputPathTextField.getText()};
                start(paths[0], paths[1]);
                //MeansStart.main(paths);
            }
        });
        sourcePathBrowseButton.addActionListener(e -> sourcePathTextField.setText(getFolderPath()));

        destPathBrowseButton.addActionListener(e -> outputPathTextField.setText(getFolderPath()));


        resetButton.addActionListener(e -> {
            sourcePathTextField.setText("");
            outputPathTextField.setText("");
            logTextArea.setText("");
        });
    }

    /**
     * THE SINGLETON INSTANCE NA HINDI NA PALA KAILANGAN PUTA SA CODE HINDI KO ALAM BAKIT KO PA ITO CINODE KASI MAY
     * PABIDA SA LECHENG STACK OVERFLOW NA GAGAMITIN DAW ANG GANITO PARANG GAGO AMPUTA HINDI KO NAMAN KAILANGAN ITO
     * PERO NILAGAY KO PA RIN SA CODE KASI TINATAMAD AKO AT HALOS KALAHATING ARAW ANG NAUBOS PARA SA LECHENG SINGLETON
     * PUTA
     *
     * @return nanay mo nirereturn..
     */
    public static MenuGUI getInstance() {
        if (instance == null) {
            instance = new MenuGUI();
            return instance;
        } else {
            return instance;
        }
    }

    /**
     * OUR MAIN METHOD WOOHOO
     *
     * @param args
     */
    public static void main(String args[]) {
        instance = getInstance();
    }


    /**
     * Thread that will be run after clicking the summarize button
     *
     * @param input  the source directory of text documents
     * @param output the output directory for the summaries  of the system
     */
    public void start(String input, String output) {


        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    //enable the reset button and enable the summarize button
                    resetButton.setEnabled(true);
                    summarizeButton.setEnabled(false);

                    //run the code
                    MEANSStart.summarize(input, output);

                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                    System.out.println("SYSTEM INTERRUPTED" + ie.toString());

                } finally {
                    resetButton.setEnabled(false);
                    summarizeButton.setEnabled(true);
                }
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


        //set the chooser to select only folders.
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //disable the all file filter to be used
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showDialog(new JFrame(), "SELECT FOLDER") == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getPath();
        } else {
            System.out.println("YOU MUST SELECT A FOLDER WHERE THE SOURCE DATA COMES FROM");
            return "";
        }
    }

}
