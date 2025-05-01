import java.io.Serializable;

/**
 * Représente un message dans le système distribué, contenant des horloges logiques
 * et des informations sur une tâche (ID, type, statut).
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    int scalarClock;
    int[] vectorClock;
    int[][] matrixClock;
    int sourceId;
    int taskId; // Identifiant unique de la tâche
    String taskType; // Type de tâche (par exemple, "Calcul", "Analyse")
    String status; // Statut de la tâche (par exemple, "Assignée", "Terminée")

    /**
     * Constructeur pour un message avec des données de tâche.
     *
     * @param scalarClock Horloge scalaire.
     * @param vectorClock Horloge vectorielle.
     * @param matrixClock Horloge matricielle.
     * @param sourceId ID du processus émetteur.
     * @param taskId ID unique de la tâche.
     * @param taskType Type de la tâche.
     * @param status Statut de la tâche.
     */
    public Message(int scalarClock, int[] vectorClock, int[][] matrixClock, int sourceId,
                   int taskId, String taskType, String status) {
        this.scalarClock = scalarClock;
        this.vectorClock = vectorClock;
        this.matrixClock = matrixClock;
        this.sourceId = sourceId;
        this.taskId = taskId;
        this.taskType = taskType;
        this.status = status;
    }
}