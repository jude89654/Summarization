package com.ust.form.gui;

import com.ust.gui.MenuGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by pc1 on 05/11/2016.
 */
public class HomeForm extends JFrame {
    private JPanel panel1;
    private JButton demoButton;
    private JButton mainButton;

    public HomeForm(){
        super("MEANS SUMMARIZATION");
        setContentPane(panel1);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

        //super.setMaximumSize(new Dimension(640, 480));
        setVisible(true);


        demoButton.addActionListener(e -> {
            DemoGUI demoGUI = new DemoGUI();
            setVisible(false);
        });
        mainButton.addActionListener(e -> {
            MainGUI menu = new MainGUI();
            setVisible(false);
        });
    }

    public static void main(String args[]){
        HomeForm homeForm = new HomeForm();
    }

}
