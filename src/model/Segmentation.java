package model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Segmentation {
    private final List<Segment> segments;
    private final List<String> memory;
    private int nextSID;

    public Segmentation() {
        this.segments = new ArrayList<>();
        this.memory = new ArrayList<>();
        this.nextSID = 0;
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public List<String> getMemory() {
        return memory;
    }

    public void reset(int memorySize) {
        segments.clear();
        memory.clear();
        nextSID = 0;

        // Initialize memory as free
        for (int i = 0; i < memorySize; i++) {
            memory.add("Tự do");
        }
    }

    public void addSegment(String name, int size, Color color) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Cần thêm tên đoạn.");
        }
        if (isSegmentNameExists(name)) {
            throw new IllegalArgumentException("Đoạn đã tồn tại.");
        }
        int address = findRandomStartAddress(size);
        if (address == -1) {
            throw new IllegalArgumentException("Không đủ vùng trống tự do để nạp đoạn này.");
        }
        Segment newSegment = new Segment(nextSID++, name, address, size, color);
        segments.add(newSegment);
        newSegment.setMark(1);
        for (int i = 0; i < size; i++) {
            memory.set(address + i, "SID " + newSegment.getSID());
        }
    }

    public void initializeOSSegments(int osSize) {
        Segment osSegment = new Segment(-1, "OS", 0, osSize, Color.LIGHT_GRAY);
        segments.add(osSegment);
        osSegment.setMark(1);
        for (int i = 0; i < osSize; i++) {
            memory.set(i, "OS");
        }
    }

    public Segment findSegmentBySID(int sid) {
        for (Segment segment : segments) {
            if (segment.getSID() == sid) {
                return segment;
            }
        }
        return null;
    }

    private boolean isSegmentNameExists(String name) {
        for (Segment segment : segments) {
            if (segment.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private int findRandomStartAddress(int segmentSize) {
        List<Integer> freeAddresses = getFreeMemoryAddresses();
        Random random = new Random();
        while (!freeAddresses.isEmpty()) {
            int randomIndex = random.nextInt(freeAddresses.size());
            int startAddress = freeAddresses.get(randomIndex);
            if (isEnoughContiguousSpace(startAddress, segmentSize)) {
                return startAddress;
            } else {
                freeAddresses.remove(randomIndex);
            }
        }
        return -1;
    }

    private boolean isEnoughContiguousSpace(int startAddress, int segmentSize) {
        for (int i = 0; i < segmentSize; i++) {
            if (startAddress + i >= memory.size() || !"Tự do".equals(memory.get(startAddress + i))) {
                return false;
            }
        }
        return true;
    }

    private List<Integer> getFreeMemoryAddresses() {
        List<Integer> freeAddresses = new ArrayList<>();
        for (int i = 0; i < memory.size(); i++) {
            if ("Tự do".equals(memory.get(i))) {
                freeAddresses.add(i);
            }
        }
        return freeAddresses;
    }
}