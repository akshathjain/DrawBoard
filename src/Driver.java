/*
 * Name: Akshath Jain
 * Date: 5/6/17
 * Purpose: AP Computer Science Final Project: DrawBoard, a hand writing recognition application
 */

import gui.DrawPanel;

import javax.swing.*;


public class Driver {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } catch (UnsupportedLookAndFeelException e) {
        }
        JFrame frame = new JFrame("Draw Board");
        DrawPanel panel = new DrawPanel();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.add(panel);
        frame.setVisible(true);
    }
}
