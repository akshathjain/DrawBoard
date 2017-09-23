/*
Name: Akshath Jain
Date: 5/8/17
Purpose: Display panel class
 */

package gui;

import letter.Letter;
import grid.Point;
import grid.PointList;
import neuralNet.NeuralNetwork;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class DrawPanel extends JPanel {
    private final int MARGIN_VERTICAL = 85;
    private final int MARGIN_HORIZONTAL = 0;
    private int pointSize = 5;
    private double xBound;
    private double yBound;

    private PointList pointList = new PointList();
    private int pointListSizeWhenMouseReleased; //variable to keep track of last pointList size so two letters aren't connected
    private ArrayList<Letter> letterList;

    private JButton clear;
    private JButton findLetters;
    private JButton startDataCollection;
    private JButton loadTrainingData;
    private JCheckBox drawSquare; //toggle drawing a square around each letter;
    private boolean drawSquaredChecked = false;
    private JCheckBox drawGrid;
    private boolean drawGridChecked = false;

    private int NUM_LETTER_SAMPLES = 5; //number of letters for each letter
    private final int END_TRAINING_INDEX = 26;
    private int trainingIndex = -1; //-1 indicates no training, 0 - 25 indicate current letter
    private ArrayList<Letter> trainingList; //holds all letters to be used in training
    private String fileName;

    public DrawPanel() {
        super();
        super.setBackground(new Color(243, 243, 243));

        setupDashboard();
    }

    private void setupDashboard() {
        //add clear button
        clear = new JButton("CLEAR");
        clear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clear();
            }
        });
        this.add(clear);

        //add find letter button
        findLetters = new JButton("FIND LETTERS");
        findLetters.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                findLetters();
            }
        });
        this.add(findLetters);

        //add draw square check box
        drawSquare = new JCheckBox("Draw Letter Square");
        drawSquare.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                drawSquaredChecked = !drawSquaredChecked;
            }
        });
        this.add(drawSquare);

        //add draw grid check box
        drawGrid = new JCheckBox("Draw Letter Grid");
        drawGrid.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                drawGridChecked = !drawGridChecked;
            }
        });
        this.add(drawGrid);

        //add get training button
        startDataCollection = new JButton("START DATA COLLECTION");
        startDataCollection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startDataCollection();
            }
        });
        this.add(startDataCollection);

        //add load training data button
        loadTrainingData = new JButton("LOAD TRAINING DATA");
        loadTrainingData.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadTrainingData();
            }
        });
        this.add(loadTrainingData);

        //add mouse listeners
        super.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                mouseButtonPressed();
            }
        });
        super.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                mousePositionMoved(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {

            }
        });
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        xBound = g.getClipBounds().getWidth();
        yBound = g.getClipBounds().getHeight();

        //draws the draw area
        for (int j = MARGIN_VERTICAL; j < (int) (yBound - MARGIN_VERTICAL); j += 7) {
            int temp = j % 2;
            for (int i = MARGIN_HORIZONTAL; i < (int) (xBound - MARGIN_HORIZONTAL); i += 7) {
                if (temp++ % 2 == 0)
                    g.setColor(new Color(255, 255, 255));
                else
                    g.setColor(new Color(243, 243, 243));
                g.fillRect(i, j, 7, 7);
            }
        }

        //draws each letter
        g.setColor(new Color(0, 0, 0));
        for (Point p : pointList) {
            g.fillRect(p.getX(), p.getY(), p.getWidth(), p.getHeight());
        }

        //draws the box around each letter if the user wants
        if (letterList != null && drawSquaredChecked) {
            g.setColor(new Color(255, 0, 0));
            for (int i = 0; i < letterList.size(); i++) {
                int x = letterList.get(i).getBox().getX();
                int y = letterList.get(i).getBox().getY();
                int width = letterList.get(i).getBox().getWidth();
                int height = letterList.get(i).getBox().getHeight();
                g.drawRect(x, y, width, height);

                //draw the grid if the user wants it
                if (drawGridChecked) {
                    for (int row = 1; row < Letter.GRID_HEIGHT; row++)
                        g.drawLine(x, y + row * height / Letter.GRID_HEIGHT, x + width, y + row * height / Letter.GRID_HEIGHT);
                    for (int col = 1; col < Letter.GRID_HEIGHT; col++)
                        g.drawLine(x + col * width / Letter.GRID_WIDTH, y, x + col * width / Letter.GRID_WIDTH, y + height);
                }
            }
        }
    }

    private void clear() {
        pointList.clear();
        letterList = new ArrayList<>();
        pointListSizeWhenMouseReleased = 0;
        repaint();
    }

    private void findLetters() {
        letterList = Letter.findLetters(pointList);
        repaint();

        if (trainingIndex >= 0) {
            if (letterList.size() >= NUM_LETTER_SAMPLES) {
                //assign known letter values to each letter, then add all letters to training list
                for (Letter l : letterList)
                    l.setKnownLetter((char) (trainingIndex + 65) + "");
                trainingList.addAll(letterList);
                trainingIndex++;

                //check to see if training is over
                if (trainingIndex == END_TRAINING_INDEX)
                    endDataCollection();
                else
                    showTrainingLetterMessage(trainingIndex);

                clear(); //clear the screen
            } else {
                JOptionPane.showMessageDialog(this,
                        "Not enough letters, draw " + (NUM_LETTER_SAMPLES - letterList.size()) + " more",
                        "Neural Network Training Directions",
                        JOptionPane.INFORMATION_MESSAGE);
                repaint();
            }
        } else {
            NeuralNetwork network = new NeuralNetwork("weights.txt");
            String message = "";
            for (int i = 0; i < letterList.size(); i++) {
                message += network.execute(letterList.get(i));

                for (int j = 0; j < letterList.get(i).getRasterGrid().length; j++) {
                    for (int k = 0; k < letterList.get(i).getRasterGrid()[0].length; k++) {
                        System.out.print(letterList.get(i).getRasterGrid()[j][k] + "\t");
                    }
                    System.out.println();
                }
                System.out.println();
            }
            JOptionPane.showMessageDialog(this, message, "Letter(s) Found", JOptionPane.INFORMATION_MESSAGE);
            repaint();
        }

    }

    private void startDataCollection() {
        startDataCollection.setVisible(false);
        loadTrainingData.setVisible(false);
        fileName = JOptionPane.showInputDialog(this, "New File Name: ");
        NUM_LETTER_SAMPLES = Integer.parseInt(JOptionPane.showInputDialog(this, "Number of samples: "));
        trainingIndex = 0; //keeps track of current letter position
        trainingList = new ArrayList<>();
        showTrainingLetterMessage(trainingIndex);
    }

    private void endDataCollection() {
        trainingIndex = -1;
        startDataCollection.setVisible(true);
        loadTrainingData.setVisible(true);

        JOptionPane.showMessageDialog(this, "Data Collection Done", "Neural Network Training Directions", JOptionPane.INFORMATION_MESSAGE);

        //write training data to file
        try {
            PrintWriter writer = new PrintWriter(fileName);
            for (int i = 0; i < trainingList.size(); i++) {
                writer.println(trainingList.get(i).toJSONObject().toString());
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        trainingList.clear();
    }

    private void loadTrainingData() {
        String[] fileName = JOptionPane.showInputDialog(this, "File Name: ").split(";");

        try {
            trainingList = new ArrayList<>();
            Scanner scanner;

            //read in training sets
            for (int i = 0; i < fileName.length; i++) {
                scanner = new Scanner(new File(fileName[i].trim()));
                while (scanner.hasNextLine()) {
                    trainingList.add(Letter.fromJSONObject(new JSONObject(scanner.nextLine())));
                }
            }

            //read in validation set
            ArrayList<Letter> validationSet = new ArrayList<>();
            scanner = new Scanner(new File("validationSet.txt"));
            while (scanner.hasNextLine())
                validationSet.add(Letter.fromJSONObject(new JSONObject(scanner.nextLine())));

            //ask user if they want to continue training with current weights (update the current weights)
            int updateWeights = JOptionPane.showConfirmDialog(this, "Would You Like to Update Current Weights?", "Weights", JOptionPane.YES_NO_OPTION);
            NeuralNetwork network;
            if(updateWeights == 0)
                network = new NeuralNetwork("weights.txt");
            else
                network = new NeuralNetwork(null);
            network.train(trainingList, validationSet, "weights.txt");
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "File Does Not Exist", "Loading Data Failed", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    //helper function to show training message displaying current letter
    private void showTrainingLetterMessage(int index) {
        JOptionPane.showMessageDialog(this,
                "Draw " + NUM_LETTER_SAMPLES + " " + (char) (index + 65) + "'s",
                "Neural Network Training Directions",
                JOptionPane.INFORMATION_MESSAGE);

        repaint();
    }

    private void mouseButtonPressed() {
        pointListSizeWhenMouseReleased = pointList.size();
    }

    private void mousePositionMoved(MouseEvent e) {
        if (e.getX() < xBound - MARGIN_HORIZONTAL && e.getX() > MARGIN_HORIZONTAL && e.getY() < yBound - MARGIN_VERTICAL && e.getY() > MARGIN_VERTICAL) {
            Point temp = new Point(e.getX(), e.getY(), pointSize);

            if (pointList.size() > pointListSizeWhenMouseReleased) {
                pointList.addBetween(temp); //connect the points using DDA algorithm (b/c mouse sometimes skips over some points)
            } else {
                pointList.add(temp);
            }
            repaint();
        }
    }
}

