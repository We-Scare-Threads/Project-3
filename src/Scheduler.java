//Scaffolding done by Roland Okungbowa

import java.util.Random;

public abstract class Scheduler {
    abstract boolean isPreemptive();
    abstract void schedule();
    abstract void addProcess(Process p);
    abstract Process getNextProcess();
    abstract int getCores();
    abstract int getProcesses();
    abstract int getBurstTime();

    
}