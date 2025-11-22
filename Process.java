//Scaffolding done by Roland Okungbowa
import java.util.Random;

public class Process extends Thread { 
    private String processName;
    private int burstTime;
    private int arrivalTime;
    private int remainingTime;
    private int processId;
    private int completionTime; 

    public Process(int processId, int burstTime) {
        this.processId = processId;
        this.processName = "Process-" + processId;
        this.remainingTime = burstTime;
        this.burstTime = burstTime;
        this.arrivalTime = 0; // Default arrival time set to 0
    }
    
    public Process(int processId, int arrivalTime, int burstTime) {
        this.processId = processId;
        this.processName = "Process-" + processId;
        this.remainingTime = burstTime;
        this.burstTime = burstTime;
        this.arrivalTime = arrivalTime;
    }

    public String getProcessName() { return processName; }
    public int getBurstTime() { return burstTime; }
    public int getArrivalTime() { return arrivalTime; }
    public int getRemainingTime() { return remainingTime; }
    
    public int getProcessId() { 
        return processId; 
    }
    
    public void setRemainingTime(int remainingTime) { 
        this.remainingTime = remainingTime; 
    }
    
    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
    
    public void setCompletionTime(int completionTime) {
        this.completionTime = completionTime;
    }
    
    public int getCompletionTime() {
        return completionTime;
    }
    
    public int getWaitTime() {
        return completionTime - arrivalTime - burstTime;
    }

    @Override
    public void run() {
        SynchronizedPrinter.printWithCategory("PROCESS-" + processId, "Starting (Arrival: " + arrivalTime + ", Burst: " + burstTime + " units)");
        
        // Simulate actual work being done
        try {
            Thread.sleep(burstTime * 50); // Scale burst time to real delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            SynchronizedPrinter.printWithCategory("PROCESS-" + processId, "Interrupted!");
            return;
        }
        
        SynchronizedPrinter.printWithCategory("PROCESS-" + processId, "Finished");
    }
}