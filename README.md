# Project 3 ReadMe Docs

## Compilation and Execution Commands
- Here is the command to use to compile all files (Weka and java files):
```
$ javac -cp "lib\weka.jar" src\main.java src\FCFS.java src\RR.java src\NPSJ.java src\PSJ.java src\ML.java
```

- Here is the command structure to execute Tasks 1 & 2 (There are 3 optional forms):
    - Only Scheduling Type Specified (1 core default):

```
$ java main -(S/s) <1-4>
```

    - Scheduling Type First with Core Specification:
```
$ java main -(S/s) <1-4> -(C/c) <1-4>
```


    - Core Specification First then Scheduling Types:
```
$ java main -(C/c) <1-4> - (S/s) <1-4>
```

- Here is the command structure to execute Tasks 3:
```
$ java --add-opens java.base/java.lang=ALL-UNNAMED -cp "src;lib/weka.jar" ML
```