//Code completed by Roland Okungbowa

public class SynchronizedPrinter {
    private static final Object lock = new Object();
    
    public static void print(String message) {
        synchronized (lock) {
            System.out.println(message);
        }
    }
    
    public static void printWithCategory(String category, String message) {
        synchronized (lock) {
            System.out.println(category + ": " + message);
        }
    }
    
    public static void printSeparator() {
        synchronized (lock) {
            System.out.println("=" + "=".repeat(50));
        }
    }
    
    public static void printBlankLine() {
        synchronized (lock) {
            System.out.println();
        }
    }
}