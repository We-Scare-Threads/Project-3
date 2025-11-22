//Scaffolding done by Justin Le

import java.util.LinkedList;
import java.util.Queue;

public class PSJ extends Scheduler {
    private int cores;
    private Queue<Process> processQueue;
    private int totalBurstTime;

    public PSJ(int cores) {
        this.cores = cores;
        this.processQueue = new LinkedList<>();
        this.totalBurstTime = 0;
        createProcesses();
        schedule();
    }
    
    public PSJ(int cores, java.util.List<Process> processes) {
        this.cores = cores;
        this.processQueue = new LinkedList<>();
        this.totalBurstTime = 0;
        this.processList = new LinkedList<>();
        
        System.out.println("\nNumber of processes created: " + processes.size() + "\n");
        for (Process p : processes) {
            System.out.println("Process " + p.getProcessId() + ": Arrival=" + p.getArrivalTime() + ", Burst=" + p.getBurstTime());
            addProcess(this.processList, p);
        }
        
        schedule();
    }

    public static void main(String[] args) {
        System.out.println("PSJ Scheduling Algorithm Executed.");
        System.out.println("Number of cores: " + args[0]);
    }

    @Override
    public void addProcess(Queue<Process> processList, Process p){
        // Add process maintaining arrival time order (preemption handled in schedule())
        processQueue.offer(p);
        totalBurstTime += p.getBurstTime();
    }

    @Override
    boolean isPreemptive() {
        return true;
    }

    @Override
    void schedule() {
        SynchronizedPrinter.printSeparator();
        SynchronizedPrinter.printWithCategory("SCHEDULER", "Starting Preemptive Shortest Job First with " + cores + " cores");
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
            initialReady.sort(java.util.Comparator.comparingInt(Process::getRemainingTime));
            sharedReadyQueue.addAll(initialReady);
        }
        
        // Start threads for each core
        for (int i = 0; i < cores; i++) {
            final int coreId = i;
            
            Thread coreThread = new Thread(() -> {
                Process currentProcess = null;
                int coreTime = minArrival;
                
                while (completed.size() < totalProcessCount) {
                    try {
                        // Get next process if we don't have one
                        if (currentProcess == null || currentProcess.getRemainingTime() <= 0) {
                            if (currentProcess != null && currentProcess.getRemainingTime() <= 0) {
                                currentProcess.setCompletionTime(coreTime);
                                completedList.add(currentProcess);
                                SynchronizedPrinter.printWithCategory("CORE-" + coreId, "Completed Process " + 
                                    currentProcess.getProcessId());
                                completed.add(currentProcess);
                            }
                            
                            currentProcess = sharedReadyQueue.poll(100, java.util.concurrent.TimeUnit.MILLISECONDS);
                            if (currentProcess != null && currentProcess.getRemainingTime() > 0) {
                                SynchronizedPrinter.printWithCategory("CORE-" + coreId, "Starting Process " + 
                                    currentProcess.getProcessId() + " (Arrival: " + currentProcess.getArrivalTime() + 
                                    ", Remaining: " + currentProcess.getRemainingTime() + " - Shortest Remaining)");
                            }
                        }
                        
                        if (currentProcess != null && currentProcess.getRemainingTime() > 0) {
                            // Execute for 1 time unit
                            Thread.sleep(50); // 1 time unit = 50ms simulation
                            currentProcess.setRemainingTime(currentProcess.getRemainingTime() - 1);
                            coreTime++;
                            
                            synchronized(lock) {
                                currentTime.set(coreTime);
                                
                                // Check for newly arrived processes at current time
                                java.util.List<Process> newlyArrived = new java.util.ArrayList<>();
                                for (Process p : allProcesses) {
                                    if (p.getArrivalTime() == currentTime.get() && !completed.contains(p)) {
                                        newlyArrived.add(p);
                                    }
                                }
                                
                                // Add newly arrived processes to ready queue
                                if (!newlyArrived.isEmpty()) {
                                    sharedReadyQueue.addAll(newlyArrived);
                                }
                                
                                // Check for preemption: see if any process in ready queue has shorter remaining time
                                Process shortestInQueue = null;
                                java.util.List<Process> tempQueue = new java.util.ArrayList<>();
                                sharedReadyQueue.drainTo(tempQueue);
                                
                                if (!tempQueue.isEmpty()) {
                                    // Find process with shortest remaining time in ready queue
                                    shortestInQueue = tempQueue.stream()
                                        .min(java.util.Comparator.comparingInt(Process::getRemainingTime))
                                        .orElse(null);
                                    
                                    // Preempt if ready queue has a process with shorter remaining time
                                    if (shortestInQueue != null && shortestInQueue.getRemainingTime() < currentProcess.getRemainingTime()) {
                                        SynchronizedPrinter.printWithCategory("CORE-" + coreId, "Process " + 
                                            currentProcess.getProcessId() + " preempted (Remaining: " + 
                                            currentProcess.getRemainingTime() + ")");
                                        
                                        // Add current process back to queue
                                        tempQueue.add(currentProcess);
                                        currentProcess = null;
                                    }
                                }
                                
                                // Re-sort and add all processes back to queue
                                tempQueue.sort(java.util.Comparator.comparingInt(Process::getRemainingTime));
                                sharedReadyQueue.addAll(tempQueue);
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
        SynchronizedPrinter.printWithCategory("SCHEDULER", "Preemptive SJF Complete!");
        SynchronizedPrinter.printWithCategory("SCHEDULER", "Total processes: " + totalProcessCount);
        SynchronizedPrinter.printWithCategory("SCHEDULER", "Total burst time: " + totalBurstTime);
        SynchronizedPrinter.printWithCategory("SCHEDULER", String.format("Average wait time: %.2f time units", avgWaitTime));
        SynchronizedPrinter.printSeparator();
    }

    @Override
    Process getNextProcess() {
        return processQueue.peek(); // Return process with shortest remaining time
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