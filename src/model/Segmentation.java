package model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Segmentation {
    private final List<Segment> segments; // List of segments in memory
    private final List<String> memory; // Memory represented as a list of strings
    private int nextSID; // Next segment ID to be assigned

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


    // Add a segment to memory
    public void addSegment(String name, int size, Color color) {
        // Validate input
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Cần thêm tên đoạn.");
        }
        if (isSegmentNameExists(name)) {
            throw new IllegalArgumentException("Đoạn đã tồn tại trong bộ nhớ.");
        }

        // Generate a random start address for the segment
        int startAddress = findRandomStartAddress(size);
        if (startAddress == -1) {
            throw new IllegalArgumentException("Không đủ vùng trống tự do để nạp đoạn này.");
        }

        // Create segment and allocate memory
        Segment newSegment = new Segment(nextSID++, name, startAddress, size, color);
        for (int i = 0; i < size; i++) {
            memory.set(startAddress + i, "Đoạn " + newSegment.getSID() + " (" + name + ") - " + i);
        }

        // Add segment to segments list and mark it as used
        segments.add(newSegment);
        newSegment.setMark(1);
    }

    public void removeSegment(int sid) {
        Segment s = findSegmentBySID(sid);
        if (s != null) {
            for (int i = s.getAddress(); i < s.getAddress() + s.getLength(); i++) {
                memory.set(i, "Tự do"); // hoặc " " nếu dùng chuỗi
            }
            segments.remove(s);
        }
    }

    // Initialize memory and OS segment
    public void initializeMemory(int memorySize, int osSize) {
        // Reset memory
        segments.clear();
        memory.clear();
        nextSID = 0;

        // Initialize memory as free
        for (int i = 0; i < memorySize; i++) {
            memory.add("Tự do");
        }

        // Create OS segment and allocate memory
        Segment osSegment = new Segment(-1, "OS", 0, osSize, Color.LIGHT_GRAY); // ID = -1 so that user's segment ID starts from 0
        for (int i = 0; i < osSize; i++) {
            memory.set(i, "OS");
        }

        // Add OS segment to segments list and mark it as used
        segments.add(osSegment);
        osSegment.setMark(1);
    }


    // Find a segment by its ID
    public Segment findSegmentBySID(int sid) {
        for (Segment segment : segments) {
            if (segment.getSID() == sid) {
                return segment;
            }
        }
        return null;
    }


    // Check if a segment name already exists
    private boolean isSegmentNameExists(String name) {
        for (Segment segment : segments) {
            if (segment.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }


    // Generate a random start address for the segment
    private int findRandomStartAddress(int segmentSize) {
        // Get all free addresses in memory
        List<Integer> freeAddresses = new ArrayList<>();
        for (int i = 0; i < memory.size(); i++) {
            if (memory.get(i).equals("Tự do")) {
                freeAddresses.add(i);
            }
        }

        // Randomly select a free address and check if it has enough contiguous space
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
        return -1; // Not enough contiguous space for the segment
    }


    // Check if there is enough contiguous space in memory for the segment from the start address
    private boolean isEnoughContiguousSpace(int startAddress, int segmentSize) {
        if (startAddress + segmentSize > memory.size()) {
            return false;
        }
        for (int i = 0; i < segmentSize; i++) {
            if (!memory.get(startAddress + i).equals("Tự do")) {
                return false;
            }
        }
        return true; // If all addresses from startAddress to startAddress + segmentSize are free
    }


    // Translate a logical address to a physical address
    public int translateAddress(int segmentID, int offset) {
        Segment segment = findSegmentBySID(segmentID);
        if (segment == null || segment.getMark() == 0) {
            throw new IllegalArgumentException("Đoạn không có trong bộ nhớ.");
        }
        if (offset < 0 || offset >= segment.getLength()) {
            throw new IllegalArgumentException("Offset vượt quá kích thước đoạn.");
        }
        return segment.getAddress() + offset;
    }
}