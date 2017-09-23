package grid; /**
 * Name: Akshath Jain
 * Date: 5/7/17
 * Purpose: grid.PointList class acting as a grid sparse array
 */

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class PointList extends ArrayList<Point> {
    public PointList() {
        super();
    }

    //adds grid to list iff grid isn't already in list given by this.contains(grid)
    @Override
    public boolean add(Point point) {
        if (!super.contains(point))
            return super.add(point);
        else
            return false;
    }

    //adds all the points between grid and this.get(size() - 1) given that size() > 0; else, just adds grid
    public boolean addBetween(Point point) {
        if (this.size() > 0) {
            Point old = this.get(size() - 1);
            double x0 = old.getX();
            double y0 = old.getY();
            int dy = old.getY() - point.getY();
            int dx = old.getX() - point.getX();
            int steps = (Math.abs(dx) > Math.abs(dy) ? Math.abs(dx) : Math.abs(dy));
            double xIncr = (double) dx / steps;
            double yIncr = (double) dy / steps;
            for (int i = 0; i < steps; i++) {
                x0 -= xIncr;
                y0 -= yIncr;
                this.add(new Point((int)Math.round(x0), (int) Math.round(y0), point.getWidth(), point.getHeight()));
            }
        }
        return this.add(point);
    }

    @Override
    public Point get(int index) {
        return super.get(index);
    }

    @Override
    public int size() {
        return super.size();
    }

    public JSONArray toJSONArray(){
        JSONArray ar = new JSONArray();
        for(int i = 0; i < size(); i++)
            ar.put(get(i).toJSONObject());
        return ar;
    }

    public static PointList fromJSONArray(JSONArray ar){
        PointList list = new PointList();
        for(int i = 0; i < ar.length(); i++)
            list.add(Point.fromJSONObject((JSONObject)ar.get(i)));
        return list;
    }

}

