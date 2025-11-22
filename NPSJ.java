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
        // Add process maintaining arrival time order (don't re-sort by burst time here)
        processQueue.offer(p);
        totalBurstTime += p.getBurstTime();
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
        
        // Convert to list for easier manipulation
        java.util.List<Process> allProcesses = new java.util.ArrayList<>(processQueue);
        java.util.Set<Process> completed = java.util.Collections.synchronizedSet(new java.util.HashSet<>());
        java.util.List<Process> completedList = java.util.Collections.synchronizedList(new java.util.ArrayList<>());
        java.util.List<Thread> coreThreads = new java.util.ArrayList<>();
        java.util.concurrent.atomic.AtomicInteger currentTime = new java.util.concurrent.atomic.AtomicInteger(0);
        int totalProcessCount = allProcesses.size();
        
        // Shared ready queue with synchronization
        java.util.concurrent.BlockingQueue<Process> sharedReadyQueue = new java.util.concurrent.LinkedBlockingQueue<>();
        Object lock = new Object();
        
        // Start with minimum arrival time
        int minArrival = allProcesses.stream().mapToInt(Process::getArrivalTime).min().orElse(1);
        currentTime.set(minArrival);
        
        // Add initially available processes
        synchronized(lock) {
            java.util.List<Process> initialReady = new java.util.ArrayList<>();
            for (Process p : allProcesses) {
                if (p.getArrivalTime() <= currentTime.get()) {
                    initialReady.add(p);
                }
            }
            initialReady.sort(java.util.Comparator.comparingInt(Process::getBurstTime));
            sharedReadyQueue.addAll(initialReady);
        }
        
        // Start threads for each core
        for (int i = 0; i < cores; i++) {
            final int coreId = i;
            
            Thread coreThread = new Thread(() -> {
                int coreTime = minArrival;
                while (completed.size() < totalProcessCount) {
                    try {
                        Process process = sharedReadyQueue.poll(100, java.util.concurrent.TimeUnit.MILLISECONDS);
                        if (process != null) {
                            coreTime = Math.max(coreTime, process.getArrivalTime());
                            
                            SynchronizedPrinter.printWithCategory("CORE-" + coreId, "Starting Process " + 
                                process.getProcessId() + " (Arrival: " + process.getArrivalTime() + 
                                ", Burst: " + process.getBurstTime() + " - Shortest Job)");
                            
                            // Execute the entire process (non-preemptive)
                            process.run();
                            
                            coreTime += process.getBurstTime();
                            process.setCompletionTime(coreTime);
                            
                            SynchronizedPrinter.printWithCategory("CORE-" + coreId, "Completed Process " + 
                                process.getProcessId());
                            
                            completed.add(process);
                            completedList.add(process);
                            
                            // Update time and add newly arrived processes
                            synchronized(lock) {
                                int newTime = coreTime;
                                currentTime.set(newTime);
                                
                                // Check for newly arrived processes
                                java.util.List<Process> newlyArrived = new java.util.ArrayList<>();
                                for (Process p : allProcesses) {
                                    if (p.getArrivalTime() <= newTime && !completed.contains(p) && !sharedReadyQueue.contains(p)) {
                                        newlyArrived.add(p);
                                    }
                                }
                                
                                if (!newlyArrived.isEmpty()) {
                                    // Sort newly arrived by burst time and add to ready queue
                                    java.util.List<Process> tempQueue = new java.util.ArrayList<>();
                                    sharedReadyQueue.drainTo(tempQueue);
                                    tempQueue.addAll(newlyArrived);
                                    tempQueue.sort(java.util.Comparator.comparingInt(Process::getBurstTime));
                                    sharedReadyQueue.addAll(tempQueue);
                                }
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
        
        // Calculate average wait time
        double totalWaitTime = 0;
        for (Process p : completedList) {
            totalWaitTime += p.getWaitTime();
        }
        double avgWaitTime = completedList.isEmpty() ? 0 : totalWaitTime / completedList.size();
        
        SynchronizedPrinter.printSeparator();
        SynchronizedPrinter.printWithCategory("SCHEDULER", "Non-Preemptive SJF Complete!");
        SynchronizedPrinter.printWithCategory("SCHEDULER", "Total processes: " + totalProcessCount);
        SynchronizedPrinter.printWithCategory("SCHEDULER", "Total burst time: " + totalBurstTime);
        SynchronizedPrinter.printWithCategory("SCHEDULER", String.format("Average wait time: %.2f time units", avgWaitTime));
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