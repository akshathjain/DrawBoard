/*
Name: Akshath Jain
Date: 5/9/17
Purpose: Letter.Letter class
 */

package letter;

import grid.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Letter {
    private PointList list;
    private Point box;
    private int numPointsInBox;
    public static final int GRID_WIDTH = 12;
    public static final int GRID_HEIGHT = 12;
    private double[][] rasterGrid;
    private String knownLetter; //known letter used only for training purposes
    private Double aspectRatio;

    public static final int NUM_FEATURES = GRID_WIDTH * GRID_HEIGHT + 1; //the one is for the aspect ratio

    public Letter() {
        list = new PointList();
    }

    public Letter(Point point) {
        list = new PointList();
        list.add(point);
    }

    public void addPoint(Point p) {
        list.add(p);
    }

    public boolean pointTouchesLetter(Point p) {
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).touches(p))
                return true;
        }
        return false;
    }

    public boolean letterTouchesLetter(Letter l) {
        for (int i = 0; i < l.list.size(); i++) {
            if (pointTouchesLetter(l.list.get(i)))
                return true;
        }
        return false;
    }

    public void addLetter(Letter other) {
        this.list.addAll(other.list);
    }

    public Point getBox() {
        if (box == null || list.size() > numPointsInBox) {
            int maxX = list.get(0).getX() + list.get(0).getWidth();
            int minX = list.get(0).getX();
            int maxY = list.get(0).getY() + list.get(0).getHeight();
            int minY = list.get(0).getY();
            for (int i = 1; i < list.size(); i++) {
                Point p = list.get(i);
                if (p.getX() + p.getWidth() > maxX)
                    maxX = p.getX() + p.getWidth();
                else if (p.getX() < minX)
                    minX = p.getX();

                if (p.getY() + p.getHeight() > maxY)
                    maxY = p.getY() + p.getHeight();
                else if (p.getY() < minY)
                    minY = p.getY();
            }
            box = new Point(minX, minY, maxX - minX, maxY - minY);
            numPointsInBox = list.size();
        }
        return box;
    }

    public double[][] getRasterGrid() {
        if (rasterGrid == null) {
            rasterGrid = new double[GRID_WIDTH][GRID_HEIGHT];
            double pixelWidth = (double) getBox().getWidth() / GRID_WIDTH;
            double pixelHeight = (double) getBox().getHeight() / GRID_HEIGHT;

            //create raster grid
            int total = 0;
            for (int i = 0; i < list.size(); i++) {
                Point p = list.get(i);
                for (int j = 0; j < p.getHeight(); j++) {
                    for (int k = 0; k < p.getWidth(); k++) {
                        rasterGrid[(int) ((p.getY() + j - box.getY()) / pixelHeight)][(int) ((p.getX() + k - box.getX()) / pixelWidth)]++;
                        total++;
                    }
                }
            }

            //get percentage coverage in each square and find max value
            double average = 0; // double max = rasterGrid[0][0] / total;
            for (int i = 0; i < GRID_HEIGHT; i++) {
                for (int j = 0; j < GRID_WIDTH; j++) {
                    rasterGrid[i][j] /= total;
                    average += rasterGrid[i][j];
                    /*if (rasterGrid[i][j] > max)
                        max = rasterGrid[i][j];*/
                }
            }
            average /= (rasterGrid.length * rasterGrid[0].length);

            //apply scalar quantity to raster grid
            double scalar = 1.0 / average;//max;
            for (int i = 0; i < GRID_HEIGHT; i++)
                for (int j = 0; j < GRID_WIDTH; j++)
                    rasterGrid[i][j] = (scalar * rasterGrid[i][j] > 1 ? 1 : scalar * rasterGrid[i][j]);

        }
        return rasterGrid;
    }

    public double getAspectRatio() {
        if (aspectRatio == null)
            aspectRatio = (double) getBox().getWidth() / getBox().getHeight();
        return aspectRatio;
    }

    public static ArrayList<Letter> findLetters(PointList pl) {
        ArrayList<Letter> toReturn = new ArrayList<>();
        if(pl.size() > 0){
            toReturn.add(new Letter(pl.get(0)));
            for (int i = 1; i < pl.size(); i++) {
                Point p = pl.get(i);

                //go through the list of letters and see if this grid belongs to any one of them; iterate backwards for efficiency
                boolean newLetterNeeded = true;
                for (int j = toReturn.size() - 1; j >= 0; j--) {
                    if (toReturn.get(j).pointTouchesLetter(p)) {
                        toReturn.get(j).addPoint(p);
                        j = 0;
                        newLetterNeeded = false;
                    }
                }
                if (newLetterNeeded) {
                    toReturn.add(new Letter(p));
                }
            }

            //combine letters if they overlap
            for (int j = toReturn.size() - 1; j > 0; j--) {
                for(int k = j; k > 0; k--){
                    if (toReturn.get(j).letterTouchesLetter(toReturn.get(k - 1))) {
                        toReturn.get(k - 1).addLetter(toReturn.get(j));
                        toReturn.remove(j);
                        k = 0;
                    }
                }
            }
        }

        return toReturn;
    }

    public void setKnownLetter(String letter) {
        this.knownLetter = letter;
    }

    public String getKnownLetter() {
        return knownLetter;
    }

    public JSONObject toJSONObject() {
        JSONObject toReturn = new JSONObject();
        toReturn.put("knownLetter", knownLetter);
        toReturn.put("list", list.toJSONArray());
        return toReturn;
    }

    public static Letter fromJSONObject(JSONObject obj) {
        Letter l = new Letter();
        l.knownLetter = obj.getString("knownLetter");
        l.list = PointList.fromJSONArray(obj.getJSONArray("list"));
        return l;
    }

    @Override
    public String toString(){
        return toJSONObject().toString();
    }
}
