package model;

public class Page {
    private int pageID;
    private int mark;
    private int address;

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