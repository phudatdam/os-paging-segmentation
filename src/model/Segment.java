package model;

import java.awt.Color;

public class Segment {
    private int SID; // ID of the segment
    private String name; // Name of the segment
    private int mark; // Mark to indicate if the segment is in memory
    private int address; // Base address of the segment in memory
    private int length; // Length of the segment
    private Color color; // Color of the segment for visualization

    public Segment(int SID, String name, int address, int length, Color color) {
        this.SID = SID;
        this.name = name;
        mark = 0;
        this.address = address;
        this.length = length;
        this.color = color;
    }

    public int getSID() {
        return SID;
    }

    public String getName() {
        return name;
    }

    public int getMark() {
        return mark;
    }

    public void setMark(int mark) {
        this.mark = mark;
    }

    public int getAddress() {
        return address;
    }

    public int getLength() {
        return length;
    }

    public Color getColor() {
        return color;
    }
}