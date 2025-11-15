//Code completed by Roland Okungbowa

import java.util.LinkedList;
import java.util.Random;
import java.util.Queue;

public abstract class Scheduler {
    abstract boolean isPreemptive();
    abstract void schedule();
    abstract Process getNextProcess();
    abstract int getCores();
    abstract int getBurstTime();
    abstract void addProcess(Queue<Process> processList, Process p);
    protected Queue<Process> processList;

    public void createProcesses() {
        //creates processes
        processList = new LinkedList<>();
        Random rand = new Random();
        int numProcesses = rand.nextInt(25)+1 ; //between 1 and 25 processes
        System.out.println("\nNumber of processes created: " + numProcesses + "\n");
        for (int i = 0; i < numProcesses; i++) {
            int burstTime = rand.nextInt(50) + 1; //burst time between 1 and 50
            Process p = new Process(i, burstTime);
            addProcess(processList, p);
        }
    }

    public int getProcesses() {
        return processList.size();
    }
    
}