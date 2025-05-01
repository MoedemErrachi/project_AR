import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;
import javax.swing.Timer;
import javax.swing.border.*;

public class MatrixClockSimulator {
    private final List<MatrixClock> processes;
    private final JFrame frame;
    private final JPanel processPanel;
    private final JTextArea logArea;
    private final JList<String> eventHistory;
    private final int numProcesses;
    private final Color[] processColors;
    private final DefaultListModel<String> historyModel;
    private final Map<Integer, Timer> highlightTimers = new HashMap<>();

    public MatrixClockSimulator(int numProcesses) {
        this.numProcesses = numProcesses;
        this.processes = new ArrayList<>();
        this.processColors = generateDistinctColors(numProcesses);
        this.historyModel = new DefaultListModel<>();

        // Initialize UI components
        this.frame = createMainFrame();
        this.processPanel = createProcessPanel();
        this.logArea = createLogArea();
        this.eventHistory = createHistoryList();

        setupProcesses();
        setupUI();
    }

    private JFrame createMainFrame() {
        JFrame frame = new JFrame("Matrix Clock Simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setSize(1200, 800);
        frame.setMinimumSize(new Dimension(1000, 700));
        return frame;
    }

    private JPanel createProcessPanel() {
        JPanel panel = new JPanel(new GridLayout(1, numProcesses, 15, 15));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(245, 245, 245));
        return panel;
    }

    private JTextArea createLogArea() {
        JTextArea area = new JTextArea(10, 60);
        area.setEditable(false);
        area.setFont(new Font("Consolas", Font.PLAIN, 13));
        area.setBackground(new Color(253, 253, 253));
        area.setBorder(new CompoundBorder(
            new LineBorder(new Color(200, 200, 200)),
            new EmptyBorder(5, 5, 5, 5)));
        return area;
    }

