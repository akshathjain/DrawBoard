/*
Name: Akshath Jain
Date: 5/14/17
Purpose: Neural network class, stores weights in json file, also can train and randomize weights
*/

package neuralNet;

import letter.Letter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class NeuralNetwork {
    private final int numInputNodes = Letter.NUM_FEATURES + 1; //includes bias
    private final int numOutputNodes = 26;
    private final int numHiddenNodes = (numInputNodes + numOutputNodes) / 2 + 1; //includes bias
    private final double n = 0.1; //learning constant

    private double[] inputLayer = new double[numInputNodes];
    private double[] hiddenLayer1 = new double[numHiddenNodes];
    private double[] hiddenLayer2 = new double[numHiddenNodes];
    private double[] outputLayer = new double[numOutputNodes];

    private double[][] wIH1 = new double[numInputNodes][numHiddenNodes - 1]; //weights from input layer to hidden layer, includes bias, so subtract one
    private double[][] wH1H2 = new double[numHiddenNodes][numHiddenNodes - 1]; //weights from hidden layer 1 to hidden layer 2, includes bias then no bias
    private double[][] wH2O = new double[numHiddenNodes][numOutputNodes]; //weights from hidden layer to output layer, includes bias

    public NeuralNetwork(String weightFileName) {
        if (weightFileName == null)
            randomizeWeights();
        else
            readWeightsFromFile(weightFileName);
    }

    private void randomizeWeights() {
        //randomize wIH1
        for (int i = 0; i < wIH1.length; i++)
            for (int j1 = 0; j1 < wIH1[0].length; j1++)
                wIH1[i][j1] = Math.random() - 0.5; //[-0.5,0.5)

        //randomize wH1H2
        for (int j1 = 0; j1 < wH1H2.length; j1++)
            for (int j2 = 0; j2 < wH1H2[0].length; j2++)
                wH1H2[j1][j2] = Math.random() - 0.5; //[-0.5,0.5)

        //randomize wH2O
        for (int j = 0; j < wH2O.length; j++)
            for (int k = 0; k < wH2O[0].length; k++)
                wH2O[j][k] = Math.random() - 0.5; //[-0.5,0.5)
    }

    public String execute(Letter l) {
        //fill input layer
        inputLayer[0] = l.getAspectRatio();
        int loc = 1;
        for (int i = 0; i < l.getRasterGrid().length; i++)
            for (int j1 = 0; j1 < l.getRasterGrid()[0].length; j1++)
                inputLayer[loc++] = l.getRasterGrid()[i][j1];
        inputLayer[inputLayer.length - 1] = 1;

        //fill hidden layer 1
        for (int j1 = 0; j1 < numHiddenNodes - 1; j1++) {
            for (int i = 0; i < numInputNodes; i++)
                hiddenLayer1[j1] += inputLayer[i] * wIH1[i][j1];

            hiddenLayer1[j1] = sigmoid(hiddenLayer1[j1]);
        }
        hiddenLayer1[hiddenLayer1.length - 1] = 1;

        //fill hidden layer 2
        for (int j2 = 0; j2 < numHiddenNodes - 1; j2++) {
            for (int j1 = 0; j1 < numHiddenNodes; j1++)
                hiddenLayer2[j2] += hiddenLayer1[j1] * wH1H2[j1][j2];

            hiddenLayer2[j2] = sigmoid(hiddenLayer2[j2]);
        }
        hiddenLayer2[hiddenLayer2.length - 1] = 1;

        //fill output layer
        for (int k = 0; k < numOutputNodes; k++) {
            for (int j2 = 0; j2 < numHiddenNodes; j2++)
                outputLayer[k] += hiddenLayer2[j2] * wH2O[j2][k];

            outputLayer[k] = sigmoid(outputLayer[k]);
        }

        //find max index of output layer
        int maxIndex = 0;
        for (int k = 1; k < numOutputNodes; k++)
            if (outputLayer[k] > outputLayer[maxIndex])
                maxIndex = k;

        return (char) (maxIndex + 65) + "";
    }

    public void train(ArrayList<Letter> data, ArrayList<Letter> validationSet, String weightOutputFileName) {
        double error;
        do {
            error = 0;

            for (int a = 0; a < data.size(); a++) {
                double[] dO = new double[numOutputNodes]; //error at output nodes
                double[] dH2 = new double[numHiddenNodes]; //errror at hidden nodes 2
                double[] dH1 = new double[numHiddenNodes]; //error at hidden nodes 1

                int proper = (int) data.get(a).getKnownLetter().charAt(0) - 65; //int from [0-25]
                execute(data.get(a)); //execute the neural net

                //calculate error at output layer
                for (int k = 0; k < numOutputNodes; k++) {
                    dO[k] = outputLayer[k] * (1 - outputLayer[k]) * ((k == proper ? 1 : 0) - outputLayer[k]);
                    error += Math.pow(((k == proper ? 1 : 0) - outputLayer[k]), 2);
                }

                //calculate error at hidden layer 2
                for (int j2 = 0; j2 < numHiddenNodes; j2++) {
                    int sum = 0;
                    for (int k = 0; k < numOutputNodes; k++)
                        sum += wH2O[j2][k] * dO[k];

                    dH2[j2] = hiddenLayer2[j2] * (1 - hiddenLayer2[j2]) * sum;
                }

                //calculate the error at hidden layer 1
                for (int j1 = 0; j1 < numHiddenNodes; j1++) {
                    int sum = 0;
                    for (int j2 = 0; j2 < numHiddenNodes - 1; j2++) //substract 1 b/c of bias value
                        sum += wH1H2[j1][j2] * dH2[j2];

                    dH1[j1] = hiddenLayer1[j1] * (1 - hiddenLayer1[j1]) * sum;
                }

                //calculate and change the weights wIH1
                for (int i = 0; i < numInputNodes; i++)
                    for (int j = 0; j < numHiddenNodes - 1; j++) //subtract 1 b/c of bias value (no weights go to that)
                        wIH1[i][j] += n * inputLayer[i] * dH1[j];

                //calculate and change the weights wH1H2
                for (int j1 = 0; j1 < numHiddenNodes; j1++)
                    for (int j2 = 0; j2 < numHiddenNodes - 1; j2++)
                        wH1H2[j1][j2] += n * hiddenLayer1[j1] * dH2[j2];

                //calculate and change the weights wOH
                for (int j2 = 0; j2 < numHiddenNodes; j2++)
                    for (int k = 0; k < numOutputNodes; k++)
                        wH2O[j2][k] += n * hiddenLayer2[j2] * dO[k];
            }
            error /= 2;
        } while (terminationCondition(validationSet, error)); //termination condition of < 1% error

        writeWeightsToFile(weightOutputFileName);
    }

    private double[][] optimalWIH1;
    private double[][] optimalWH1H2;
    private double[][] optimalWH2O;
    private int increasing = 0;
    private double prevValError = -1;
    private double optimalValError = -1;
    private long startTime = -1;
    private final long TRAINING_TIME = 1000 * 60 * 10; //5 minutes;

    private boolean terminationCondition(ArrayList<Letter> validationSet, double networkError) {
        double valError = 0;
        if (startTime == -1)
            startTime = System.currentTimeMillis();

        //cheap way out of not having a legit validation set
       /* if (System.currentTimeMillis() - startTime > TRAINING_TIME)
            return false;
        else {
            System.out.println(networkError);
            return true;
        }*/
        for (int a = 0; a < validationSet.size(); a++) {
            execute(validationSet.get(a));
            int proper = (int) validationSet.get(a).getKnownLetter().charAt(0) - 65; //int from [0-25]
            for (int k = 0; k < numOutputNodes; k++)
                valError += Math.pow(((k == proper ? 1 : 0) - outputLayer[k]), 2);
            valError /= 2;

            if (System.currentTimeMillis() - startTime > TRAINING_TIME) {
                wIH1 = optimalWIH1;
                wH1H2 = optimalWH1H2;
                wH2O = optimalWH2O;
                return false;
            } else if ((valError <= prevValError || prevValError == -1) && valError > 0 && networkError > 0) {
                prevValError = valError;
                increasing = 0;
                if (valError <= optimalValError || optimalValError == -1) {
                    optimalWIH1 = wIH1.clone();
                    optimalWH1H2 = wH1H2.clone();
                    optimalWH2O = wH2O.clone();
                    optimalValError = valError;
                    System.out.println(networkError + "\t\tOPTIMAL: " + optimalValError);
                } else {
                    System.out.println(networkError + " " + valError);
                }

                return true;
            } else {
                prevValError = valError;
                increasing++;
                System.out.println(networkError + " " + valError + " " + increasing);
                if (increasing > 1200) {
                    wIH1 = optimalWIH1;
                    wH1H2 = optimalWH1H2;
                    wH2O = optimalWH2O;
                    return false;
                } else {
                    return true;
                }
            }

        }
        return false;
    }

    private double sigmoid(double s) {
        return 1.0 / (1.0 + Math.pow(Math.E, -1 * s));
    }

    private void writeWeightsToFile(String outputFile) {
        JSONObject obj = new JSONObject();
        obj.put("wIH1", wIH1);
        obj.put("wH1H2", wH1H2);
        obj.put("wH2O", wH2O);

        try {
            PrintWriter writer = new PrintWriter(outputFile);
            writer.println(obj.toString());
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void readWeightsFromFile(String inputFile) {
        try {
            Scanner scanner = new Scanner(new File(inputFile));
            JSONObject obj = new JSONObject(scanner.nextLine());

            //get wIH1
            JSONArray ih1 = (JSONArray) obj.get("wIH1");
            for (int i = 0; i < numInputNodes; i++)
                for (int j = 0; j < numHiddenNodes - 1; j++)
                    wIH1[i][j] = ih1.getJSONArray(i).getDouble(j);

            //get wH1H2
            JSONArray h1h2 = (JSONArray) obj.get("wH1H2");
            for (int j = 0; j < numHiddenNodes; j++)
                for (int j2 = 0; j2 < numHiddenNodes - 1; j2++)
                    wH1H2[j][j2] = h1h2.getJSONArray(j).getDouble(j2);

            //get wH2O
            JSONArray h2o = (JSONArray) obj.get("wH2O");
            for (int j = 0; j < numHiddenNodes; j++)
                for (int k = 0; k < numOutputNodes; k++)
                    wH2O[j][k] = h2o.getJSONArray(j).getDouble(k);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
