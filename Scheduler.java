//Code completed by Roland Okungbowa

import java.util.LinkedList;
import java.util.Random;
import java.util.Queue;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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
        
        // Create a list to sort processes by arrival time
        List<Process> tempProcessList = new ArrayList<>();
        
        for (int i = 0; i < numProcesses; i++) {
            int burstTime = rand.nextInt(50) + 1; //burst time between 1 and 50
            int arrivalTime = rand.nextInt(10) + 1; // arrival time between 1 and 10
            Process p = new Process(i + 1, arrivalTime, burstTime); // processId starts from 1
            tempProcessList.add(p);
        }
        
        // Sort processes by arrival time
        Collections.sort(tempProcessList, new Comparator<Process>() {
            @Override
            public int compare(Process p1, Process p2) {
                return Integer.compare(p1.getArrivalTime(), p2.getArrivalTime());
            }
        });
        
        // Add sorted processes to the queue
        for (Process p : tempProcessList) {
            addProcess(processList, p);
            System.out.println("Process " + p.getProcessId() + ": Arrival=" + p.getArrivalTime() + ", Burst=" + p.getBurstTime());
        }
    }

    public int getProcesses() {
        return processList.size();
    }
    
}