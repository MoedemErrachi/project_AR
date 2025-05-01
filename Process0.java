import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Arrays;
import java.util.Random;

/**
 * Processus 0 dans un système distribué, gérant des événements locaux et l'envoi/réception
 * de messages représentant des tâches avec horloges logiques.
 */
public class Process0 {
    private static final int ID = 0;
    private static final int NUM_PROCESSES = 4;
    private static final int BASE_PORT = 8000;
    private static final ScalarClock scalarClock = new ScalarClock(ID);
    private static final VectorClock vectorClock = new VectorClock(ID, NUM_PROCESSES);
    private static final MatrixClock matrixClock = new MatrixClock(ID, NUM_PROCESSES);
    private static ServerSocket serverSocket;
    private static final Random random = new Random(); // Pour générer des taskId et taskType
    private static final String[] TASK_TYPES = {"Calcul", "Analyse", "Stockage", "Transfert"};
    private static final String[] TASK_STATUSES = {"Assignée", "En cours", "Terminée"};

    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(BASE_PORT + ID);
            new Thread(() -> acceptConnections()).start();
            execute();
        } catch (IOException e) {
            System.err.println("Error starting Process " + ID + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    private static void acceptConnections() {
        while (!serverSocket.isClosed()) {
            Socket socket = null;
            ObjectOutputStream out = null;
            ObjectInputStream in = null;
            try {
                socket = serverSocket.accept();
                System.out.println("Received connection attempt on port " + socket.getPort());
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                int sourceId = in.readInt();
                if (sourceId >= 0 && sourceId < NUM_PROCESSES && sourceId != ID) {
                    Object obj = in.readObject();
                    if (obj instanceof Message) {
                        Message message = (Message) obj;
                        System.out.println("Connection from Process " + sourceId);
                        System.out.println("Received Task: ID=" + message.taskId + ", Type=" + message.taskType + ", Status=" + message.status);
                        System.out.println("Received Message Scalar Clock: " + message.scalarClock);
                        System.out.println("Received Message Vector Clock: " + Arrays.toString(message.vectorClock));
                        System.out.println("Received Message Matrix Clock:\n" + matrixToString(message.matrixClock));
                        scalarClock.receiveEvent(message.scalarClock);
                        vectorClock.receiveEvent(message.vectorClock);
                        matrixClock.receiveEvent(message.matrixClock);
                    } else {
                        System.out.println("Received invalid message type from Process " + sourceId);
                    }
                } else {
                    System.out.println("Invalid source ID: " + sourceId + ", closing connection");
                }
            } catch (IOException | ClassNotFoundException e) {
                if (!serverSocket.isClosed()) {
                    System.err.println("Error processing connection: " + e.getMessage());
                }
            } finally {
                try {
                    if (out != null) out.close();
                    if (in != null) in.close();
                    if (socket != null && !socket.isClosed()) socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing resources: " + e.getMessage());
                }
            }
        }
    }

    private static class Connection {
        Socket socket;
        ObjectOutputStream out;
        ObjectInputStream in;

        Connection(Socket socket, ObjectOutputStream out, ObjectInputStream in) {
            this.socket = socket;
            this.out = out;
            this.in = in;
        }
    }

    private static Connection connectToProcess(int targetId) throws IOException {
        if (targetId == ID || targetId < 0 || targetId >= NUM_PROCESSES) {
            throw new IllegalArgumentException("Invalid target process ID: " + targetId);
        }
        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            System.out.println("Attempting to connect to Process " + targetId + " on port " + (BASE_PORT + targetId));
            socket = new Socket("localhost", BASE_PORT + targetId);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            out.writeInt(ID);
            out.flush();
            System.out.println("Successfully connected to Process " + targetId);
            return new Connection(socket, out, in);
        } catch (IOException e) {
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException ex) {
                System.err.println("Error closing resources: " + ex.getMessage());
            }
            throw new IOException("Could not connect to Process " + targetId + ": " + e.getMessage());
        }
    }

    private static void execute() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Process " + ID + " started. Commands: 'local' for local event, 'send <process_id>' to send task, 'exit' to quit");

        while (true) {
            String input = scanner.nextLine().trim();
            try {
                if (input.equals("local")) {
                    scalarClock.localEvent();
                    vectorClock.localEvent();
                    matrixClock.localEvent();
                    System.out.println("Local event processed");
                    /*System.out.println("Local Scalar Clock: " + scalarClock.getClock());
                    System.out.println("Local Vector Clock: " + Arrays.toString(vectorClock.getClock()));
                    System.out.println("Local Matrix Clock:\n" + matrixToString(matrixClock.getClock()));*/
                } else if (input.startsWith("send ")) {
                    int target = Integer.parseInt(input.split(" ")[1]);
                    if (target == ID || target < 0 || target >= NUM_PROCESSES) {
                        System.out.println("Invalid target process ID: " + target);
                        continue;
                    }
                    try {
                        Connection conn = connectToProcess(target);
                        try {
                            // Générer des données de tâche
                            int taskId = random.nextInt(1000); // ID de tâche aléatoire (0-999)
                            String taskType = TASK_TYPES[random.nextInt(TASK_TYPES.length)];
                            String status = TASK_STATUSES[random.nextInt(TASK_STATUSES.length)];
                            Message message = new Message(
                                scalarClock.sendEvent(),
                                vectorClock.sendEvent(),
                                matrixClock.sendEvent(target),
                                ID,
                                taskId,
                                taskType,
                                status
                            );
                            conn.out.writeObject(message);
                            conn.out.flush();
                            /*System.out.println("Connection to Process " + target);
                            System.out.println("Sent Task: ID=" + taskId + ", Type=" + taskType + ", Status=" + status);
                            System.out.println("Sent Message Scalar Clock: " + message.scalarClock);
                            System.out.println("Sent Message Vector Clock: " + Arrays.toString(message.vectorClock));
                            System.out.println("Sent Message Matrix Clock:\n" + matrixToString(message.matrixClock));
                            System.out.println("Local Scalar Clock: " + scalarClock.getClock());
                            System.out.println("Local Vector Clock: " + Arrays.toString(vectorClock.getClock()));
                            System.out.println("Local Matrix Clock:\n" + matrixToString(matrixClock.getClock()));*/
                            System.out.println("Task sent to Process " + target);
                        } finally {
                            try {
                                if (conn.out != null) conn.out.close();
                                if (conn.in != null) conn.in.close();
                                if (conn.socket != null && !conn.socket.isClosed()) conn.socket.close();
                            } catch (IOException e) {
                                System.err.println("Error closing resources: " + e.getMessage());
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("Failed to send task to Process " + target + ": " + e.getMessage());
                    }
                } else if (input.equals("exit")) {
                    break;
                } else {
                    System.out.println("Invalid command. Use 'local', 'send <process_id>', or 'exit'");
                }
            } catch (Exception e) {
                System.out.println("Error processing command: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private static String matrixToString(int[][] matrix) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : matrix) {
            sb.append(Arrays.toString(row)).append("\n");
        }
        return sb.toString();
    }

    private static void cleanup() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }
}