//Scaffolding done by Roland Okungbowa

import java.util.LinkedList;
import java.util.Queue;

public class RR extends Scheduler {
    private int cores;
    private Queue<Process> processQueue;
    private int totalBurstTime;
    private int timeQuantum;

    public RR(int cores, int timeQuantum) {
        this.cores = cores;
        this.processQueue = new LinkedList<>();
        this.totalBurstTime = 0;
        this.timeQuantum = timeQuantum;
    }
    public static void main(String[] args) {
        System.out.println("RR Scheduling Algorithm Executed.");
        System.out.println("Number of cores: " + args[0]);
    }

    @Override
    public void addProcess(Queue<Process> processList, Process p){
    }

    @Override
    boolean isPreemptive() {
        return true;
    }

    @Override
    void schedule() {
        // Implementation of RR scheduling algorithm
    }

    @Override
    Process getNextProcess() {
        // Return the next process to be scheduled
        return null;
    }

    @Override
    int getCores() {
        return cores;
    }

    @Override
    int getBurstTime() {
        // Return the total burst time
        return 0;
    }
}