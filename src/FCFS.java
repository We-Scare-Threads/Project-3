//Scaffolding done by Roland Okungbowa

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class FCFS extends Scheduler {
    public int ID;
    private int cores;
    public static Queue<Process> processQueue;
    private int totalBurstTime;

    public FCFS(int cores, int ID) {
        this.cores = cores;
        this.processQueue = new LinkedList<>();
        this.totalBurstTime = 0;
        this.ID = ID;

        print();

    }
    public void print() {
        System.out.println("Hi, I am FCFS thread #"+ID);
    }


    @Override
    boolean isPreemptive() {
        return false;
    }

    @Override
    void schedule() {
        // Implementation of FCFS scheduling algorithm
    }

    @Override
    void addProcess(Process p) {
        // Add process to the scheduler
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
    int getProcesses() {
        // Return the number of processes
        return 0;
    }

    @Override
    int getBurstTime() {
        // Return the total burst time
        return 0;
    }
}