package model;

public class Page {
    private int pageID; // ID of the page
    private int mark; // Mark to indicate if the page is in memory
    private int address; // Address of the frame that the page is mapped to

    public Page(int pageID, int address) {
        this.pageID = pageID;
        mark = 0;
        this.address = address;
    }

    public int getPageID() {
        return pageID;
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
}