    private JList<String> createHistoryList() {
        JList<String> list = new JList<>(historyModel);
        list.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        list.setBackground(new Color(250, 250, 250));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new HistoryListRenderer());
        return list;
    }

    private void setupProcesses() {
        for (int i = 0; i < numProcesses; i++) {
            processes.add(new MatrixClock(i, numProcesses));
        }
    }

    private void setupUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create main components
        JPanel headerPanel = createHeaderPanel();
        JPanel controlPanel = createControlPanel();
        JPanel visualizationPanel = createVisualizationPanel();

        // Add components to frame
        frame.add(headerPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(processPanel), BorderLayout.CENTER);
        frame.add(controlPanel, BorderLayout.EAST);
        frame.add(visualizationPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
        updateProcessDisplays();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new GradientPanel();
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(frame.getWidth(), 80));

        JLabel title = new JLabel("Matrix Clock Simulation", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        panel.add(title, BorderLayout.CENTER);

        JLabel subTitle = new JLabel("Distributed Systems Event Visualization", SwingConstants.CENTER);
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subTitle.setForeground(new Color(220, 220, 220));
        panel.add(subTitle, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new CompoundBorder(
            new EmptyBorder(15, 15, 15, 15),
            new LineBorder(new Color(200, 200, 200))));
        panel.setBackground(Color.WHITE);

        // Event controls
        JPanel eventPanel = new JPanel(new GridLayout(0, 1, 5, 10));
        eventPanel.setBorder(new TitledBorder("Event Controls"));
        eventPanel.setBackground(panel.getBackground());

        JComboBox<Integer> senderCombo = new JComboBox<>();
        JComboBox<Integer> receiverCombo = new JComboBox<>();
        for (int i = 0; i < numProcesses; i++) {
            senderCombo.addItem(i);
            receiverCombo.addItem(i);
        }

        JButton localEventBtn = createStyledButton("Trigger Local Event", 
            new Color(46, 125, 50), 14);
        JButton sendEventBtn = createStyledButton("Send Message", 
            new Color(30, 136, 229), 14);
        JButton resetBtn = createStyledButton("Reset Simulation", 
            new Color(198, 40, 40), 14);

        localEventBtn.addActionListener(e -> triggerLocalEvent(senderCombo.getSelectedIndex()));
        sendEventBtn.addActionListener(e -> sendMessage(
            senderCombo.getSelectedIndex(), 
            receiverCombo.getSelectedIndex()));
        resetBtn.addActionListener(e -> resetSimulation());

        eventPanel.add(new JLabel("Select Process:"));
        eventPanel.add(senderCombo);
        eventPanel.add(localEventBtn);
        eventPanel.add(new JSeparator());
        eventPanel.add(new JLabel("Send to Process:"));
        eventPanel.add(receiverCombo);
        eventPanel.add(sendEventBtn);
        eventPanel.add(new JSeparator());
        eventPanel.add(resetBtn);

        // Statistics panel
        JPanel statsPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        statsPanel.setBorder(new TitledBorder("Simulation Statistics"));
        statsPanel.setBackground(panel.getBackground());

        // Add components
        panel.add(eventPanel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(statsPanel);

        return panel;
    }

    private JPanel createVisualizationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Create tabbed pane
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Log tab
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(new TitledBorder("Event Log"));
        tabs.addTab("Log", logScroll);

        // History tab
        JScrollPane historyScroll = new JScrollPane(eventHistory);
        historyScroll.setBorder(new TitledBorder("Event History"));
        tabs.addTab("History", historyScroll);

        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    private JButton createStyledButton(String text, Color bgColor, int fontSize) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        button.setBackground(bgColor);
        button.setForeground(Color.green);
        button.setFocusPainted(false);
        button.setBorder(new CompoundBorder(
            new LineBorder(bgColor.darker()),
            new EmptyBorder(8, 15, 8, 15)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.brighter());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }

    private void triggerLocalEvent(int processId) {
        processes.get(processId).localEvent();
        updateProcessDisplays();
        highlightProcess(processId, processColors[processId]);
        logEvent("Local event at Process " + processId, processColors[processId]);
        historyModel.addElement(String.format("[%tT] Process %d: Local Event", new Date(), processId));
    }

    private void sendMessage(int senderId, int receiverId) {
        if (senderId == receiverId) {
            showWarning("Sender and receiver must be different");
            return;
        }
        
        int[][] sentClock = processes.get(senderId).sendEvent(receiverId);
        processes.get(receiverId).receiveEvent(sentClock);
        
        updateProcessDisplays();
        highlightProcess(senderId, processColors[senderId]);
        highlightProcess(receiverId, processColors[receiverId]);
        
        Color blendColor = blendColors(processColors[senderId], processColors[receiverId]);
        String message = String.format("Message from P%d to P%d", senderId, receiverId);
        logEvent(message, blendColor);
        
        historyModel.addElement(String.format("[%tT] %s", new Date(), message));
    }

    private void updateProcessDisplays() {
        processPanel.removeAll();
        
        for (int i = 0; i < numProcesses; i++) {
            JPanel panel = createProcessCard(i);
            processPanel.add(panel);
        }
        
        processPanel.revalidate();
        processPanel.repaint();
    }

    private JPanel createProcessCard(int processId) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new CompoundBorder(
            new MatteBorder(2, 2, 2, 2, processColors[processId]),
            new EmptyBorder(10, 10, 10, 10)));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Process " + processId, SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(processColors[processId].darker());
        panel.add(title, BorderLayout.NORTH);

        JTextArea clockDisplay = new JTextArea(processes.get(processId).matrixToString());
        clockDisplay.setEditable(false);
        clockDisplay.setFont(new Font("Consolas", Font.PLAIN, 13));
        clockDisplay.setBackground(new Color(250, 250, 250));
        panel.add(new JScrollPane(clockDisplay), BorderLayout.CENTER);

        return panel;
    }

    private void highlightProcess(int processId, Color color) {
        // Cancel any existing timer for this process
        if (highlightTimers.containsKey(processId)) {
            highlightTimers.get(processId).stop();
        }

        Component[] cards = processPanel.getComponents();
        if (processId >= 0 && processId < cards.length) {
            JPanel card = (JPanel) cards[processId];
            Border originalBorder = card.getBorder();
            card.setBorder(new CompoundBorder(
                new MatteBorder(4, 4, 4, 4, color.brighter()),
                new EmptyBorder(8, 8, 8, 8)));

            Timer timer = new Timer(1000, e -> {
                card.setBorder(originalBorder);
                highlightTimers.remove(processId);
            });
            timer.setRepeats(false);
            timer.start();
            highlightTimers.put(processId, timer);
        }
    }

    private void logEvent(String message, Color color) {
        logArea.setForeground(color.darker());
        logArea.append(String.format("[%tT] %s\n", new Date(), message));
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void resetSimulation() {
        for (int i = 0; i < numProcesses; i++) {
            processes.set(i, new MatrixClock(i, numProcesses));
        }
        logArea.setText("");
        historyModel.clear();
        updateProcessDisplays();
        logEvent("Simulation reset", new Color(100, 100, 100));
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(frame, 
            message, 
            "Warning", 
            JOptionPane.WARNING_MESSAGE);
    }

    private Color[] generateDistinctColors(int count) {
        Color[] colors = new Color[count];
        float hueStep = 1.0f / count;
        for (int i = 0; i < count; i++) {
            colors[i] = Color.getHSBColor(i * hueStep, 0.7f, 0.9f);
        }
        return colors;
    }

    private Color blendColors(Color c1, Color c2) {
        return new Color(
            (c1.getRed() + c2.getRed()) / 2,
            (c1.getGreen() + c2.getGreen()) / 2,
            (c1.getBlue() + c2.getBlue()) / 2
        );
    }

    private static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            GradientPaint gradient = new GradientPaint(
                0, 0, new Color(70, 130, 180), 
                getWidth(), 0, new Color(25, 25, 112));
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private static class HistoryListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
            label.setBorder(new EmptyBorder(3, 5, 3, 5));
            return label;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JPanel inputPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            // Title
            JLabel title = new JLabel("Matrix Clock Simulation Setup");
            title.setFont(new Font("Segoe UI", Font.BOLD, 16));
            gbc.gridwidth = 2;
            gbc.gridx = 0;
            gbc.gridy = 0;
            inputPanel.add(title, gbc);

            // Process count
            gbc.gridy++;
            gbc.gridwidth = 1;
            inputPanel.add(new JLabel("Number of processes:"), gbc);
            gbc.gridx = 1;
            JSpinner processSpinner = new JSpinner(new SpinnerNumberModel(3, 2, 10, 1));
            processSpinner.setEditor(new JSpinner.NumberEditor(processSpinner, "#"));
            inputPanel.add(processSpinner, gbc);

            int result = JOptionPane.showConfirmDialog(null, inputPanel, 
                "Simulation Setup", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
            if (result == JOptionPane.OK_OPTION) {
                int numProcesses = (Integer) processSpinner.getValue();
                new MatrixClockSimulator(numProcesses);
            }
        });
    }
}