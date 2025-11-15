//Scaffolding done by Roland Okungbowa
import java.util.Random;

public class Process extends Thread { 
    private String processName;
    private int burstTime;
    private int arrivalTime;
    private int remainingTime;
    private int processId; 

    public Process(int processId, int burstTime) {
        this.processId = processId;
        this.processName = "Process-" + processId;
        this.remainingTime = burstTime;
        this.burstTime = burstTime;
        this.arrivalTime = 0; // Default arrival time set to 0
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

    @Override
    public void run() {
        SynchronizedPrinter.printWithCategory("PROCESS-" + processId, "Starting (Burst: " + burstTime + " units)");
        
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