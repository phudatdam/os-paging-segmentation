package model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Paging {
    private final List<Program> programs;
    private final List<String> memory;
    private final List<Integer> freeFrames;
    private int frameSize;
    private int nextPID;

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

    public void addProgram(String name, int size, Color color) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Cần thêm tên chương trình.");
        }
        if (isProgramNameExists(name)) {
            throw new IllegalArgumentException("Chương trình đã tồn tại.");
        }

        int totalFramesNeeded = (int) Math.ceil((double) size / frameSize);
        if (totalFramesNeeded > freeFrames.size()) {
            throw new IllegalArgumentException("Không đủ trang vật lý tự do để nạp chương trình này.");
        }

        Program newProgram = new Program(nextPID++, name, size, color);
        Random random = new Random();

        for (int pageIndex = 0; pageIndex < totalFramesNeeded; pageIndex++) {
            int randomIndex = random.nextInt(freeFrames.size());
            int frame = freeFrames.get(randomIndex);
            freeFrames.remove(randomIndex);
            int pageSize = Math.min(frameSize, size - pageIndex * frameSize);
            for (int i = 0; i < pageSize; i++) {
                memory.set(frame * frameSize + i, "PID " + newProgram.getPID() + " - Page " + pageIndex + " - " + i + " (" + name + ")");
            }
            Page newPage = new Page(pageIndex, frame);
            newProgram.addPage(newPage);
        }
        programs.add(newProgram);
    }

    public void initializeMemory(int memorySize, int frameSize, int osSize) {
        this.frameSize = frameSize;
        // Reset memory
        programs.clear();
        memory.clear();
        nextPID = 0;
        // Initialize memory as free
        for (int i = 0; i < memorySize; i++) {
            memory.add("Tự do");
        }
        for (int i = 0; i < memorySize / frameSize; i++) {
            freeFrames.add(i);
        }
        // Add OS program
        Program osProgram = new Program(-1, "OS", osSize, Color.LIGHT_GRAY);
        int totalFramesNeeded = (int) Math.ceil((double) osSize / frameSize);
        for (int pageIndex = 0; pageIndex < totalFramesNeeded; pageIndex++) {
            int pageSize = Math.min(frameSize, osSize - pageIndex * frameSize);
            for (int i = 0; i < pageSize; i++) {
                memory.set(pageIndex * frameSize + i, "OS");
            }
            // Remove from free frames list
            freeFrames.remove(pageIndex);
            Page osPage = new Page(pageIndex, pageIndex);
            osProgram.addPage(osPage);
        }
        programs.add(osProgram);
    }

    public Program findProgramByPID(int pid) {
        for (Program program : programs) {
            if (program.getPID() == pid) {
                return program;
            }
        }
        return null;
    }

    public boolean isProgramNameExists(String name) {
        for (Program program : programs) {
            if (program.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}