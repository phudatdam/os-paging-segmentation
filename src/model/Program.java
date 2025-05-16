package model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Program {
    private final List<Page> pages; // List of pages in the program
    private int PID; // ID of the program
    private String name; // Name of the program
    private int size; // Size of the program in bytes
    private Color color; // Color of the program for visualization

    public Program(int PID, String name, int size, Color color) {
        this.PID = PID;
        this.name = name;
        this.size = size;
        this.pages = new ArrayList<>();
        this.color = color;
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

    public List<Page> getPages() {
        return pages;
    }

    public Color getColor() {
        return color;
    }    

    // Add a page to the program
    public void addPage(Page page) {
        pages.add(page);
        page.setMark(1);
    }
}