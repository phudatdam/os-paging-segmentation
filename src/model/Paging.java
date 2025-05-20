package model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Paging {
    private final List<Program> programs; // List of programs in memory
    private final List<String> memory; // Memory represented as a list of strings
    private final List<Integer> freeFrames; // List of free frames in memory
    private int frameSize; // Size of each frame in bytes
    private int nextPID; // Next program ID to be assigned

    public Paging() {
        this.programs = new ArrayList<>();
        this.memory = new ArrayList<>();
        this.freeFrames = new ArrayList<>();
        this.nextPID = 0;
    }

    public List<Program> getPrograms() {
        return programs;
    }
    
    public List<String> getMemory() {
        return memory;
    }

    public int getFrameSize() {
        return frameSize;
    }


    // Add a program to memory
    public void addProgram(String name, int size, Color color) {
        // Validate input
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Cần thêm tên chương trình.");
        }
        if (isProgramNameExists(name)) {
            throw new IllegalArgumentException("Chương trình đã tồn tại trong bộ nhớ.");
        }

        // Calculate the number of frames needed for the program
        int totalFramesNeeded = (int) Math.ceil((double) size / frameSize);
        if (totalFramesNeeded > freeFrames.size()) {
            throw new IllegalArgumentException("Không đủ trang vật lý tự do để nạp chương trình này.");
        }

        // Create program and allocate frames
        Program newProgram = new Program(nextPID++, name, size, color);
        Random random = new Random();

        for (int pageIndex = 0; pageIndex < totalFramesNeeded; pageIndex++) {
            // Randomly select a free frame
            int randomIndex = random.nextInt(freeFrames.size());
            int frame = freeFrames.get(randomIndex);
            freeFrames.remove(randomIndex);
            // Allocate memory for the page
            int pageSize = Math.min(frameSize, size - pageIndex * frameSize);
            for (int i = 0; i < pageSize; i++) {
                memory.set(frame * frameSize + i, "CT " + newProgram.getPID() + " (" + name + ") - Trang " + pageIndex + " - " + i);
            }
            // Create a new page, add it to the program, and mark it as used
            Page newPage = new Page(pageIndex, frame);
            newProgram.addPage(newPage);
            newPage.setMark(1);
        }
        // Add program to programs list
        programs.add(newProgram);
    }


    // Initialize memory and OS program
    public void initializeMemory(int memorySize, int frameSize, int osSize) {
        // Reset memory
        this.frameSize = frameSize;
        programs.clear();
        memory.clear();
        freeFrames.clear();
        nextPID = 0;

        // Initialize memory as free
        for (int i = 0; i < memorySize; i++) {
            memory.add("Tự do");
        }
        for (int i = 0; i < memorySize / frameSize; i++) {
            freeFrames.add(i);
        }

        // Create OS program and allocate frames
        Program osProgram = new Program(-1, "OS", osSize, Color.LIGHT_GRAY); // ID = -1 so that user's program ID starts from 0
        int totalFramesNeeded = (int) Math.ceil((double) osSize / frameSize);

        for (int pageIndex = 0; pageIndex < totalFramesNeeded; pageIndex++) {
            freeFrames.remove(0); // Remove frame from free frames list
            // Allocate memory for the page
            int pageSize = Math.min(frameSize, osSize - pageIndex * frameSize);
            for (int i = 0; i < pageSize; i++) {
                memory.set(pageIndex * frameSize + i, "OS");
            }
            // Create a new page, add it to the OS program, and mark it as used
            Page osPage = new Page(pageIndex, pageIndex);
            osProgram.addPage(osPage);
            osPage.setMark(1);
        }
        // Add OS program to programs list
        programs.add(osProgram);
    }


    // Find a program by its ID
    public Program findProgramByPID(int pid) {
        for (Program program : programs) {
            if (program.getPID() == pid) {
                return program;
            }
        }
        return null;
    }


    // Check if a program name already exists
    public boolean isProgramNameExists(String name) {
        for (Program program : programs) {
            if (program.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    // Translate a logical address to a physical address
    public int translateAddress(int programID, int offset) {
        Program program = findProgramByPID(programID);
        if (program == null) {
            throw new IllegalArgumentException("Chương trình không có trong bộ nhớ.");
        }
        int pageIndex = offset / frameSize;
        int pageOffset = offset % frameSize;
        if (pageIndex >= program.getPages().size()) {
            throw new IllegalArgumentException("Offset vượt quá kích thước chương trình.");
        }
        Page page = program.getPages().get(pageIndex);
        if (page.getMark() == 0) {
            throw new IllegalArgumentException("Trang không có trong bộ nhớ.");
        }
        return page.getAddress() * frameSize + pageOffset;
    }
}