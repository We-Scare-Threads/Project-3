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

    public static void main(String[] args) {
        System.out.println("PSJ Scheduling Algorithm Executed.");
        System.out.println("Number of cores: " + args[0]);
    }

    @Override
    public void addProcess(Queue<Process> processList, Process p){
        // Add process to internal queue and track total burst time
        processQueue.offer(p);
        totalBurstTime += p.getBurstTime();
        
        // Convert to list, sort by remaining time (shortest remaining time first)
        java.util.List<Process> sortedProcesses = new java.util.ArrayList<>(processQueue);
        sortedProcesses.sort(java.util.Comparator.comparingInt(Process::getRemainingTime));
        
        // Clear and refill queue with sorted processes
        processQueue.clear();
        processQueue.addAll(sortedProcesses);
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
        
        // PSJ: processes sorted by remaining time, shortest remaining time first
        java.util.List<Thread> coreThreads = new java.util.ArrayList<>();
        java.util.concurrent.BlockingQueue<Process> psjtQueue = new java.util.concurrent.LinkedBlockingQueue<>(processQueue);
        java.util.concurrent.atomic.AtomicInteger completedProcesses = new java.util.concurrent.atomic.AtomicInteger(0);
        int totalProcessCount = processQueue.size();
        
        // Start threads for each core
        for (int i = 0; i < cores; i++) {
            final int coreId = i;
            
            Thread coreThread = new Thread(() -> {
                while (completedProcesses.get() < totalProcessCount) {
                    try {
                        Process process = psjtQueue.poll(100, java.util.concurrent.TimeUnit.MILLISECONDS);
                        if (process != null && process.getRemainingTime() > 0) {
                            SynchronizedPrinter.printWithCategory("CORE-" + coreId, "Starting Process " + 
                                process.getProcessId() + " (Remaining: " + process.getRemainingTime() + " - Shortest Remaining)");
                            
                            // Execute for a small time slice to allow preemption
                            int timeSlice = Math.min(5, process.getRemainingTime()); // Small slice for preemption
                            
                            try {
                                Thread.sleep(timeSlice * 50);
                                process.setRemainingTime(process.getRemainingTime() - timeSlice);
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
                                
                                // Re-sort and add back to queue based on remaining time
                                java.util.List<Process> tempList = new java.util.ArrayList<>();
                                psjtQueue.drainTo(tempList);
                                tempList.add(process);
                                tempList.sort(java.util.Comparator.comparingInt(Process::getRemainingTime));
                                psjtQueue.addAll(tempList);
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
        SynchronizedPrinter.printWithCategory("SCHEDULER", "Preemptive SJF Complete!");
        SynchronizedPrinter.printWithCategory("SCHEDULER", "Total processes: " + totalProcessCount);
        SynchronizedPrinter.printWithCategory("SCHEDULER", "Total burst time: " + totalBurstTime);
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