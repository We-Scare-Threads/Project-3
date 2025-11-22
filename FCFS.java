//Code completed by Roland Okungbowa

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class FCFS extends Scheduler {
    public int ID;
    private static int cores;
    private int totalBurstTime;

    public FCFS(int cores, int ID) {
        this.cores = cores;
        this.totalBurstTime = 0;
        this.ID = ID;
        createProcesses();
        schedule();
    }

    public static void main(String[] args) {
        System.out.println("FCFS Scheduling Algorithm Executed.");
        System.out.println("Number of cores: " + cores);

    }

    @Override
    public void addProcess(Queue<Process> processList, Process p){
        processList.offer(p);
    }

    @Override
    boolean isPreemptive() {
        return false;
    }

    @Override
    void schedule() {
        SynchronizedPrinter.printSeparator();
        SynchronizedPrinter.printWithCategory("SCHEDULER", "Starting FCFS with " + cores + " cores");
        SynchronizedPrinter.printSeparator();
        
        if (processList == null || processList.isEmpty()) {
            SynchronizedPrinter.printWithCategory("SCHEDULER", "No processes to schedule");
            return;
        }
        
        // Simple approach: assign processes to cores as they become available (FCFS order)
        java.util.List<Thread> coreThreads = new java.util.ArrayList<>();
        java.util.concurrent.BlockingQueue<Process> processQueue = new java.util.concurrent.LinkedBlockingQueue<>(processList);
        java.util.concurrent.atomic.AtomicInteger completedProcesses = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger currentTime = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.List<Process> completedList = java.util.Collections.synchronizedList(new java.util.ArrayList<>());
        
        // Calculate total burst time
        for (Process p : processList) {
            totalBurstTime += p.getBurstTime();
        }
        int totalProcessCount = processList.size();
        
        // Start threads for each core that will pick up processes in FCFS order
        for (int i = 0; i < cores; i++) {
            final int coreId = i;
            
            Thread coreThread = new Thread(() -> {
                int coreTime = 0;
                while (completedProcesses.get() < totalProcessCount) {
                    try {
                        Process process = processQueue.poll(100, java.util.concurrent.TimeUnit.MILLISECONDS);
                        if (process != null) {
                            // Wait for arrival time
                            coreTime = Math.max(coreTime, process.getArrivalTime());
                            
                            SynchronizedPrinter.printWithCategory("CORE-" + coreId, "Starting Process " + 
                                process.getProcessId() + " (Burst: " + process.getBurstTime() + ")");
                            
                            process.run();
                            
                            coreTime += process.getBurstTime();
                            process.setCompletionTime(coreTime);
                            completedList.add(process);
                            
                            SynchronizedPrinter.printWithCategory("CORE-" + coreId, "Completed Process " + 
                                process.getProcessId());
                            
                            completedProcesses.incrementAndGet();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
            
            coreThreads.add(coreThread);
            coreThread.start();
        }
        
        // Wait for all cores to finish
        for (Thread t : coreThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Calculate average wait time
        double totalWaitTime = 0;
        for (Process p : completedList) {
            totalWaitTime += p.getWaitTime();
        }
        double avgWaitTime = completedList.isEmpty() ? 0 : totalWaitTime / completedList.size();
        
        SynchronizedPrinter.printSeparator();
        SynchronizedPrinter.printWithCategory("SCHEDULER", "FCFS Complete!");
        SynchronizedPrinter.printWithCategory("SCHEDULER", "Total processes: " + totalProcessCount);
        SynchronizedPrinter.printWithCategory("SCHEDULER", "Total burst time: " + totalBurstTime);
        SynchronizedPrinter.printWithCategory("SCHEDULER", String.format("Average wait time: %.2f time units", avgWaitTime));
        SynchronizedPrinter.printSeparator();
    }

    @Override
    Process getNextProcess() {
        return (processList != null && !processList.isEmpty()) ? processList.peek() : null;
    }

    @Override
    int getCores() {
        return cores;
    }

    @Override
    int getBurstTime() {
        return totalBurstTime;
    }
}