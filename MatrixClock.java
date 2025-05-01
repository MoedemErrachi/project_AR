import java.io.Serializable;
import java.util.Arrays;

public class MatrixClock implements Serializable {
    private final int[][] clock;
    private final int processId;
    private final int numProcesses;

    public MatrixClock(int processId, int numProcesses) {
        this.processId = processId;
        this.numProcesses = numProcesses;
        this.clock = new int[numProcesses][numProcesses];
    }

    public synchronized void localEvent() {
        clock[processId][processId]++;
        System.out.println("Process " + processId + " - Matrix Clock updated to:\n" + matrixToString());
    }

    public synchronized int[][] sendEvent(int targetid) {
        clock[processId][processId]++;
        clock[processId][targetid]++;
        System.out.println("Process " + processId + " - Matrix Clock updated to:\n" + matrixToString() + " (send)");
        return deepCopy(clock);
    }

    public synchronized void receiveEvent(int[][] receivedClock) {
        for (int i = 0; i < numProcesses; i++) {
            for (int j = 0; j < numProcesses; j++) {
                clock[i][j] = Math.max(clock[i][j], receivedClock[i][j]);
            }
        }
        clock[processId][processId]++;
        System.out.println("Process " + processId + " - Matrix Clock updated to:\n" + matrixToString() + " (receive)");
    }

    String matrixToString() {
        StringBuilder sb = new StringBuilder();
        for (int[] row : clock) {
            sb.append(Arrays.toString(row)).append("\n");
        }
        return sb.toString();
    }

    private int[][] deepCopy(int[][] matrix) {
        int[][] copy = new int[numProcesses][numProcesses];
        for (int i = 0; i < numProcesses; i++) {
            System.arraycopy(matrix[i], 0, copy[i], 0, numProcesses);
        }
        return copy;
    }
    
}