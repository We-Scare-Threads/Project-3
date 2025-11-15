//Scaffolding done by Darrius Rious

import java.util.LinkedList;
import java.util.Queue;

public class NPSJ extends Scheduler {
    private int cores;
    private Queue<Process> processQueue;
    private int totalBurstTime;

    public NPSJ(int cores) {
        this.cores = cores;
        this.processQueue = new LinkedList<>();
        this.totalBurstTime = 0;
        createProcesses();
        schedule();
    }
    public static void main(String[] args) {
        System.out.println("NPSJ Scheduling Algorithm Executed.");
        System.out.println("Number of cores: " + args[0]);
    }

    @Override
    public void addProcess(Queue<Process> processList, Process p){
        // Add process to internal queue and track total burst time
        processQueue.offer(p);
        totalBurstTime += p.getBurstTime();
        
        // Convert to list, sort by burst time (shortest first), then back to queue
        java.util.List<Process> sortedProcesses = new java.util.ArrayList<>(processQueue);
        sortedProcesses.sort(java.util.Comparator.comparingInt(Process::getBurstTime));
        
        // Clear and refill queue with sorted processes
        processQueue.clear();
        processQueue.addAll(sortedProcesses);
    }

    @Override
    boolean isPreemptive() {
        return false;
    }

    @Override
    void schedule() {
        SynchronizedPrinter.printSeparator();
        SynchronizedPrinter.printWithCategory("SCHEDULER", "Starting Non-Preemptive Shortest Job First with " + cores + " cores");
        SynchronizedPrinter.printSeparator();
        
        if (processQueue == null || processQueue.isEmpty()) {
            SynchronizedPrinter.printWithCategory("SCHEDULER", "No processes to schedule");
            return;
        }
        
        // NPSJ: processes are already sorted by burst time, execute shortest first
        java.util.List<Thread> coreThreads = new java.util.ArrayList<>();
        java.util.concurrent.BlockingQueue<Process> sjfQueue = new java.util.concurrent.LinkedBlockingQueue<>(processQueue);
        java.util.concurrent.atomic.AtomicInteger completedProcesses = new java.util.concurrent.atomic.AtomicInteger(0);
        int totalProcessCount = processQueue.size();
        
        // Start threads for each core
        for (int i = 0; i < cores; i++) {
            final int coreId = i;
            
            Thread coreThread = new Thread(() -> {
                while (completedProcesses.get() < totalProcessCount) {
                    try {
                        Process process = sjfQueue.poll(100, java.util.concurrent.TimeUnit.MILLISECONDS);
                        if (process != null) {
                            SynchronizedPrinter.printWithCategory("CORE-" + coreId, "Starting Process " + 
                                process.getProcessId() + " (Burst: " + process.getBurstTime() + " - Shortest Job)");
                            
                            // Execute the entire process (non-preemptive)
                            process.run();
                            
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
        
        SynchronizedPrinter.printSeparator();
        SynchronizedPrinter.printWithCategory("SCHEDULER", "Non-Preemptive SJF Complete!");
        SynchronizedPrinter.printWithCategory("SCHEDULER", "Total processes: " + totalProcessCount);
        SynchronizedPrinter.printWithCategory("SCHEDULER", "Total burst time: " + totalBurstTime);
        SynchronizedPrinter.printSeparator();
    }

    @Override
    Process getNextProcess() {
        return processQueue.peek(); // Return shortest job next
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