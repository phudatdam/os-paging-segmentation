package model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Program {
    private final List<Page> pages;
    private int PID;
    private String name;
    private int size;
    private Color color;

    public Program(int PID, String name, int size, Color color) {
        this.PID = PID;
        this.name = name;
        this.size = size;
        this.pages = new ArrayList<>();
        this.color = color;
    }

    public Color getColor() {
        return color;
    }    

    // Method to add a page to the process
    public void addPage(Page page) {
        pages.add(page);
        page.setMark(1);
    }

    public List<Page> getPages() {
        return pages;
    }

    public int getPID() {
        return PID;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }
}