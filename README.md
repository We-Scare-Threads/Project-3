# Project 3 ReadMe Docs

## Compilation and Execution Commands
- Here is the command to use to compile all files (Weka and java files):
```
$ javac -cp "lib/weka.jar" -d . src/*.java
```

Alternative compilation (individual files):
```
$ javac -cp "lib/weka.jar" src/main.java src/FCFS.java src/RR.java src/NPSJ.java src/PSJ.java src/ML.java src/Scheduler.java src/Process.java
```

### Here is the command structure to execute Tasks 1 & 2 (There are 3 optional forms):

__Only Scheduling Type Specified (1 core default):__

```
$ java main -(S/s) <1-4>
```

__Scheduling Type First then Core Specification:__
```
$ java main -(S/s) <1-4> -(C/c) <1-4>
```

__Core Specification First then Scheduling Types:__
```
$ java main -(C/c) <1-4> -(S/s) <1-4>
```

### Round Robin (RR) Scheduling with Time Quantum:

__Round Robin with Time Quantum (Scheduling First):__
```
$ java main -(S/s) 2 <timeQuantum> -(C/c) <1-4>
```

__Round Robin with Time Quantum (Cores First):__
```
$ java main -(C/c) <1-4> -(S/s) 2 <timeQuantum>
```

**Examples:**
```
$ java main -s 2 5 -c 2    # RR with time quantum 5, 2 cores
$ java main -c 3 -s 2 10   # 3 cores, RR with time quantum 10
```

### Scheduling Algorithm Key:
- **1**: FCFS (First Come First Serve)
- **2**: RR (Round Robin) - requires time quantum when specified
- **3**: NPSJ (Non-Preemptive Shortest Job)
- **4**: PSJ (Preemptive Shortest Job)

### Here is the command to execute Task 3:
```
$ java --add-opens java.base/java.lang=ALL-UNNAMED -cp "src;lib/weka.jar" ML
```