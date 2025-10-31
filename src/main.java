//Task 4: Command Line
// Code Written by: Roland Okungbowa

public class main {

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Please provide at least one argument. (e.g., -S (1-4) or -ML)");
            return;
        }
        String option1 = args[0];
        String option2;
        String option3;
        String option4;
        switch (option1) {
            case "-s":
            case "-S":
                if (args.length < 2) {
                    System.out.println("Please provide a number (1-4) for the -S option.");
                    return;
                }
                option2 = args[1];
                if (args.length == 4){
                    option3 = args[2];
                    option4 = args[3];
                    if(option3.equals("-C") || option3.equals("-c")) {
                        int num = Integer.parseInt(option4);
                        if (num >=1 && num <=4) {
                            switch (option2) {
                                case "1":
                                    FCFS.main(new String[]{option4});
                                    return;
                                case "2":
                                    RR.main(new String[]{option4});
                                    return;
                                case "3":
                                    NPSJ.main(new String[]{option4});
                                    return;
                                case "4":
                                    PSJ.main(new String[]{option4});
                                    return;
                                default:
                                    System.out.println("Unknown option2: " + option2);
                                    System.out.println("Available tasks: -S1 (FCFS), -S2 (RR), -S3 (NPSJ), -S4 (PSJ)");
                                    return;
                            }
                        } else {
                            System.out.println("Invalid number of cores: " + option4);
                            System.out.println("Please provide a number between 1 and 4.");
                            return;
                        }
                    } else {
                        System.out.println("Unknown option3: " + option3);
                        System.out.println("Expected -C or -c for core count.");
                    }
                } else if (args.length == 2){
                    switch (option2) {
                        case "1":
                            System.out.println("Executing FCFS with default 1 core.\n");
                            FCFS.main(new String[]{"1"});
                            break;
                        case "2":
                            System.out.println("Executing RR with default 1 core.\n");
                            RR.main(new String[]{"1"});
                            break;
                        case "3":
                            System.out.println("Executing NPSJ with default 1 core.\n");
                            NPSJ.main(new String[]{"1"});
                            break;
                        case "4":
                            System.out.println("Executing PSJ with default 1 core.\n");
                            PSJ.main(new String[]{"1"});
                            break;
                        default:
                            System.out.println("Unknown option2: " + option2);
                            System.out.println("Available tasks: -S1 (FCFS), -S2 (RR), -S3 (NPSJ), -S4 (PSJ)");
                    }
                    break;
                } else {
                    System.out.println("Invalid number of arguments for -S option.");
                    System.out.println("Usage: -S (1-4) or -S (1-4) -C (1-4)");
                    return;
                }
            case "-C":
            case "-c":
            if (args.length < 2) {
                    System.out.println("Please provide a number (1-4) for the number of cores followed by scheduling option. (e.g., -C 2 -S 1)");
                    return;
                }
                option2 = args[1];
                int num = Integer.parseInt(option2);
                if (num >=1 && num <=4) {
                if (args.length < 4) {
                    System.out.println("Please provide scheduling option after number of cores. (e.g., -C 2 -S 1)");
                    return;
                }
                option3 = args[2];
                option4 = args[3];
                } else {
                    System.out.println("Invalid number of cores: " + option2);
                    System.out.println("Please provide a number between 1 and 4.");
                    return;
                }
                if(option3.equals("-S") || option3.equals("-s")) {
                    switch (option4) {
                        case "1":
                            FCFS.main(new String[]{option2});
                            break;
                        case "2":
                            RR.main(new String[]{option2});
                            break;
                        case "3":
                            NPSJ.main(new String[]{option2});
                            break;
                        case "4":
                            PSJ.main(new String[]{option2});
                            break;
                        default:
                            System.out.println("Unknown option4: " + option4);
                            System.out.println("Available tasks: -S1 (FCFS), -S2 (RR), -S3 (NPSJ), -S4 (PSJ)");
                    }
                } else {
                    System.out.println("Unknown option3: " + option3);
                    System.out.println("Expected -S or -s for scheduling algorithm selection.");
                }
                break;
            case "-ml":
            case "-ML":
            case "-Ml":
            case "-mL":
                ML.main(new String[]{});
                break;
            default:
                System.out.println("Unknown option1: " + option1);
                System.out.println("Available options: -S 1 (FCFS), -S 2 (RR), -S 3 (NPSJ), -S 4 (PSJ), -ML (ML Scheduling)");
        }
    }
}
