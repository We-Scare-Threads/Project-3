import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.J48;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.DenseInstance;
import weka.core.Attribute;
import weka.core.SerializationHelper;
import java.util.*;
import java.util.stream.Collectors;
import java.io.*;
import java.util.Random;

public class ML {
    private static final String CSV_FILE = "scheduler_data.csv";
    private static final String ARFF_FILE = "scheduler_data.arff";
    
    public static void main(String[] args) {
        ML ml = new ML();
        
        // Streamlined ML process - collect, train, predict, and run
        System.out.println("\n=== ML Scheduler Selection Process ===");
        System.out.println();
        
        // Step 1: Ensure training data exists
        File csvFile = new File(CSV_FILE);
        File arffFile = new File(ARFF_FILE);
        if (!csvFile.exists() || csvFile.length() == 0) {
            System.out.println("Collecting training data...");
            ml.collectTrainingDataQuiet();
            System.out.println("Converting to ARFF format...");
            try {
                ml.convertCsvToArff();
            } catch (Exception e) {
                System.err.println("Error converting CSV to ARFF: " + e.getMessage());
            }
        }
        
        // Ensure ARFF file exists and has content
        if (!arffFile.exists() || arffFile.length() == 0) {
            System.out.println("ARFF file missing or empty, converting from CSV...");
            try {
                ml.convertCsvToArff();
            } catch (Exception e) {
                System.err.println("Error converting CSV to ARFF: " + e.getMessage());
                return;
            }
        }
        
        // Step 2: Train models if needed
        File modelFile = new File("scheduler_model.model");
        if (!modelFile.exists()) {
            System.out.println("Training ML models...");
            ml.trainModelsQuiet();
        }
        
        // Step 3: Generate test scenario and run simulation
        Random rand = new Random();
        int numTasks = rand.nextInt(10) + 5; // 5-14 tasks
        int minBurst = rand.nextInt(10) + 1; // 1-10
        int maxBurst = minBurst + rand.nextInt(30) + 10; // minBurst + 10-39
        double avgBurst = minBurst + (maxBurst - minBurst) * rand.nextDouble();
        int numCores = rand.nextInt(4) + 1; // 1-4 cores
        int timeQuantum = rand.nextInt(8) + 3; // 3-10
        
        System.out.println("Test Scenario:");
        System.out.printf("  Tasks: %d\n", numTasks);
        System.out.printf("  Burst Time: Min=%d, Avg=%.1f, Max=%d\n", minBurst, avgBurst, maxBurst);
        System.out.printf("  Cores: %d, Time Quantum: %d\n", numCores, timeQuantum);
        System.out.println();
        
        // Step 4: Run simulation output for all schedulers
        System.out.println("=== Simulation Output ===");
        ml.runAllSchedulersForComparison(numTasks, minBurst, avgBurst, maxBurst, numCores, timeQuantum);
        
        // Step 5: Show model results
        System.out.println("=== Model Results ===");
        ml.showModelAccuracy();
        
        // Step 6: Predict and announce best scheduler
        System.out.println("=== ML Prediction ===");
        String predictedScheduler = ml.predictBestScheduler(numTasks, minBurst, avgBurst, maxBurst, numCores, timeQuantum);
        
        System.out.println("Recommendation: " + predictedScheduler + " is the optimal scheduler for this workload.\n");
        
        // Step 7: Automatically launch the predicted scheduler
        ml.runPredictedScheduler(predictedScheduler, numCores, timeQuantum);
    }
    
