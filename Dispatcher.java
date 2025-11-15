//Code completed by Roland Okungbowa

import java.util.concurrent.Semaphore;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Dispatcher {
    private int contextSwitchTime;
    Semaphore[] coreSemaphores;
    private ExecutorService executor;

    public Dispatcher(int cores) {
        this.contextSwitchTime = 1; // Time units for context switch
        coreSemaphores = new Semaphore[cores];
        for (int i = 0; i < cores; i++) {
            coreSemaphores[i] = new Semaphore(1);
        }
        executor = Executors.newFixedThreadPool(cores); // Thread pool for parallel execution
    }
    
    public CompletableFuture<Integer> dispatchAsync(Process process, int processStartTime) {
        return CompletableFuture.supplyAsync(() -> {
            int allocatedCore = -1;
            long actualStartTime = System.currentTimeMillis();
            
            try {
                // Try to acquire any available core
                for (int i = 0; i < coreSemaphores.length; i++) {
                    if (coreSemaphores[i].tryAcquire()) {
                        allocatedCore = i;
                        break;
                    }
                }
                
                if (allocatedCore == -1) {
                    // No core immediately available, wait for first available core
                    SynchronizedPrinter.printWithCategory("PROCESS-" + process.getProcessId(), "Waiting for core...");
                    for (int i = 0; i < coreSemaphores.length; i++) {
                        try {
                            coreSemaphores[i].acquire();
                            allocatedCore = i;
                            break;
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return -1;
                        }
                    }
                }
                
                SynchronizedPrinter.printWithCategory("CORE-" + allocatedCore, "Loading Process " + process.getProcessId());
                
                // Simulate context switch overhead
                try {
                    Thread.sleep(contextSwitchTime * 10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                // Execute the process (this will run in parallel)
                process.run();
                
                SynchronizedPrinter.printWithCategory("CORE-" + allocatedCore, "Process " + process.getProcessId() + " completed");
                
                return allocatedCore;
                
            } finally {
                // Always release the core when process completes
                if (allocatedCore != -1) {
                    coreSemaphores[allocatedCore].release();
                }
            }
        }, executor);
    }
    
    public int getContextSwitchTime() {
        return contextSwitchTime;
    }
    
    public int getAvailableCores() {
        int availableCount = 0;
        for (Semaphore core : coreSemaphores) {
            availableCount += core.availablePermits();
        }
        return availableCount;
    }
    
    public int getTotalCores() {
        return coreSemaphores.length;
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}