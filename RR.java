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
        createProcesses();
        schedule();
    }
    public static void main(String[] args) {
        System.out.println("RR Scheduling Algorithm Executed.");
        System.out.println("Number of cores: " + args[0]);
    }

    @Override
    public void addProcess(Queue<Process> processList, Process p){
        processQueue.offer(p); // Add to RR queue in arrival order
        totalBurstTime += p.getBurstTime();
    }

    @Override
    boolean isPreemptive() {
        return true;
    }

    @Override
    void schedule() {
        SynchronizedPrinter.printSeparator();
        SynchronizedPrinter.printWithCategory("SCHEDULER", "Starting Round Robin with " + cores + " cores (Quantum: " + timeQuantum + ")");
        SynchronizedPrinter.printSeparator();
        
        if (processQueue == null || processQueue.isEmpty()) {
            SynchronizedPrinter.printWithCategory("SCHEDULER", "No processes to schedule");
            return;
        }
        
        // Round Robin: processes get time slices, cycling through the queue
        java.util.List<Thread> coreThreads = new java.util.ArrayList<>();
        java.util.concurrent.BlockingQueue<Process> rrQueue = new java.util.concurrent.LinkedBlockingQueue<>(processQueue);
        java.util.concurrent.atomic.AtomicInteger completedProcesses = new java.util.concurrent.atomic.AtomicInteger(0);
        int totalProcessCount = processQueue.size();
        
        // Start threads for each core
        for (int i = 0; i < cores; i++) {
            final int coreId = i;
            
            Thread coreThread = new Thread(() -> {
                while (completedProcesses.get() < totalProcessCount) {
                    try {
                        Process process = rrQueue.poll(100, java.util.concurrent.TimeUnit.MILLISECONDS);
                        if (process != null && process.getRemainingTime() > 0) {
                            SynchronizedPrinter.printWithCategory("CORE-" + coreId, "Starting Process " + 
                                process.getProcessId() + " (Remaining: " + process.getRemainingTime() + ", Quantum: " + timeQuantum + ")");
                            
                            // Execute for time quantum or until completion
                            int executeTime = Math.min(timeQuantum, process.getRemainingTime());
                            
                            // Simulate execution for the time slice
                            try {
                                Thread.sleep(executeTime * 50);
                                process.setRemainingTime(process.getRemainingTime() - executeTime);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                            
                            if (process.getRemainingTime() <= 0) {
                                SynchronizedPrinter.printWithCategory("CORE-" + coreId, "Completed Process " + 
                                    process.getProcessId());
                                completedProcesses.incrementAndGet();
                            } else {
                                SynchronizedPrinter.printWithCategory("CORE-" + coreId, "Process " + 
                                    process.getProcessId() + " preempted (Remaining: " + process.getRemainingTime() + ")");
                                rrQueue.offer(process); // Put back in queue for next time slice
                            }
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
        
        SynchronizedPrinter.printSeparator();
        SynchronizedPrinter.printWithCategory("SCHEDULER", "Round Robin Complete!");
        SynchronizedPrinter.printWithCategory("SCHEDULER", "Total processes: " + totalProcessCount);
        SynchronizedPrinter.printWithCategory("SCHEDULER", "Total burst time: " + totalBurstTime);
        SynchronizedPrinter.printWithCategory("SCHEDULER", "Time quantum: " + timeQuantum);
        SynchronizedPrinter.printSeparator();
    }

    @Override
    Process getNextProcess() {
        return processQueue.peek(); // Return next process without removing
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