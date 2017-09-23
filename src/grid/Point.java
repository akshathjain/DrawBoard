/*
Name: Akshath Jain
Date: 5/6/17
Purpose: basic grid class
 */

package grid;

import org.json.JSONObject;

public class Point {
    private int x;
    private int y;
    private int width;
    private int height;

    public Point(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Point(int x, int y, int size) {
        this.x = x;
        this.y = y;
        this.width = this.height = size;
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
        this.width = this.height = 1;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    @Override
    public boolean equals(Object other) {
        Point p = (Point) other;
        return this.x == p.x && this.y == p.y;
    }

    public boolean touches(Point other) {
        int minW = -1 * this.width;
        int maxW = other.width ;

        int minH = -1 * this.height;
        int maxH = other.height;

        int dw = this.x - other.x;
        int dh = this.y - other.y;

        return dw >= minW && dw <= maxW && dh >= minH && dh <= maxH;
    }

    public JSONObject toJSONObject(){
        JSONObject obj = new JSONObject();
        obj.put("x", x);
        obj.put("y", y);
        obj.put("width", width);
        obj.put("height", height);
        return obj;
    }

    public static Point fromJSONObject(JSONObject obj){
        Point p = new Point(obj.getInt("x"), obj.getInt("y"));
        p.width = obj.getInt("width");
        p.height = obj.getInt("height");
        return p;
    }
}