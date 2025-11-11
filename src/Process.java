//Scaffolding done by Roland Okungbowa

public class Process extends Thread { 
    private String processName;
    private int burstTime;
    private int arrivalTime;
    private int remainingTime;
    private int processId; 

    public Process(int processId, int burstTime, int arrivalTime) {
        this.processId = processId;
        this.processName = "Process-" + processId;
        this.burstTime = burstTime;
        this.arrivalTime = arrivalTime;
        this.remainingTime = burstTime;
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
        // Thread execution logic - to be implemented
    }
}