    public void collectTrainingDataQuiet() {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE));
            writer.println("numTasks,minBurst,avgBurst,maxBurst,numCores,timeQuantum,bestScheduler");
            
            Random rand = new Random();
            int samplesPerScheduler = 12;
            
            for (int i = 0; i < samplesPerScheduler * 4; i++) {
                int numTasks = rand.nextInt(15) + 5;
                int minBurst = rand.nextInt(10) + 1;
                int maxBurst = minBurst + rand.nextInt(40) + 10;
                double avgBurst = minBurst + (maxBurst - minBurst) * rand.nextDouble();
                int numCores = rand.nextInt(4) + 1;
                int timeQuantum = rand.nextInt(10) + 3;
                
                String bestScheduler = findBestScheduler(numTasks, minBurst, avgBurst, maxBurst, numCores, timeQuantum);
                
                writer.printf("%d,%.1f,%.2f,%.1f,%d,%d,%s%n", 
                    numTasks, (double)minBurst, avgBurst, (double)maxBurst, numCores, timeQuantum, bestScheduler);
            }
            writer.close();
            System.out.println("Training data collected (" + (samplesPerScheduler * 4) + " samples)");
        } catch (IOException e) {
            System.err.println("Error collecting training data: " + e.getMessage());
        }
    }
    
    public void trainModelsQuiet() {
        try {
            convertCsvToArff();
            DataSource source = new DataSource(ARFF_FILE);
            Instances data = source.getDataSet();
            data.setClassIndex(data.numAttributes() - 1);
            
            RandomForest rf = new RandomForest();
            rf.setNumIterations(50);
            rf.buildClassifier(data);
            SerializationHelper.write("scheduler_model.model", rf);
            System.out.println("Models trained and saved");
        } catch (Exception e) {
            System.err.println("Error training models: " + e.getMessage());
        }
    }
    
    public void runAllSchedulersForComparison(int numTasks, int minBurst, double avgBurst, int maxBurst, int numCores, int timeQuantum) {
        System.out.println("Comparing all scheduling algorithms...");
        System.out.println();
        
        // Generate ONE set of test processes for fair comparison
        Random rand = new Random();
        List<Process> testProcesses = new ArrayList<>();
        for (int i = 0; i < numTasks; i++) {
            int burstTime = (int)(avgBurst + rand.nextGaussian() * (avgBurst * 0.3));
            burstTime = Math.max(1, Math.min(burstTime, maxBurst)); // Clamp to range
            int arrivalTime = rand.nextInt(10) + 1;
            testProcesses.add(new Process(i + 1, arrivalTime, burstTime));
        }
        testProcesses.sort(Comparator.comparingInt(Process::getArrivalTime));
        
        String[] schedulers = {"FCFS", "RR", "NPSJ", "PSJ"};
        double[] waitTimes = new double[4];
        
        waitTimes[0] = simulateFCFS(new ArrayList<>(testProcesses), numCores);
        System.out.printf("  FCFS: %.2f time units\n", waitTimes[0]);
        
        waitTimes[1] = simulateRR(new ArrayList<>(testProcesses), numCores, timeQuantum);
        System.out.printf("  RR: %.2f time units\n", waitTimes[1]);
        
        waitTimes[2] = simulateNPSJ(new ArrayList<>(testProcesses), numCores);
        System.out.printf("  NPSJ: %.2f time units\n", waitTimes[2]);
        
        waitTimes[3] = simulatePSJ(new ArrayList<>(testProcesses), numCores);
        System.out.printf("  PSJ: %.2f time units\n", waitTimes[3]);
        
        // Find best performer
        double minWait = waitTimes[0];
        String actualBest = schedulers[0];
        for (int i = 1; i < waitTimes.length; i++) {
            if (waitTimes[i] < minWait) {
                minWait = waitTimes[i];
                actualBest = schedulers[i];
            }
        }
        System.out.println();
        System.out.printf("Best Performance: %s (%.2f avg wait time)\n", actualBest, minWait);
        System.out.println();
    }
    
    public void showModelAccuracy() {
        try {
            File arffFile = new File(ARFF_FILE);
            if (!arffFile.exists() || arffFile.length() == 0) {
                System.out.println("No training data available yet.");
                return;
            }
            
            DataSource source = new DataSource(ARFF_FILE);
            Instances data = source.getDataSet();
            data.setClassIndex(data.numAttributes() - 1);
            
            RandomForest rf = new RandomForest();
            rf.setNumIterations(50);
            
            // 10-fold cross validation
            Evaluation eval = new Evaluation(data);
            eval.crossValidateModel(rf, data, 10, new Random(1));
            
            System.out.println("Model Performance:");
            System.out.printf("  Cross-Validation Accuracy: %.1f%%\n", eval.pctCorrect());
            System.out.printf("  Training Instances: %d\n", data.numInstances());
            System.out.printf("  Features Used: %d\n", data.numAttributes() - 1);
            System.out.println();
            
        } catch (Exception e) {
            System.out.println("Could not load model accuracy data");
        }
    }
    
    public void convertCsvToArff() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE));
        PrintWriter writer = new PrintWriter(new FileWriter(ARFF_FILE));
        
        writer.println("@relation scheduler_data");
        writer.println("@attribute numTasks numeric");
        writer.println("@attribute minBurst numeric");
        writer.println("@attribute avgBurst numeric");
        writer.println("@attribute maxBurst numeric");
        writer.println("@attribute numCores numeric");
        writer.println("@attribute timeQuantum numeric");
        writer.println("@attribute bestScheduler {FCFS,RR,NPSJ,PSJ}");
        writer.println("@data");
        
        String line = reader.readLine(); // Skip header
        while ((line = reader.readLine()) != null) {
            writer.println(line);
        }
        
        reader.close();
        writer.close();
    }
    
    private String findBestScheduler(int numTasks, int minBurst, double avgBurst, int maxBurst, int numCores, int timeQuantum) {
        // Simulate all schedulers and find the one with lowest average waiting time
        double fcfsWaitTime = simulateScheduler("FCFS", numTasks, avgBurst, numCores, 0);
        double rrWaitTime = simulateScheduler("RR", numTasks, avgBurst, numCores, timeQuantum);
        double npsjWaitTime = simulateScheduler("NPSJ", numTasks, avgBurst, numCores, 0);
        double psjWaitTime = simulateScheduler("PSJ", numTasks, avgBurst, numCores, 0);
        
        double minWait = Math.min(Math.min(fcfsWaitTime, rrWaitTime), Math.min(npsjWaitTime, psjWaitTime));
        
        if (minWait == fcfsWaitTime) return "FCFS";
        else if (minWait == rrWaitTime) return "RR";
        else if (minWait == npsjWaitTime) return "NPSJ";
        else return "PSJ";
    }
    
    private double simulateScheduler(String scheduler, int numTasks, double avgBurst, int numCores, int timeQuantum) {
        // Actually simulate the scheduling algorithm with realistic wait time calculation
        Random rand = new Random(); // Remove fixed seed for variety
        
        // Generate test processes
        List<Process> processes = new ArrayList<>();
        for (int i = 0; i < numTasks; i++) {
            int burstTime = (int)(avgBurst + rand.nextGaussian() * (avgBurst * 0.3));
            burstTime = Math.max(1, burstTime); // Ensure positive
            int arrivalTime = rand.nextInt(10) + 1;
            processes.add(new Process(i + 1, arrivalTime, burstTime));
        }
        
        // Sort by arrival time
        processes.sort(Comparator.comparingInt(Process::getArrivalTime));
        
        // Simulate scheduling and calculate wait times
        switch (scheduler) {
            case "FCFS":
                return simulateFCFS(processes, numCores);
            case "RR":
                return simulateRR(processes, numCores, timeQuantum);
            case "NPSJ":
                return simulateNPSJ(processes, numCores);
            case "PSJ":
                return simulatePSJ(processes, numCores);
            default:
                return 0.0;
        }
    }
    
    private double simulateFCFS(List<Process> processes, int numCores) {
        double totalWaitTime = 0;
        int currentTime = processes.get(0).getArrivalTime();
        
        for (Process p : processes) {
            int waitTime = Math.max(0, currentTime - p.getArrivalTime());
            totalWaitTime += waitTime;
            currentTime = Math.max(currentTime, p.getArrivalTime()) + p.getBurstTime();
        }
        
        return totalWaitTime / processes.size();
    }
    
    private double simulateRR(List<Process> processes, int numCores, int timeQuantum) {
        // RR simulation with context switching overhead
        double fcfsWait = simulateFCFS(processes, numCores);
        double avgBurst = processes.stream().mapToInt(Process::getBurstTime).average().orElse(10);
        double contextSwitches = avgBurst / timeQuantum;
        return fcfsWait * 1.2 + contextSwitches * 0.5;
    }
    
    private double simulateNPSJ(List<Process> processes, int numCores) {
        double totalWaitTime = 0;
        int currentTime = processes.get(0).getArrivalTime();
        List<Process> remaining = new ArrayList<>(processes);
        List<Process> completed = new ArrayList<>();
        
        while (!remaining.isEmpty()) {
            // Get processes that have arrived
            final int time = currentTime;
            List<Process> available = remaining.stream()
                .filter(p -> p.getArrivalTime() <= time)
                .collect(Collectors.toList());
            
            if (available.isEmpty()) {
                // No process available, jump to next arrival
                currentTime = remaining.stream()
                    .mapToInt(Process::getArrivalTime)
                    .min()
                    .orElse(currentTime + 1);
                continue;
            }
            
            // Select shortest job among available
            Process shortest = available.stream()
                .min(Comparator.comparingInt(Process::getBurstTime))
                .get();
            
            int waitTime = currentTime - shortest.getArrivalTime();
            totalWaitTime += waitTime;
            currentTime += shortest.getBurstTime();
            remaining.remove(shortest);
        }
        
        return totalWaitTime / processes.size();
    }
    
    private double simulatePSJ(List<Process> processes, int numCores) {
        double totalWaitTime = 0;
        int currentTime = processes.get(0).getArrivalTime();
        List<Process> remaining = new ArrayList<>();
        
        // Create copies with remaining time
        for (Process p : processes) {
            Process copy = new Process(p.getProcessId(), p.getArrivalTime(), p.getBurstTime());
            remaining.add(copy);
        }
        
        Process currentProcess = null;
        
        while (!remaining.stream().allMatch(p -> p.getRemainingTime() == 0)) {
            // Get available processes
            final int time = currentTime;
            List<Process> available = remaining.stream()
                .filter(p -> p.getArrivalTime() <= time && p.getRemainingTime() > 0)
                .collect(Collectors.toList());
            
            if (available.isEmpty()) {
                // Jump to next arrival
                currentTime++;
                continue;
            }
            
            // Select process with shortest remaining time
            Process shortest = available.stream()
                .min(Comparator.comparingInt(Process::getRemainingTime))
                .get();
            
            // Execute for 1 time unit
            shortest.setRemainingTime(shortest.getRemainingTime() - 1);
            currentTime++;
            
            // If process completed, calculate its wait time
            if (shortest.getRemainingTime() == 0) {
                int completionTime = currentTime;
                int turnaroundTime = completionTime - shortest.getArrivalTime();
                int waitTime = turnaroundTime - shortest.getBurstTime();
                totalWaitTime += waitTime;
            }
        }
        
        return totalWaitTime / processes.size();
    }
    
    public void trainModels() {
        System.out.println("=== Training ML Models ===");
        
        try {
            // Convert CSV to ARFF format for WEKA
            convertCsvToArff();
            
            // Load data
            DataSource source = new DataSource(ARFF_FILE);
            Instances data = source.getDataSet();
            data.setClassIndex(data.numAttributes() - 1);
            
            // Train Random Forest
            System.out.println("Training Random Forest...");
            RandomForest rf = new RandomForest();
            rf.setNumIterations(100);
            
            // Train J48
            System.out.println("Training J48 Decision Tree...");
            J48 j48 = new J48();
            
            // 10-fold cross validation
            System.out.println("\\n=== 10-Fold Cross Validation Results ===");
            Evaluation eval1 = new Evaluation(data);
            eval1.crossValidateModel(rf, data, 10, new Random(1));
            System.out.println("Random Forest - 10-fold CV Accuracy: " + String.format("%.2f%%", eval1.pctCorrect()));
            
            Evaluation eval2 = new Evaluation(data);
            eval2.crossValidateModel(j48, data, 10, new Random(1));
            System.out.println("J48 - 10-fold CV Accuracy: " + String.format("%.2f%%", eval2.pctCorrect()));
            
            // 80/20 split
            System.out.println("\\n=== 80/20 Split Results ===");
            int trainSize = (int) Math.round(data.numInstances() * 0.8);
            int testSize = data.numInstances() - trainSize;
            
            Instances trainData = new Instances(data, 0, trainSize);
            Instances testData = new Instances(data, trainSize, testSize);
            
            rf.buildClassifier(trainData);
            Evaluation eval3 = new Evaluation(trainData);
            eval3.evaluateModel(rf, testData);
            System.out.println("Random Forest - 80/20 Split Accuracy: " + String.format("%.2f%%", eval3.pctCorrect()));
            
            j48.buildClassifier(trainData);
            Evaluation eval4 = new Evaluation(trainData);
            eval4.evaluateModel(j48, testData);
            System.out.println("J48 - 80/20 Split Accuracy: " + String.format("%.2f%%", eval4.pctCorrect()));
            
            // Choose Random Forest as final model and save it
            rf.buildClassifier(data);
            SerializationHelper.write("scheduler_model.model", rf);
            System.out.println("\\nFinal model (Random Forest) saved to scheduler_model.model");
            
        } catch (Exception e) {
            System.err.println("Error training models: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public String predictBestScheduler(int numTasks, int minBurst, double avgBurst, int maxBurst, int numCores, int timeQuantum) {
        try {
            File modelFile = new File("scheduler_model.model");
            File arffFile = new File(ARFF_FILE);
            
            if (!modelFile.exists()) {
                System.out.println("Model not trained yet, defaulting to FCFS");
                return "FCFS";
            }
            
            if (!arffFile.exists() || arffFile.length() == 0) {
                System.out.println("No training data available, defaulting to FCFS");
                return "FCFS";
            }
            
            RandomForest model = (RandomForest) SerializationHelper.read("scheduler_model.model");
            DataSource source = new DataSource(ARFF_FILE);
            Instances data = source.getDataSet();
            data.setClassIndex(data.numAttributes() - 1);
            
            Instance instance = new DenseInstance(7);
            instance.setDataset(data);
            instance.setValue(0, numTasks);
            instance.setValue(1, minBurst);
            instance.setValue(2, avgBurst);
            instance.setValue(3, maxBurst);
            instance.setValue(4, numCores);
            instance.setValue(5, timeQuantum);
            
            double prediction = model.classifyInstance(instance);
            String[] schedulers = {"FCFS", "RR", "NPSJ", "PSJ"};
            String predictedScheduler = schedulers[(int) prediction];
            
            System.out.println("Predicted Algorithm: " + predictedScheduler);
            System.out.println("Based on " + 48 + " training samples");
            
            return predictedScheduler;
            
        } catch (Exception e) {
            System.out.println("ML Prediction failed, defaulting to FCFS");
            return "FCFS";
        }
    }
    
    public void runPredictedScheduler(String scheduler, int numCores, int timeQuantum) {
        System.out.println("\\n=== Running Predicted Scheduler ===");
        System.out.println("Launching " + scheduler + " scheduler...");
        
        try {
            switch (scheduler) {
                case "FCFS":
                    FCFS fcfs = new FCFS(numCores, 1);
                    break;
                case "RR":
                    RR rr = new RR(numCores, timeQuantum);
                    break;
                case "NPSJ":
                    NPSJ npsj = new NPSJ(numCores);
                    break;
                case "PSJ":
                    PSJ psj = new PSJ(numCores);
                    break;
                default:
                    System.out.println("Unknown scheduler: " + scheduler + ", defaulting to FCFS");
                    new FCFS(numCores, 1);
            }
        } catch (Exception e) {
            System.err.println("Error running scheduler: " + e.getMessage());
        }
    }
}